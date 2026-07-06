package com.fullstack.inventoryservice.query.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "inventory_view")
public class InventoryView {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String productId;

    private int stockQuantity;
    private int reservedQuantity;

    private Instant updatedAt;

    @Version
    private Long version;
}
