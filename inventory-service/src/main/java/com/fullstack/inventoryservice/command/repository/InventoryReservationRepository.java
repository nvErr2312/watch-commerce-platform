package com.fullstack.inventoryservice.command.repository;

import com.fullstack.inventoryservice.command.model.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
}
