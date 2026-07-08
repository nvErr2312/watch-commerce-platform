package com.fullstack.commonservice.order.command;

import com.fullstack.commonservice.order.OrderItemPayload;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private Long userId;
    private List<OrderItemPayload> items;
    // ponytail: this is the order subtotal until shipping fee is calculated.
    private BigDecimal totalAmount;
    private String shippingAddress;
}
