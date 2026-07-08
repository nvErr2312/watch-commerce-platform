package com.fullstack.commonservice.shipping.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingCreatedEvent {
    private Long orderId;
    private String trackingCode;
}
