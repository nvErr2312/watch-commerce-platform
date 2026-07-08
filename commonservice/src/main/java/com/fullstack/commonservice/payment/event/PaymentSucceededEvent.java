package com.fullstack.commonservice.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceededEvent {
    private Long orderId;
    private String paymentId;
}
