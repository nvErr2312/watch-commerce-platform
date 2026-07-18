package com.fullstack.commonservice.inventory.command;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustInventoryCommand {
    private UUID productId;
    private int availableQuantity;
}
