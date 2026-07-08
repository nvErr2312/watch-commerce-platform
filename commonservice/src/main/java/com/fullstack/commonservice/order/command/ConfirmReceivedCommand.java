package com.fullstack.commonservice.order.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmReceivedCommand {
    @TargetAggregateIdentifier
    private Long orderId;
}
