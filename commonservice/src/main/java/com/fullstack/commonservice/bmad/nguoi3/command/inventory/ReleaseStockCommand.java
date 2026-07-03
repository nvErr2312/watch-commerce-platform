package com.fullstack.commonservice.bmad.nguoi3.command.inventory;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStockCommand {

    @TargetAggregateIdentifier
    private String inventoryId;

    private String orderId;
    private int quantity;
}
