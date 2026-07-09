package com.fullstack.commonservice.bmad.nguoi3.command.product;

import java.math.BigDecimal;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePriceCommand {

    @TargetAggregateIdentifier
    private String productId;

    private BigDecimal newPrice;
}
