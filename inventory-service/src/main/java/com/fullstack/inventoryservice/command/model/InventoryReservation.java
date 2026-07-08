package com.fullstack.inventoryservice.command.model;

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
@Table(name = "inventory_reservations")
public class InventoryReservation {
    @Id
    private Long orderId;

    @Column(nullable = false)
    private String status;

    private Instant updatedAt;
}
