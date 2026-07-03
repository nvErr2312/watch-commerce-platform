package com.fullstack.commonservice.bmad.nguoi3.event.product;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {

    private String productId;
    private String name;
    private String brand;
    private String category;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Instant createdAt;
}
