package com.fullstack.commonservice.order.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderTotalUpdatedEvent {
    private Long orderId;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String status;
}
