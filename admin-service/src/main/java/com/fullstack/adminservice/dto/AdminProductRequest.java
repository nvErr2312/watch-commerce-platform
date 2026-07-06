package com.fullstack.adminservice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductRequest {

    private String name;
    private String brand;
    private String category;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}
