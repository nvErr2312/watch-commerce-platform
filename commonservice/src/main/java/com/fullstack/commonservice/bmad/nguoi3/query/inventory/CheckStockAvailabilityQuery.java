package com.fullstack.commonservice.bmad.nguoi3.query.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckStockAvailabilityQuery {

    private String productId;
    private int requestedQuantity;
}
