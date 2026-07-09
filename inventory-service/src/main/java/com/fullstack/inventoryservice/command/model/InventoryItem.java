package com.fullstack.inventoryservice.command.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    private String productId;

    @Column(nullable = false)
    private int availableQuantity;
}
