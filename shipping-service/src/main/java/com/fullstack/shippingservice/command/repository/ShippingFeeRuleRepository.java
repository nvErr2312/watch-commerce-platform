package com.fullstack.shippingservice.command.repository;

import com.fullstack.shippingservice.command.model.ShippingFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingFeeRuleRepository extends JpaRepository<ShippingFeeRule, String> {
}
