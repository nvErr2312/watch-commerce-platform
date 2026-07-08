package com.fullstack.commonservice.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelRequestedEvent {
    private Long orderId;
    private String reason;
}
