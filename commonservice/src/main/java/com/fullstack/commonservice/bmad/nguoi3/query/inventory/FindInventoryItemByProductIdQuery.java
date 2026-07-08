package com.fullstack.commonservice.bmad.nguoi3.query.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * productId is Long to match InventoryItem (inventory-service's Command-side
 * entity, owned by Nguoi 4 for the checkout Saga) - NOT the same identity
 * space as Product Service's String/UUID productId. See dto/inventory
 * package Javadoc for the full explanation of this mismatch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindInventoryItemByProductIdQuery {

    private Long productId;
}
