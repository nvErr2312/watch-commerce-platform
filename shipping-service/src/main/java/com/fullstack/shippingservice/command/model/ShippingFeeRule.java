package com.fullstack.shippingservice.command.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shipping_fee_rules")
public class ShippingFeeRule {
    @Id
    private String regionCode;

    @Column(nullable = false)
    private String regionName;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(name = "remote_fee")
    private BigDecimal remoteFee;
}
