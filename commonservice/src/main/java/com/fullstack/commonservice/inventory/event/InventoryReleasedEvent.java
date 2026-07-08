package com.fullstack.commonservice.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReleasedEvent {
    private Long orderId;
}
