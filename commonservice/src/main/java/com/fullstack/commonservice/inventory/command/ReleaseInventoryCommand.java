package com.fullstack.commonservice.inventory.command;

import com.fullstack.commonservice.order.OrderItemPayload;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryCommand {
    private Long orderId;
    private List<OrderItemPayload> items;
}
