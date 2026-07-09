package com.fullstack.commonservice.bmad.nguoi3.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors inventory-service's InventoryItem entity (Command side, owned by
 * Nguoi 4 for the checkout Saga: reserve/release against Postgres).
 *
 * productId is now String/UUID, matching Product Service's ProductAggregate
 * identity - the previous Long/String cross-team gap has been resolved, so
 * Inventory rows correspond 1:1 with real Product Service products.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto {

    private String productId;
    private int availableQuantity;
}
