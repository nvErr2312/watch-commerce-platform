package com.fullstack.orderservice.command.aggregate;

import com.fullstack.commonservice.order.command.CreateOrderCommand;
import com.fullstack.commonservice.order.command.CancelOrderCommand;
import com.fullstack.commonservice.order.command.ConfirmReceivedCommand;
import com.fullstack.commonservice.order.command.RequestOrderCancelCommand;
import com.fullstack.commonservice.order.command.UpdateOrderTotalCommand;
import com.fullstack.commonservice.order.command.UpdateOrderStatusCommand;
import com.fullstack.commonservice.order.event.OrderCancelRequestedEvent;
import com.fullstack.commonservice.order.event.OrderCancelledEvent;
import com.fullstack.commonservice.order.event.OrderCompletedEvent;
import com.fullstack.commonservice.order.event.OrderCreatedEvent;
import com.fullstack.commonservice.order.event.OrderStatusUpdatedEvent;
import com.fullstack.commonservice.order.event.OrderTotalUpdatedEvent;
import com.fullstack.orderservice.command.model.OrderStatus;
import java.math.BigDecimal;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
@NoArgsConstructor
public class OrderAggregate {
    @AggregateIdentifier
    private Long orderId;
    private OrderStatus status;
    private BigDecimal totalAmount;

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        AggregateLifecycle.apply(new OrderCreatedEvent(
                command.getOrderId(),
                command.getUserId(),
                command.getItems(),
                command.getTotalAmount(),
                command.getShippingAddress(),
                OrderStatus.PENDING_SHIPPING_FEE.name()));
    }

    @CommandHandler
    public void handle(UpdateOrderTotalCommand command) {
        AggregateLifecycle.apply(new OrderTotalUpdatedEvent(
                command.getOrderId(),
                command.getShippingFee(),
                command.getTotalAmount(),
                OrderStatus.PENDING_PAYMENT.name()));
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        AggregateLifecycle.apply(new OrderCancelledEvent(
                command.getOrderId(),
                command.getReason(),
                OrderStatus.CANCELLED.name()));
    }

    @CommandHandler
    public void handle(RequestOrderCancelCommand command) {
        AggregateLifecycle.apply(new OrderCancelRequestedEvent(command.getOrderId(), command.getReason()));
    }

    @CommandHandler
    public void handle(UpdateOrderStatusCommand command) {
        AggregateLifecycle.apply(new OrderStatusUpdatedEvent(command.getOrderId(), command.getStatus()));
    }

    @CommandHandler
    public void handle(ConfirmReceivedCommand command) {
        AggregateLifecycle.apply(new OrderCompletedEvent(command.getOrderId(), OrderStatus.COMPLETED.name()));
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        orderId = event.getOrderId();
        status = OrderStatus.valueOf(event.getStatus());
        totalAmount = event.getTotalAmount();
    }

    @EventSourcingHandler
    public void on(OrderTotalUpdatedEvent event) {
        totalAmount = event.getTotalAmount();
        status = OrderStatus.valueOf(event.getStatus());
    }

    @EventSourcingHandler
    public void on(OrderCancelledEvent event) {
        status = OrderStatus.valueOf(event.getStatus());
    }

    @EventSourcingHandler
    public void on(OrderStatusUpdatedEvent event) {
        status = OrderStatus.valueOf(event.getStatus());
    }

    @EventSourcingHandler
    public void on(OrderCompletedEvent event) {
        status = OrderStatus.valueOf(event.getStatus());
    }
}
