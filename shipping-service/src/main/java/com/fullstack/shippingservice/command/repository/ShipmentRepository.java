package com.fullstack.shippingservice.command.repository;

import com.fullstack.shippingservice.command.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}
