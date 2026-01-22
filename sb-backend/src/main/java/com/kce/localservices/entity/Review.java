package com.kce.localservices.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    @JsonProperty("booking_id")
    private Integer bookingId;

    @Column(name = "service_id", nullable = false)
    @JsonProperty("service_id")
    private Integer serviceId;

    @Column(name = "customer_id", nullable = false)
    @JsonProperty("customer_id")
    private Integer customerId;

    @Column(name = "provider_id", nullable = false)
    @JsonProperty("provider_id")
    private Integer providerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
}
