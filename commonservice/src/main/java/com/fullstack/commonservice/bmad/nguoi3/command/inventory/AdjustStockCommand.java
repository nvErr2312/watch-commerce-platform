package com.fullstack.commonservice.bmad.nguoi3.command.inventory;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustStockCommand {

    @TargetAggregateIdentifier
    private String inventoryId;

    private int newStockQuantity;
}
