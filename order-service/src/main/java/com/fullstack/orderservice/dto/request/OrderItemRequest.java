package com.fullstack.orderservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    @NotNull
    @Min(1)
    private Long productId;

    @Min(1)
    private int quantity;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal unitPrice;
}
