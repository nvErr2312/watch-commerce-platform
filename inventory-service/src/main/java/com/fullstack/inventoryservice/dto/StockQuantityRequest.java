package com.fullstack.inventoryservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockQuantityRequest {

    private String orderId;

    @Positive(message = "Số lượng phải > 0")
    private int quantity;
}
