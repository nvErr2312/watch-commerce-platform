package com.fullstack.commonservice.payment.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    private Long orderId;
    private String paymentId;
    private BigDecimal amount;
    private String paymentUrl;
}
