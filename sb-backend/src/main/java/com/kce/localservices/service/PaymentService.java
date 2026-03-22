package com.kce.localservices.service;

import com.kce.localservices.entity.Booking;
import com.kce.localservices.entity.Payment;
import com.kce.localservices.entity.Service;
import com.kce.localservices.entity.User;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.repository.PaymentRepository;
import com.kce.localservices.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Scanner;

@org.springframework.stereotype.Service
public class PaymentService {

    @Value("${razorpay.key.id:NOT_SET}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:NOT_SET}")
    private String razorpayKeySecret;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Map<String, Object> createOrder(Integer bookingId) throws Exception {

        if ("NOT_SET".equals(razorpayKeyId) || razorpayKeyId.contains("YOUR_KEY")) {
            throw new RuntimeException(
                "Razorpay keys not configured. Add razorpay.key.id and " +
                "razorpay.key.secret to application.properties. " +
                "Get free test keys at https://dashboard.razorpay.com");
        }

        User currentUser = userService.getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomerId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only pay for your own bookings.");
        }

        if (!"Confirmed".equals(booking.getStatus())) {
            throw new RuntimeException(
                "Payment is only available for Confirmed bookings. Current status: "
                + booking.getStatus());
        }

        paymentRepository.findByBookingId(bookingId).ifPresent(p -> {
            if ("SUCCESS".equals(p.getStatus())) {
                throw new RuntimeException("This booking has already been paid.");
            }
        });

        Service service = serviceRepository.findById(booking.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        BigDecimal amount = service.getPrice();
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();

        // ── Call Razorpay using standard Java HttpURLConnection (no external JSON lib) ──
        String credentials = Base64.getEncoder()
                .encodeToString((razorpayKeyId + ":" + razorpayKeySecret).getBytes());

        // Build JSON body manually — no org.json needed
        String requestBody = String.format(
                "{\"amount\":%d,\"currency\":\"INR\",\"receipt\":\"booking_%d\"}",
                amountInPaise, bookingId);

        URL url = new URL("https://api.razorpay.com/v1/orders");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + credentials);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int statusCode = conn.getResponseCode();
        String responseBody;
        try (Scanner scanner = new Scanner(
                statusCode == 200 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8)) {
            responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }

        if (statusCode != 200) {
            throw new RuntimeException("Razorpay API error (" + statusCode + "): " + responseBody);
        }

        // Parse Razorpay order ID from JSON response using simple string extraction
        String razorpayOrderId = extractJsonValue(responseBody, "id");
        if (razorpayOrderId == null || razorpayOrderId.isEmpty()) {
            throw new RuntimeException("Could not parse Razorpay order ID from response: " + responseBody);
        }

        // Save PENDING payment record
        Payment payment = paymentRepository.findByBookingId(bookingId).orElse(new Payment());
        payment.setBookingId(bookingId);
        payment.setCustomerId(currentUser.getId());
        payment.setAmount(amount);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", razorpayOrderId);
        result.put("amount", amountInPaise);
        result.put("currency", "INR");
        result.put("keyId", razorpayKeyId);
        result.put("bookingId", bookingId);
        result.put("serviceName", service.getServiceName());
        return result;
    }

    @Transactional
    public Map<String, Object> verifyPayment(String razorpayOrderId,
                                              String razorpayPaymentId,
                                              String razorpaySignature) throws Exception {
        // Verify HMAC-SHA256 signature — fraud prevention
        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String generatedSignature = hmacSHA256(payload, razorpayKeySecret);

        if (!generatedSignature.equals(razorpaySignature)) {
            throw new RuntimeException("Payment verification failed. Invalid signature.");
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found."));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("Paid");
        bookingRepository.save(booking);

        Service service = serviceRepository.findById(booking.getServiceId()).orElse(null);
        String serviceName = service != null ? service.getServiceName() : "a service";

        notificationService.createNotification(
                booking.getProviderId(),
                "Payment Received 💰",
                "Payment of ₹" + payment.getAmount() + " received for \"" + serviceName + "\".",
                "BOOKING_CONFIRMED", booking.getId());

        notificationService.createNotification(
                booking.getCustomerId(),
                "Payment Successful ✅",
                "Your payment of ₹" + payment.getAmount() + " for \"" + serviceName + "\" was successful.",
                "BOOKING_CONFIRMED", booking.getId());

        return Map.of(
                "message", "Payment verified successfully.",
                "paymentId", razorpayPaymentId,
                "status", "SUCCESS");
    }

    public Map<String, Object> getPaymentStatus(Integer bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", p.getStatus());
                    map.put("amount", p.getAmount());
                    map.put("paymentId", p.getRazorpayPaymentId());
                    map.put("createdAt", p.getCreatedAt());
                    return map;
                })
                .orElse(Map.of("status", "NOT_PAID"));
    }

    /**
     * Simple JSON string value extractor — no external library needed.
     * Handles: "key":"value" patterns in Razorpay responses.
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return null;
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private String hmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}