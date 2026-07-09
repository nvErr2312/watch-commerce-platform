package com.fullstack.commonservice.bmad.nguoi3.dto.inventory;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wraps a list response so Axon's QueryGateway can use ResponseTypes.instanceOf(...)
 * instead of multipleInstancesOf(...) - see ProductListResult (product-service)
 * for why: multipleInstancesOf consistently failed to convert distributed list
 * responses back into their element type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemListResult {

    private List<InventoryItemDto> items;
}
