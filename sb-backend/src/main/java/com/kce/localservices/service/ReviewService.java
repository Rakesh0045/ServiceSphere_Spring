package com.kce.localservices.service;

import com.kce.localservices.entity.Booking;
import com.kce.localservices.entity.Review;
import com.kce.localservices.entity.Service;
import com.kce.localservices.entity.User;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.repository.ReviewRepository;
import com.kce.localservices.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@org.springframework.stereotype.Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void createReview(Review review) {
        User currentUser = userService.getCurrentUser();
        if (!"Customer".equals(currentUser.getRole())) {
            throw new RuntimeException("Only customers can leave reviews.");
        }

        review.setCustomerId(currentUser.getId());

        Booking booking = bookingRepository.findById(review.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getCustomerId().equals(currentUser.getId()) || !"Completed".equals(booking.getStatus())) {
            throw new RuntimeException("You can only review your own completed services.");
        }

        // Check duplicate (DB unique constraint also handles this)
        if (reviewRepository.findByBookingId(review.getBookingId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this booking.");
        }

        reviewRepository.save(review);

        // --- Update avg_rating and total_reviews on the Service ---
        Service service = serviceRepository.findById(review.getServiceId()).orElse(null);
        if (service != null) {
            Double avgRating = reviewRepository.getAverageRating(review.getServiceId());
            Integer reviewCount = reviewRepository.countByServiceId(review.getServiceId());
            service.setAvgRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            service.setTotalReviews(reviewCount != null ? reviewCount : 0);
            serviceRepository.save(service);
        }

        // --- Notify the provider about the new review ---
        String serviceName = service != null ? service.getServiceName() : "a service";
        notificationService.createNotification(
                booking.getProviderId(),
                "New Review Received ⭐",
                currentUser.getName() + " left a " + review.getRating() + "-star review for \"" + serviceName + "\".",
                "REVIEW_RECEIVED",
                review.getServiceId()
        );
    }
}