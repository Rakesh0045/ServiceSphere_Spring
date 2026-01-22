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
// import org.springframework.stereotype.Service;
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

    @Transactional
    public void createBooking(Booking booking) {
        User currentUser = userService.getCurrentUser();
        booking.setCustomerId(currentUser.getId());
        booking.setStatus("Pending");

        // Calculate end time (1 hour duration as per server.js)
        Date startTime = booking.getBookingStartTime();
        Date endTime = new Date(startTime.getTime() + 60 * 60 * 1000); // 1 hour
        booking.setBookingEndTime(endTime);

        // Check availability
        // Logic: Check if any booking exists for provider at this start time with
        // status Pending or Confirmed
        List<Booking> existing = bookingRepository.findByProviderIdAndBookingStartTimeAndStatusIn(
                booking.getProviderId(),
                startTime,
                Arrays.asList("Pending", "Confirmed"));

        if (!existing.isEmpty()) {
            throw new RuntimeException("This time slot is no longer available. Please select another.");
        }

        bookingRepository.save(booking);

        // Publish analytics events for new booking
        eventPublisher.publishEvent(new AnalyticsEvent(this, "total_bookings", 1));
        eventPublisher.publishEvent(new AnalyticsEvent(this, "pending_bookings", 1));
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

        // Map to response format
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

            // Fetch review data if exists
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

    public void updateBookingStatus(Integer bookingId, String status) {
        User currentUser = userService.getCurrentUser();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getProviderId().equals(currentUser.getId())) {
            throw new RuntimeException("You do not have permission.");
        }

        String oldStatus = booking.getStatus();
        booking.setStatus(status);
        bookingRepository.save(booking);

        // Publish analytics events for status changes
        if (!oldStatus.equals(status)) {
            // Decrement old status count
            if ("Pending".equals(oldStatus)) {
                eventPublisher.publishEvent(new AnalyticsEvent(this, "pending_bookings", -1));
            } else if ("Cancelled".equals(oldStatus)) {
                eventPublisher.publishEvent(new AnalyticsEvent(this, "cancelled_bookings", -1));
            } else if ("Completed".equals(oldStatus)) {
                eventPublisher.publishEvent(new AnalyticsEvent(this, "completed_bookings", -1));
            }

            // Increment new status count
            if ("Pending".equals(status)) {
                eventPublisher.publishEvent(new AnalyticsEvent(this, "pending_bookings", 1));
            } else if ("Cancelled".equals(status)) {
                eventPublisher.publishEvent(new AnalyticsEvent(this, "cancelled_bookings", 1));
            } else if ("Completed".equals(status)) {
                eventPublisher.publishEvent(new AnalyticsEvent(this, "completed_bookings", 1));
            }
        }
    }
}
