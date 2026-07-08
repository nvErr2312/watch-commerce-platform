package com.fullstack.shippingservice.command.repository;

import com.fullstack.shippingservice.command.model.ShippingFeeCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingFeeCalculationRepository extends JpaRepository<ShippingFeeCalculation, Long> {
}
