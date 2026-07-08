package com.fullstack.orderservice.query.controller;

import com.fullstack.commonservice.order.query.GetOrderByIdQuery;
import com.fullstack.commonservice.order.result.OrderResult;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.orderservice.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderQueryController {
    private final QueryGateway queryGateway;

    @GetMapping("/api/orders/{id}")
    public ResponseEntity<ResponseData<OrderResponse>> getById(@PathVariable Long id) {
        OrderResult order = queryGateway.query(new GetOrderByIdQuery(id),
                ResponseTypes.instanceOf(OrderResult.class)).join();
        return ResponseEntity.ok(new ResponseData<>("ORDER_FOUND", "Order found", OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentUrl(order.getPaymentUrl())
                .build()));
    }
}
