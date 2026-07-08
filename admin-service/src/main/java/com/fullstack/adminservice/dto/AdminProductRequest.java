package com.fullstack.adminservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared by both create and update - price is validated as PositiveOrZero
 * when present, but NOT @NotNull, because update() never reads price
 * (only /price does, via a separate raw BigDecimal body) and must not
 * require callers to resend it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String brand;
    private String category;
    private String description;

    @PositiveOrZero(message = "Giá sản phẩm phải >= 0")
    private BigDecimal price;

    private String imageUrl;
}
