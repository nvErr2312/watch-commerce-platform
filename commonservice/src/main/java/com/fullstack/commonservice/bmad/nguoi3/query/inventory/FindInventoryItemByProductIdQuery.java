package com.fullstack.commonservice.bmad.nguoi3.query.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * productId is String/UUID, matching Product Service's identity space -
 * the Long/String mismatch between Product and Inventory has been resolved
 * team-wide; Inventory Service's Command side now uses the same UUIDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindInventoryItemByProductIdQuery {

    private String productId;
}
