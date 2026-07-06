package com.fullstack.inventoryservice.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustStockRequest {

    @PositiveOrZero(message = "Số lượng tồn kho mới phải >= 0")
    private int newStockQuantity;
}
