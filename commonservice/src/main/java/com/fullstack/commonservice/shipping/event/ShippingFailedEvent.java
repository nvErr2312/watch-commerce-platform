package com.fullstack.commonservice.shipping.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFailedEvent {
    private Long orderId;
    private String reason;
}
