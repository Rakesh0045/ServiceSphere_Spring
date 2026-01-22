package com.kce.localservices.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "admin_analytics")
public class AdminAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "metric_name", nullable = false, unique = true)
    private String metricName;

    @Column(name = "metric_value", nullable = false)
    private Integer metricValue;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private Timestamp lastUpdated;
}
