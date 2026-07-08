package com.fullstack.commonservice.bmad.nguoi3.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors inventory-service's InventoryItem entity (Command side, owned by
 * Nguoi 4 for the checkout Saga: reserve/release against Postgres, seeded by
 * Flyway with mock productId 10/11/12).
 *
 * IMPORTANT - known cross-team gap: this productId (Long) is NOT the same
 * identity as Product Service's productId (String/UUID, owned by Nguoi 3's
 * ProductAggregate). The two catalogs are currently unrelated - Inventory's
 * mock items don't correspond to any real Product Service product. Admin
 * Service's dashboard therefore cannot join Product + Inventory by id until
 * the team agrees on a single product identity scheme.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto {

    private Long productId;
    private int availableQuantity;
}
