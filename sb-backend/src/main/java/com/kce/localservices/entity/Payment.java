package com.kce.localservices.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the booking this payment is for
    @Column(name = "booking_id", nullable = false)
    private Integer bookingId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    // Amount in INR
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Razorpay order ID (from createOrder)
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    // Razorpay payment ID (from frontend after success)
    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    // Razorpay signature (for verification)
    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    // PENDING, SUCCESS, FAILED, REFUNDED
    @Column(nullable = false)
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;
}