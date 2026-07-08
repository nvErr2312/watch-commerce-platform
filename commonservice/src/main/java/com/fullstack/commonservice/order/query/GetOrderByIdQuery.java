package com.fullstack.commonservice.order.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderByIdQuery {
    private Long orderId;
}
