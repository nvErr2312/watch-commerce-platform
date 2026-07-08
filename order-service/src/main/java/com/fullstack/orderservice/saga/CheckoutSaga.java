package com.fullstack.orderservice.saga;

import com.fullstack.commonservice.inventory.command.ReleaseInventoryCommand;
import com.fullstack.commonservice.inventory.command.ReserveInventoryCommand;
import com.fullstack.commonservice.inventory.event.InventoryReserveFailedEvent;
import com.fullstack.commonservice.inventory.event.InventoryReservedEvent;
import com.fullstack.commonservice.order.OrderItemPayload;
import com.fullstack.commonservice.order.command.CancelOrderCommand;
import com.fullstack.commonservice.order.command.UpdateOrderTotalCommand;
import com.fullstack.commonservice.order.command.UpdateOrderStatusCommand;
import com.fullstack.commonservice.order.event.OrderCancelRequestedEvent;
import com.fullstack.commonservice.order.event.OrderCreatedEvent;
import com.fullstack.commonservice.order.event.OrderTotalUpdatedEvent;
import com.fullstack.commonservice.payment.command.CreatePaymentCommand;
import com.fullstack.commonservice.payment.command.RefundPaymentCommand;
import com.fullstack.commonservice.payment.event.PaymentCancelledEvent;
import com.fullstack.commonservice.payment.event.PaymentExpiredEvent;
import com.fullstack.commonservice.payment.event.PaymentSucceededEvent;
import com.fullstack.commonservice.shipping.command.CalculateShippingFeeCommand;
import com.fullstack.commonservice.shipping.command.CreateShippingCommand;
import com.fullstack.commonservice.shipping.event.ShippingCreatedEvent;
import com.fullstack.commonservice.shipping.event.ShippingFailedEvent;
import com.fullstack.commonservice.shipping.event.ShippingFeeCalculatedEvent;
import com.fullstack.orderservice.command.model.OrderStatus;
import java.util.List;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@NoArgsConstructor
public class CheckoutSaga {
    @Autowired
    private transient CommandGateway commandGateway;
    private List<OrderItemPayload> items;
    private String shippingAddress;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event) {
        items = event.getItems();
        shippingAddress = event.getShippingAddress();
        commandGateway.send(new ReserveInventoryCommand(
                event.getOrderId(),
                event.getItems(),
                event.getTotalAmount(),
                event.getShippingAddress()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReserveFailedEvent event) {
        commandGateway.send(new CancelOrderCommand(event.getOrderId(), event.getReason()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReservedEvent event) {
        items = event.getItems();
        shippingAddress = event.getShippingAddress();
        // ponytail: query order state later if shipping fee needs destination rules beyond address.
        commandGateway.send(new CalculateShippingFeeCommand(event.getOrderId(), event.getItems(),
                event.getSubtotalAmount(), event.getShippingAddress()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShippingFeeCalculatedEvent event) {
        commandGateway.send(new UpdateOrderTotalCommand(
                event.getOrderId(),
                event.getShippingFee(),
                event.getTotalAmount()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderTotalUpdatedEvent event) {
        commandGateway.send(new CreatePaymentCommand(event.getOrderId(), event.getTotalAmount()));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentExpiredEvent event) {
        cancelAndRelease(event.getOrderId(), "Payment expired");
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentCancelledEvent event) {
        cancelAndRelease(event.getOrderId(), "Payment cancelled");
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCancelRequestedEvent event) {
        cancelAndRelease(event.getOrderId(), event.getReason());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentSucceededEvent event) {
        commandGateway.send(new CreateShippingCommand(event.getOrderId(), shippingAddress));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShippingFailedEvent event) {
        commandGateway.send(new RefundPaymentCommand(event.getOrderId()));
        cancelAndRelease(event.getOrderId(), event.getReason());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShippingCreatedEvent event) {
        commandGateway.send(new UpdateOrderStatusCommand(event.getOrderId(), OrderStatus.SHIPPING_CREATED.name()));
    }

    private void cancelAndRelease(Long orderId, String reason) {
        commandGateway.send(new ReleaseInventoryCommand(orderId, items));
        commandGateway.send(new CancelOrderCommand(orderId, reason));
    }
}
