package com.fullstack.productservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String brand;
    private String category;
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá sản phẩm phải >= 0")
    private BigDecimal price;

    private String imageUrl;
}
