package com.fullstack.paymentservice.command.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    @Id
    private String code;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    private boolean active;
}
