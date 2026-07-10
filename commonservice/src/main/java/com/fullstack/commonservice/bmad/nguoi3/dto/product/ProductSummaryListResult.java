package com.fullstack.commonservice.bmad.nguoi3.dto.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wraps a list response so Axon's QueryGateway can use
 * ResponseTypes.instanceOf(...) instead of multipleInstancesOf(...), which
 * consistently failed to convert distributed list responses back into their
 * element type. See ProductListResult (product-service) for the full story.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryListResult {

    private List<ProductSummaryDto> products;
}
