package com.fullstack.orderservice.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private String status;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentUrl;
    private Instant confirmAvailableAt;
}
