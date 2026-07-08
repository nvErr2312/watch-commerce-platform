package com.fullstack.orderservice.query.handler;

import com.fullstack.commonservice.advice.ResourceNotFoundException;
import com.fullstack.commonservice.order.query.GetOrderByIdQuery;
import com.fullstack.commonservice.order.result.OrderResult;
import com.fullstack.orderservice.query.model.OrderReadModel;
import com.fullstack.orderservice.query.projection.OrderReadModelRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderQueryHandler {
    private final OrderReadModelRepository repository;

    @QueryHandler
    public OrderResult handle(GetOrderByIdQuery query) {
        OrderReadModel order = repository.findById(query.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new OrderResult(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentUrl());
    }
}
