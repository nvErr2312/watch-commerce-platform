package com.fullstack.commonservice.bmad.nguoi3.event.product;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdatedEvent {

    private String productId;
    private String name;
    private String brand;
    private String category;
    private String description;
    private String imageUrl;
    private Instant updatedAt;
}
