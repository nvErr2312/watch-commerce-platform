package com.fullstack.commonservice.bmad.nguoi3.query.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Distinct from {@link FindAllProductsQuery} on purpose: Axon dispatches by
 * (query type, response type), and registering two handlers for the SAME
 * query class with different response types (ProductView vs ProductSummaryDto)
 * turned out to be ambiguous in practice - the query bus does not reliably
 * disambiguate generic List<T> response types. A separate query type used
 * only by Admin Service avoids that ambiguity entirely.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FindAllProductSummariesQuery {

    private int page;
    private int size;
}
