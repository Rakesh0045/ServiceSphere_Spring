package com.kce.localservices.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "service_id", nullable = false)
    @JsonProperty("service_id")
    private Integer serviceId;

    @Column(name = "customer_id", nullable = false)
    @JsonProperty("customer_id")
    private Integer customerId;

    @Column(name = "provider_id", nullable = false)
    @JsonProperty("provider_id")
    private Integer providerId;

    @Column(name = "booking_start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("booking_start_time")
    private Date bookingStartTime;

    @Column(name = "booking_end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty("booking_end_time")
    private Date bookingEndTime;

    private String status = "Pending";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    // Optional: Relationships for easier fetching if needed later
    // @ManyToOne
    // @JoinColumn(name = "service_id", insertable = false, updatable = false)
    // private Service service;
}
