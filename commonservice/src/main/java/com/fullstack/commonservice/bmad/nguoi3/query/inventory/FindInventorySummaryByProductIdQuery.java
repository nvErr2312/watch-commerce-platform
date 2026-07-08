package com.fullstack.commonservice.bmad.nguoi3.query.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Distinct from {@link FindInventoryByProductIdQuery} on purpose - see
 * FindAllProductSummariesQuery for why two handlers on the same query type
 * with different response types is unsafe with Axon's query bus.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindInventorySummaryByProductIdQuery {

    private String productId;
}
