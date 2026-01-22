package com.kce.localservices.repository;

import com.kce.localservices.entity.AdminAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAnalyticsRepository extends JpaRepository<AdminAnalytics, Integer> {
    AdminAnalytics findByMetricName(String metricName);
}
