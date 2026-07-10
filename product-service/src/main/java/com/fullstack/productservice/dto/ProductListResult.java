package com.fullstack.productservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wraps a list response so Axon's QueryGateway can use
 * ResponseTypes.instanceOf(...) instead of multipleInstancesOf(...).
 * multipleInstancesOf consistently failed to convert distributed query
 * responses ("Retrieved response [ArrayList] is not convertible to a List
 * of the expected response type") - a known Axon/Jackson gap where the
 * serialized list loses per-element type info. Wrapping the list in a single
 * object sidesteps that entirely, since single-instance conversion works.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResult {

    private List<ProductResponse> products;
}
