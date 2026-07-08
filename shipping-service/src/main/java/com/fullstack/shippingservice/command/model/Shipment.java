package com.fullstack.shippingservice.command.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shipments")
public class Shipment {
    @Id
    private Long orderId;

    private String trackingCode;

    @Column(nullable = false)
    private String status;

    private Instant createdAt;
}
