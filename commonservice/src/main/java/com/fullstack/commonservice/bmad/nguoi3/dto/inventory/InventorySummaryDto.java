package com.fullstack.commonservice.bmad.nguoi3.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cross-service query response shared between Inventory Service (query handler)
 * and Admin Service (query gateway caller) - kept separate from the JPA
 * InventoryView entity so Axon's distributed query bus does not need the
 * persistence class on Admin Service's classpath.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryDto {

    private String inventoryId;
    private String productId;
    private int stockQuantity;
    private int reservedQuantity;
    private int availableQuantity;
}
