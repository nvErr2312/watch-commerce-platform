package com.fullstack.adminservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin-only presentation composition (Product + Inventory joined for the
 * dashboard) - not a cross-service message, so it stays local to Admin
 * Service rather than in commonservice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryView {

    private String productId;
    private String name;
    private String brand;
    private String category;
    private BigDecimal price;
    private String imageUrl;
    private int stockQuantity;
    private int reservedQuantity;
    private int availableQuantity;
}
