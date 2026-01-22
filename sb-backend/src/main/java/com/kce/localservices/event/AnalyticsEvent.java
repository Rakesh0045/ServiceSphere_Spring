package com.kce.localservices.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Base event class for analytics-related events
 */
@Getter
public class AnalyticsEvent extends ApplicationEvent {
    private final String metricName;
    private final Integer delta; // Change in value (+1, -1, etc.)

    public AnalyticsEvent(Object source, String metricName, Integer delta) {
        super(source);
        this.metricName = metricName;
        this.delta = delta;
    }
}
