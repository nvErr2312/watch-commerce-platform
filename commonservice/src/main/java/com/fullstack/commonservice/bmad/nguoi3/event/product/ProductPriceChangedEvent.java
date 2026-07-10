package com.fullstack.commonservice.bmad.nguoi3.event.product;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceChangedEvent {

    private String productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private Instant changedAt;
}
