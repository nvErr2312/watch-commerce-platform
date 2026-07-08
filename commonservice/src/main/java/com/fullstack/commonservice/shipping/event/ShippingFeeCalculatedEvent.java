package com.fullstack.commonservice.shipping.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeCalculatedEvent {
    private Long orderId;
    private BigDecimal subtotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
}
