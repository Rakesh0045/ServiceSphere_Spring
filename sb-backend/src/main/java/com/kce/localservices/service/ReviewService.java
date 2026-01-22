package com.kce.localservices.service;

import com.kce.localservices.entity.Booking;
import com.kce.localservices.entity.Review;
import com.kce.localservices.entity.User;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

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

        // Check duplicate? DB unique constraint handles it, but good to check.
        // Assuming unique constraint on booking_id in DB schema.

        reviewRepository.save(review);
    }
}
