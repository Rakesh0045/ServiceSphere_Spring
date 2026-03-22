package com.kce.localservices.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private Boolean isRead = false;

    // Type: BOOKING_CREATED, BOOKING_CONFIRMED, BOOKING_COMPLETED, BOOKING_CANCELLED, REVIEW_RECEIVED
    @Column(name = "notification_type")
    private String notificationType;

    // Optional link to relevant entity
    @Column(name = "reference_id")
    private Integer referenceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
}
