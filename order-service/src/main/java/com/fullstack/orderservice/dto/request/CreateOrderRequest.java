package com.fullstack.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {
    @NotNull
    private Long userId;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;

    @NotBlank
    private String shippingAddress;
}
