package com.fullstack.inventoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

    @NotBlank(message = "productId không được để trống")
    private String productId;

    @PositiveOrZero(message = "Số lượng ban đầu phải >= 0")
    private int initialQuantity;
}
