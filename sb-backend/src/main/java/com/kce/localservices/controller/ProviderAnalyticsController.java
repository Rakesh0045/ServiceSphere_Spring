package com.kce.localservices.controller;

import com.kce.localservices.entity.User;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provider")
public class ProviderAnalyticsController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserService userService;

    /**
     * GET /api/provider/analytics
     * Returns monthly earnings (last 6 months) + booking status breakdown
     * for the currently authenticated provider.
     */
    @GetMapping("/analytics")
    public ResponseEntity<?> getProviderAnalytics() {
        try {
            User currentUser = userService.getCurrentUser();
            if (!"Service Provider".equals(currentUser.getRole())) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied."));
            }

            // Monthly earnings
            List<Map<String, Object>> monthlyEarnings = new ArrayList<>();
            for (Object[] row : bookingRepository.getProviderMonthlyEarnings(currentUser.getId())) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("month", row[0]);       // "Mar 2026"
                entry.put("monthKey", row[1]);    // "2026-03"
                entry.put("earnings", row[2]);
                entry.put("bookings", row[3]);
                monthlyEarnings.add(entry);
            }

            // Booking status breakdown
            Map<String, Long> statusBreakdown = new HashMap<>();
            for (Object[] row : bookingRepository.getProviderBookingStatusSummary(currentUser.getId())) {
                statusBreakdown.put((String) row[0], ((Number) row[1]).longValue());
            }

            return ResponseEntity.ok(Map.of(
                "monthlyEarnings", monthlyEarnings,
                "statusBreakdown", statusBreakdown
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }
}