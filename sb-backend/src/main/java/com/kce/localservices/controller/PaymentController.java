package com.kce.localservices.controller;

import com.kce.localservices.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * POST /api/payments/create-order
     * Body: { "bookingId": 5 }
     * Creates a Razorpay order and returns order details to frontend.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(paymentService.createOrder(body.get("bookingId")));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * POST /api/payments/verify
     * Body: { razorpayOrderId, razorpayPaymentId, razorpaySignature }
     * Verifies signature and marks booking as Paid.
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(paymentService.verifyPayment(
                    body.get("razorpayOrderId"),
                    body.get("razorpayPaymentId"),
                    body.get("razorpaySignature")
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/payments/status/{bookingId}
     * Returns payment status for a booking.
     */
    @GetMapping("/status/{bookingId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Integer bookingId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentStatus(bookingId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}