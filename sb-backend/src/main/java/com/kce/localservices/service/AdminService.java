package com.kce.localservices.service;

import com.kce.localservices.entity.AdminAnalytics;
import com.kce.localservices.repository.AdminAnalyticsRepository;
import com.kce.localservices.repository.BookingRepository;
import com.kce.localservices.repository.ServiceRepository;
import com.kce.localservices.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AdminAnalyticsRepository analyticsRepository;

    /**
     * Initialize analytics on application startup
     */
    @PostConstruct
    public void initializeAnalytics() {
        // Check if analytics are already initialized
        AdminAnalytics totalUsers = analyticsRepository.findByMetricName("total_users");
        if (totalUsers == null) {
            System.out.println("Initializing admin analytics for the first time...");
            refreshAllAnalytics();
        } else {
            System.out.println("Admin analytics already initialized. Last updated: " + totalUsers.getLastUpdated());
        }
    }

    /**
     * Get comprehensive statistics for admin dashboard
     * Includes metrics, revenue, breakdowns, and recent activities
     */
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();

        // Core metrics from admin_analytics table
        Map<String, Long> stats = new HashMap<>();
        stats.put("total_users", getMetricValue("total_users"));
        stats.put("total_providers", getMetricValue("total_providers"));
        stats.put("total_services", getMetricValue("total_services"));
        stats.put("total_bookings", getMetricValue("total_bookings"));
        stats.put("completed_bookings", getMetricValue("completed_bookings"));
        stats.put("pending_bookings", getMetricValue("pending_bookings"));
        stats.put("cancelled_bookings", getMetricValue("cancelled_bookings"));
        result.put("stats", stats);

        // Revenue analytics
        Map<String, Object> revenue = new HashMap<>();
        Double totalRevenue = bookingRepository.getTotalRevenue();
        Double avgBookingValue = bookingRepository.getAverageBookingValue();
        Double pendingRevenue = bookingRepository.getPendingRevenue();

        revenue.put("total_revenue", totalRevenue != null ? totalRevenue : 0.0);
        revenue.put("average_booking_value", avgBookingValue != null ? avgBookingValue : 0.0);
        revenue.put("pending_revenue", pendingRevenue != null ? pendingRevenue : 0.0);
        result.put("revenue", revenue);

        // Booking status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        statusBreakdown.put("Pending", bookingRepository.countByStatus("Pending"));
        statusBreakdown.put("Confirmed", bookingRepository.countByStatus("Confirmed"));
        statusBreakdown.put("Completed", bookingRepository.countByStatus("Completed"));
        statusBreakdown.put("Cancelled", bookingRepository.countByStatus("Cancelled"));
        result.put("bookingStatusBreakdown", statusBreakdown);

        // Top categories by booking count
        List<Map<String, Object>> topCategories = serviceRepository.findTopCategoriesByBookingCount()
                .stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> category = new HashMap<>();
                    category.put("category", row[0]);
                    category.put("bookingCount", row[1]);
                    return category;
                })
                .toList();
        result.put("topCategories", topCategories);

        // Top services by booking count
        List<Map<String, Object>> topServices = serviceRepository.findTopServicesByBookingCount()
                .stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> service = new HashMap<>();
                    service.put("serviceId", row[0]);
                    service.put("serviceName", row[1]);
                    service.put("category", row[2]);
                    service.put("bookingCount", row[3]);
                    return service;
                })
                .toList();
        result.put("topServices", topServices);

        // Top providers by earnings
        List<Map<String, Object>> topProviders = bookingRepository.getTopProvidersByEarnings()
                .stream()
                .limit(5)
                .map(row -> {
                    Map<String, Object> provider = new HashMap<>();
                    provider.put("providerId", row[0]);
                    provider.put("providerName", row[1]);
                    provider.put("totalEarnings", row[2]);
                    provider.put("completedBookings", row[3]);
                    return provider;
                })
                .toList();
        result.put("topProviders", topProviders);

        // Recent activities (last 10 bookings)
        List<Map<String, Object>> recentActivities = bookingRepository.getRecentActivities()
                .stream()
                .limit(10)
                .map(row -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("bookingId", row[0]);
                    activity.put("customerName", row[1]);
                    activity.put("serviceName", row[2]);
                    activity.put("status", row[3]);
                    activity.put("bookingTime", row[4]);
                    activity.put("price", row[5]);
                    activity.put("providerName", row[6]);
                    return activity;
                })
                .toList();
        result.put("recentActivities", recentActivities);

        return result;
    }

    /**
     * Get a metric value from admin_analytics table
     * Returns 0 if metric doesn't exist
     */
    private Long getMetricValue(String metricName) {
        AdminAnalytics metric = analyticsRepository.findByMetricName(metricName);
        return metric != null ? metric.getMetricValue().longValue() : 0L;
    }

    /**
     * Update or create a metric in admin_analytics table
     */
    @Transactional
    public void updateMetric(String metricName, Integer value) {
        AdminAnalytics metric = analyticsRepository.findByMetricName(metricName);
        if (metric == null) {
            metric = new AdminAnalytics();
            metric.setMetricName(metricName);
            metric.setMetricValue(value);
        } else {
            metric.setMetricValue(value);
        }
        analyticsRepository.save(metric);
    }

    /**
     * Increment a metric by a delta value
     */
    @Transactional
    public void incrementMetric(String metricName, Integer delta) {
        AdminAnalytics metric = analyticsRepository.findByMetricName(metricName);
        if (metric == null) {
            // If metric doesn't exist, refresh all analytics to initialize it
            refreshAllAnalytics();
        } else {
            metric.setMetricValue(metric.getMetricValue() + delta);
            analyticsRepository.save(metric);
        }
    }

    /**
     * Refresh all analytics by recalculating from database
     * This is called on initialization and periodically via scheduled task
     */
    @Transactional
    public void refreshAllAnalytics() {
        System.out.println("Refreshing all admin analytics...");

        long totalUsers = userRepository.count();
        long totalProviders = userRepository.countByRole("Service Provider");
        long totalServices = serviceRepository.count();
        long totalBookings = bookingRepository.count();
        long completedBookings = bookingRepository.countByStatus("Completed");
        long pendingBookings = bookingRepository.countByStatus("Pending");
        long cancelledBookings = bookingRepository.countByStatus("Cancelled");

        updateMetric("total_users", (int) totalUsers);
        updateMetric("total_providers", (int) totalProviders);
        updateMetric("total_services", (int) totalServices);
        updateMetric("total_bookings", (int) totalBookings);
        updateMetric("completed_bookings", (int) completedBookings);
        updateMetric("pending_bookings", (int) pendingBookings);
        updateMetric("cancelled_bookings", (int) cancelledBookings);

        System.out.println("Admin analytics refreshed successfully.");
    }

    /**
     * Scheduled task to refresh analytics every hour
     * Runs at the top of every hour (0 minutes, 0 seconds)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledAnalyticsRefresh() {
        System.out.println("Running scheduled analytics refresh...");
        refreshAllAnalytics();
    }
}
