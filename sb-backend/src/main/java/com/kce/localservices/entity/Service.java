package com.kce.localservices.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "service_name", nullable = false)
    @JsonProperty("service_name")
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "provider_id", nullable = false)
    @JsonProperty("provider_id")
    private Integer providerId;

    @Column(name = "image_url")
    @JsonProperty("image_url")
    private String imageUrl;

    private String location;

    private String availability;

    private String status = "Pending";

    @Column(name = "avg_rating", precision = 3, scale = 2)
    @JsonProperty("avg_rating")
    private BigDecimal avgRating;

    @Column(name = "total_reviews")
    @JsonProperty("total_reviews")
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
}
