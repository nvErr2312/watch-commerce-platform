package com.fullstack.commonservice.shipping.command;

import com.fullstack.commonservice.order.OrderItemPayload;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateShippingFeeCommand {
    private Long orderId;
    private List<OrderItemPayload> items;
    private BigDecimal subtotalAmount;
    private String shippingAddress;
}
