package com.kce.localservices.event;

import com.kce.localservices.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener that updates admin analytics when domain events occur
 */
@Component
public class AnalyticsEventListener {

    @Autowired
    private AdminService adminService;

    /**
     * Handle analytics events asynchronously to avoid blocking main operations
     */
    @Async
    @EventListener
    public void handleAnalyticsEvent(AnalyticsEvent event) {
        try {
            adminService.incrementMetric(event.getMetricName(), event.getDelta());
        } catch (Exception e) {
            // Log error but don't fail the main operation
            System.err.println(
                    "Failed to update analytics for metric: " + event.getMetricName() + " - " + e.getMessage());
        }
    }
}
