package com.fullstack.adminservice.dto;

import java.util.List;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kept as two separate lists rather than joined - Product Service's productId
 * (String/UUID) and Inventory Service's productId (Long, checkout-reservation
 * mock catalog) are not the same identity space yet, so a per-product
 * stock join would be fabricated data. See InventoryItemDto Javadoc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private int totalProducts;
    private List<ProductSummaryDto> products;
    private int totalInventoryItems;
    private List<InventoryItemDto> inventoryItems;
}
