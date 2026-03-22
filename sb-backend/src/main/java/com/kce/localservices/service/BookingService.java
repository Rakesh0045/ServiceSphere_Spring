package com.kce.localservices.service;

import com.kce.localservices.entity.Booking;
import com.kce.localservices.entity.Review;
import com.kce.localservices.entity.Service;
import com.kce.localservices.entity.User;
import com.kce.localservices.event.AnalyticsEvent;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.repository.ReviewRepository;
import com.kce.localservices.repository.ServiceRepository;
import com.kce.localservices.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void createBooking(Booking booking) {
        User currentUser = userService.getCurrentUser();
        booking.setCustomerId(currentUser.getId());
        booking.setStatus("Pending");

        // Calculate end time (1 hour duration)
        Date startTime = booking.getBookingStartTime();
        Date endTime = new Date(startTime.getTime() + 60 * 60 * 1000);
        booking.setBookingEndTime(endTime);

        // Check availability
        List<Booking> existing = bookingRepository.findByProviderIdAndBookingStartTimeAndStatusIn(
                booking.getProviderId(),
                startTime,
                Arrays.asList("Pending", "Confirmed"));

        if (!existing.isEmpty()) {
            throw new RuntimeException("This time slot is no longer available. Please select another.");
        }

        bookingRepository.save(booking);

        // Analytics events
        eventPublisher.publishEvent(new AnalyticsEvent(this, "total_bookings", 1));
        eventPublisher.publishEvent(new AnalyticsEvent(this, "pending_bookings", 1));

        // Fetch service name for notification
        String serviceName = serviceRepository.findById(booking.getServiceId())
                .map(Service::getServiceName).orElse("a service");

        // Notify the provider about the new booking request
        notificationService.createNotification(
                booking.getProviderId(),
                "New Booking Request",
                currentUser.getName() + " has requested to book \"" + serviceName + "\".",
                "BOOKING_CREATED",
                booking.getId()
        );

        // Confirm receipt to the customer
        notificationService.createNotification(
                currentUser.getId(),
                "Booking Submitted",
                "Your booking request for \"" + serviceName + "\" has been sent. Awaiting provider confirmation.",
                "BOOKING_CREATED",
                booking.getId()
        );
    }

    public List<Map<String, Object>> getBookings() {
        User currentUser = userService.getCurrentUser();
        List<Booking> bookings;

        if ("Customer".equals(currentUser.getRole())) {
            bookings = bookingRepository.findByCustomerIdOrderByCreatedAtDesc(currentUser.getId());
        } else if ("Service Provider".equals(currentUser.getRole())) {
            bookings = bookingRepository.findByProviderIdOrderByCreatedAtDesc(currentUser.getId());
        } else {
            throw new RuntimeException("Unauthorized role.");
        }

        return bookings.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("status", b.getStatus());
            map.put("booking_start_time", b.getBookingStartTime());

            Service service = serviceRepository.findById(b.getServiceId()).orElse(null);
            if (service != null) {
                map.put("service_id", service.getId());
                map.put("service_name", service.getServiceName());
                map.put("price", service.getPrice());
            }

            User customer = userRepository.findById(b.getCustomerId()).orElse(null);
            if (customer != null) {
                map.put("customer_name", customer.getName());
            }

            User provider = userRepository.findById(b.getProviderId()).orElse(null);
            if (provider != null) {
                map.put("provider_id", provider.getId());
                map.put("provider_name", provider.getName());
            }

            Review review = reviewRepository.findByBookingId(b.getId()).orElse(null);
            if (review != null) {
                map.put("review_id", review.getId());
                map.put("rating", review.getRating());
                map.put("comment", review.getComment());
            } else {
                map.put("review_id", null);
                map.put("rating", null);
                map.put("comment", null);
            }

            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateBookingStatus(Integer bookingId, String status) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        boolean isProvider = booking.getProviderId().equals(currentUser.getId())
                && "Service Provider".equals(currentUser.getRole());
        boolean isCustomerCancelling = booking.getCustomerId().equals(currentUser.getId())
                && "Customer".equals(currentUser.getRole())
                && "Cancelled".equals(status)
                && "Pending".equals(booking.getStatus()); // customers can only cancel pending bookings

        if (!isProvider && !isCustomerCancelling) {
            throw new RuntimeException("You do not have permission to perform this action.");
        }

        String oldStatus = booking.getStatus();
        booking.setStatus(status);
        bookingRepository.save(booking);

        // Analytics events
        if (!oldStatus.equals(status)) {
            if ("Pending".equals(oldStatus)) eventPublisher.publishEvent(new AnalyticsEvent(this, "pending_bookings", -1));
            else if ("Cancelled".equals(oldStatus)) eventPublisher.publishEvent(new AnalyticsEvent(this, "cancelled_bookings", -1));
            else if ("Completed".equals(oldStatus)) eventPublisher.publishEvent(new AnalyticsEvent(this, "completed_bookings", -1));

            if ("Pending".equals(status)) eventPublisher.publishEvent(new AnalyticsEvent(this, "pending_bookings", 1));
            else if ("Cancelled".equals(status)) eventPublisher.publishEvent(new AnalyticsEvent(this, "cancelled_bookings", 1));
            else if ("Completed".equals(status)) eventPublisher.publishEvent(new AnalyticsEvent(this, "completed_bookings", 1));
        }

        // Fetch service name for notifications
        String serviceName = serviceRepository.findById(booking.getServiceId())
                .map(Service::getServiceName).orElse("a service");

        // Send notifications based on the new status
        switch (status) {
            case "Confirmed" -> {
                // Notify customer
                notificationService.createNotification(
                        booking.getCustomerId(),
                        "Booking Confirmed! 🎉",
                        "Your booking for \"" + serviceName + "\" has been confirmed by the provider.",
                        "BOOKING_CONFIRMED",
                        bookingId
                );
            }
            case "Completed" -> {
                // Notify customer to leave a review
                notificationService.createNotification(
                        booking.getCustomerId(),
                        "Service Completed",
                        "Your booking for \"" + serviceName + "\" is marked complete. Share your experience!",
                        "BOOKING_COMPLETED",
                        bookingId
                );
            }
            case "Cancelled" -> {
                // Notify the other party
                if (isProvider) {
                    // Provider cancelled → notify customer
                    notificationService.createNotification(
                            booking.getCustomerId(),
                            "Booking Cancelled",
                            "Unfortunately, your booking for \"" + serviceName + "\" was cancelled by the provider.",
                            "BOOKING_CANCELLED",
                            bookingId
                    );
                } else {
                    // Customer cancelled → notify provider
                    User customer = userRepository.findById(booking.getCustomerId()).orElse(null);
                    String customerName = customer != null ? customer.getName() : "A customer";
                    notificationService.createNotification(
                            booking.getProviderId(),
                            "Booking Cancelled",
                            customerName + " cancelled their booking for \"" + serviceName + "\".",
                            "BOOKING_CANCELLED",
                            bookingId
                    );
                }
            }
        }
    }
}