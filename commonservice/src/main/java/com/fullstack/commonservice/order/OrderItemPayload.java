package com.fullstack.commonservice.order;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPayload {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;
}
