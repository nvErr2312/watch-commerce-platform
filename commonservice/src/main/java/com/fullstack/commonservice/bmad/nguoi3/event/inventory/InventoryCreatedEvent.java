package com.fullstack.commonservice.bmad.nguoi3.event.inventory;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreatedEvent {

    private String inventoryId;
    private String productId;
    private int initialQuantity;
    private Instant createdAt;
}
