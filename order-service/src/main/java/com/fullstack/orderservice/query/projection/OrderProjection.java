package com.fullstack.orderservice.query.projection;

import com.fullstack.commonservice.order.event.OrderCancelledEvent;
import com.fullstack.commonservice.order.event.OrderCompletedEvent;
import com.fullstack.commonservice.order.event.OrderCreatedEvent;
import com.fullstack.commonservice.order.event.OrderStatusUpdatedEvent;
import com.fullstack.commonservice.order.event.OrderTotalUpdatedEvent;
import com.fullstack.commonservice.payment.event.PaymentCreatedEvent;
import com.fullstack.orderservice.query.model.OrderReadModel;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ProcessingGroup("order-projection")
public class OrderProjection {
    private final OrderReadModelRepository repository;

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderReadModel order = new OrderReadModel();
        order.setId(event.getOrderId());
        order.setUserId(event.getUserId());
        order.setStatus(event.getStatus());
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(event.getTotalAmount());
        order.setShippingAddress(event.getShippingAddress());
        order.setUpdatedAt(Instant.now());
        repository.save(order);
    }

    @EventHandler
    public void on(OrderTotalUpdatedEvent event) {
        repository.findById(event.getOrderId()).ifPresent(order -> {
            order.setShippingFee(event.getShippingFee());
            order.setTotalAmount(event.getTotalAmount());
            order.setStatus(event.getStatus());
            order.setUpdatedAt(Instant.now());
            repository.save(order);
        });
    }

    @EventHandler
    public void on(PaymentCreatedEvent event) {
        repository.findById(event.getOrderId()).ifPresent(order -> {
            order.setPaymentUrl(event.getPaymentUrl());
            order.setUpdatedAt(Instant.now());
            repository.save(order);
        });
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        repository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(event.getStatus());
            order.setUpdatedAt(Instant.now());
            repository.save(order);
        });
    }

    @EventHandler
    public void on(OrderStatusUpdatedEvent event) {
        repository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(event.getStatus());
            order.setUpdatedAt(Instant.now());
            repository.save(order);
        });
    }

    @EventHandler
    public void on(OrderCompletedEvent event) {
        repository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(event.getStatus());
            order.setUpdatedAt(Instant.now());
            repository.save(order);
        });
    }
}
