package com.fullstack.productservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Plain response DTO for list-returning queries. Axon's ResponseTypes.multipleInstancesOf(...)
 * failed to convert distributed query responses back into List<ProductView> (a JPA entity),
 * even though the equivalent instanceOf(ProductView.class) single-item query worked fine.
 * Returning a plain POJO instead avoids whatever JPA/Jackson interaction was breaking that
 * conversion, and also stops leaking the persistence entity over the query bus.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private String brand;
    private String category;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}
