package com.fullstack.commonservice.inventory.event;

import com.fullstack.commonservice.order.OrderItemPayload;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent {
    private Long orderId;
    private List<OrderItemPayload> items;
    private BigDecimal subtotalAmount;
    private String shippingAddress;
}
