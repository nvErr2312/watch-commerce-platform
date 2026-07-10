package com.fullstack.productservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePriceRequest {

    @NotNull(message = "Giá mới không được để trống")
    @PositiveOrZero(message = "Giá mới phải >= 0")
    private BigDecimal newPrice;
}
