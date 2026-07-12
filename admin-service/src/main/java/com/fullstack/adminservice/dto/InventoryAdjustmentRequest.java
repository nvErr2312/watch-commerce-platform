package com.fullstack.adminservice.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryAdjustmentRequest {
    @Min(0)
    private int availableQuantity;
}
