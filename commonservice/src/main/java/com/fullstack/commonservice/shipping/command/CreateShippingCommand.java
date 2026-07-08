package com.fullstack.commonservice.shipping.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateShippingCommand {
    private Long orderId;
    private String shippingAddress;
}
