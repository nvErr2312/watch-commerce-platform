package com.fullstack.commonservice.order.command;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderTotalCommand {
    @TargetAggregateIdentifier
    private Long orderId;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
}
