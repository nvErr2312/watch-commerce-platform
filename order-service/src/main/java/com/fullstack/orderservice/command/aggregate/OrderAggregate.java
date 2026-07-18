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
        requireStatus(OrderStatus.PENDING_SHIPPING_FEE, "Order is not waiting for shipping fee");
        AggregateLifecycle.apply(new OrderTotalUpdatedEvent(
                command.getOrderId(),
                command.getShippingFee(),
                command.getTotalAmount(),
                OrderStatus.PENDING_PAYMENT.name()));
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        if (OrderStatus.CANCELLED.equals(status)) {
            return;
        }
        if (OrderStatus.COMPLETED.equals(status)) {
            throw new IllegalStateException("Completed order cannot be cancelled");
        }
        AggregateLifecycle.apply(new OrderCancelledEvent(
                command.getOrderId(),
                command.getReason(),
                OrderStatus.CANCELLED.name()));
    }

    @CommandHandler
    public void handle(RequestOrderCancelCommand command) {
        if (OrderStatus.CANCELLED.equals(status)) {
            return;
        }
        if (!OrderStatus.PENDING_SHIPPING_FEE.equals(status)) {
            throw new IllegalStateException("Order can no longer be cancelled");
        }
        AggregateLifecycle.apply(new OrderCancelRequestedEvent(command.getOrderId(), command.getReason()));
    }

    @CommandHandler
    public void handle(UpdateOrderStatusCommand command) {
        if (OrderStatus.SHIPPING_CREATED.name().equals(command.getStatus())
                && OrderStatus.SHIPPING_CREATED.equals(status)) {
            return;
        }
        requireStatus(OrderStatus.PENDING_PAYMENT, "Order is not ready for shipping");
        AggregateLifecycle.apply(new OrderStatusUpdatedEvent(command.getOrderId(), command.getStatus()));
    }

    @CommandHandler
    public void handle(ConfirmReceivedCommand command) {
        requireStatus(OrderStatus.SHIPPING_CREATED, "Order is not being shipped");
        AggregateLifecycle.apply(new OrderCompletedEvent(command.getOrderId(), OrderStatus.COMPLETED.name()));
    }

    private void requireStatus(OrderStatus expected, String message) {
        if (!expected.equals(status)) {
            throw new IllegalStateException(message);
        }
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
