package com.fullstack.commonservice.bmad.nguoi3.dto.product;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cross-service query response shared between Product Service (query handler)
 * and Admin Service (query gateway caller) - kept separate from the JPA
 * ProductView entity so Axon's distributed query bus does not need the
 * persistence class on Admin Service's classpath.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDto {

    private String productId;
    private String name;
    private String brand;
    private String category;
    private BigDecimal price;
    private String imageUrl;
}
