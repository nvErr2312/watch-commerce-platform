package com.fullstack.commonservice.bmad.nguoi3.event.inventory;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustedEvent {

    private String inventoryId;
    private String productId;
    private int oldStockQuantity;
    private int newStockQuantity;
    private Instant adjustedAt;
}
