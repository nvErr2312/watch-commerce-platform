package com.fullstack.productservice.query.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "product_view")
public class ProductView {

    @Id
    private String id;

    private String name;
    private String brand;
    private String category;

    @Column(length = 2000)
    private String description;

    private BigDecimal price;
    private String imageUrl;
    private boolean deleted;

    private Instant createdAt;
    private Instant updatedAt;

    @Version
    private Long version;
}
