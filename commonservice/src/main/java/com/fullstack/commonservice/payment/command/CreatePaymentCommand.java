package com.fullstack.commonservice.payment.command;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentCommand {
    private Long orderId;
    private BigDecimal amount;
}
