package com.fullstack.adminservice.dto;

import java.util.List;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kept as two separate lists rather than joined server-side. Both productId
 * spaces now match (String/UUID, see InventoryItemDto Javadoc), so a
 * per-product stock join is possible - just not implemented here yet.
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
