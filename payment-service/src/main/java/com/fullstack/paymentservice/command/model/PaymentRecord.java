package com.fullstack.paymentservice.command.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_records")
public class PaymentRecord {
    @Id
    private String paymentId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentUrl;

    @Column(nullable = false)
    private String status;

    private Instant createdAt;

    private Instant expiresAt;
}
