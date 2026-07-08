package com.fullstack.commonservice.payment.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentCommand {
    private Long orderId;
}
