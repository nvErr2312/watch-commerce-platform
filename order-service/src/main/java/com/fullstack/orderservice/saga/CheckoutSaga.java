package com.fullstack.orderservice.saga;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fullstack.commonservice.inventory.command.ReleaseInventoryCommand;
import com.fullstack.commonservice.inventory.command.ReserveInventoryCommand;
import com.fullstack.commonservice.inventory.event.InventoryReleasedEvent;
import com.fullstack.commonservice.inventory.event.InventoryReserveFailedEvent;
import com.fullstack.commonservice.inventory.event.InventoryReservedEvent;
import com.fullstack.commonservice.order.OrderItemPayload;
import com.fullstack.commonservice.order.command.CancelOrderCommand;
import com.fullstack.commonservice.order.command.UpdateOrderStatusCommand;
import com.fullstack.commonservice.order.command.UpdateOrderTotalCommand;
import com.fullstack.commonservice.order.event.OrderCancelRequestedEvent;
import com.fullstack.commonservice.order.event.OrderCancelledEvent;
import com.fullstack.commonservice.order.event.OrderCreatedEvent;
import com.fullstack.commonservice.order.event.OrderStatusUpdatedEvent;
import com.fullstack.commonservice.order.event.OrderTotalUpdatedEvent;
import com.fullstack.commonservice.payment.command.CreatePaymentCommand;
import com.fullstack.commonservice.payment.command.RefundPaymentCommand;
import com.fullstack.commonservice.payment.event.PaymentCancelledEvent;
import com.fullstack.commonservice.payment.event.PaymentCreatedEvent;
import com.fullstack.commonservice.payment.event.PaymentExpiredEvent;
import com.fullstack.commonservice.payment.event.PaymentRefundedEvent;
import com.fullstack.commonservice.payment.event.PaymentSucceededEvent;
import com.fullstack.commonservice.shipping.command.CalculateShippingFeeCommand;
import com.fullstack.commonservice.shipping.command.CreateShippingCommand;
import com.fullstack.commonservice.shipping.event.ShippingCreatedEvent;
import com.fullstack.commonservice.shipping.event.ShippingFailedEvent;
import com.fullstack.commonservice.shipping.event.ShippingFeeCalculatedEvent;
import com.fullstack.orderservice.command.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
@NoArgsConstructor
@Slf4j
public class CheckoutSaga {
    private static final String RETRY_DEADLINE = "checkout-stage-retry";
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(3);
    private static final Duration RECOVERY_DELAY = Duration.ofSeconds(30);

    private static final String RESERVE = "RESERVE";
    private static final String FEE = "FEE";
    private static final String UPDATE_TOTAL = "UPDATE_TOTAL";
    private static final String CREATE_PAYMENT = "CREATE_PAYMENT";
    private static final String WAIT_PAYMENT = "WAIT_PAYMENT";
    private static final String CREATE_SHIPPING = "CREATE_SHIPPING";
    private static final String MARK_SHIPPING = "MARK_SHIPPING";
    private static final String REFUND = "REFUND";
    private static final String RELEASE = "RELEASE";
    private static final String CANCEL = "CANCEL";

    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient Configuration configuration;

    @JsonProperty
    private Long orderId;
    @JsonProperty
    private List<OrderItemPayload> items;
    @JsonProperty
    private String shippingAddress;
    @JsonProperty
    private BigDecimal subtotalAmount;
    @JsonProperty
    private BigDecimal shippingFee;
    @JsonProperty
    private BigDecimal totalAmount;
    @JsonProperty
    private String stage;
    @JsonProperty
    private String failureReason;
    @JsonProperty
    private int attempts;
    @JsonProperty
    private boolean paid;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCreatedEvent event) {
        orderId = event.getOrderId();
        items = event.getItems();
        shippingAddress = event.getShippingAddress();
        subtotalAmount = event.getTotalAmount();
        totalAmount = event.getTotalAmount();
        begin(RESERVE);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReserveFailedEvent event) {
        if (RESERVE.equals(stage)) {
            failureReason = event.getReason();
            begin(CANCEL);
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReservedEvent event) {
        if (!RESERVE.equals(stage)) {
            return;
        }
        items = event.getItems();
        shippingAddress = event.getShippingAddress();
        subtotalAmount = event.getSubtotalAmount();
        begin(FEE);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShippingFeeCalculatedEvent event) {
        if (!FEE.equals(stage)) {
            return;
        }
        shippingFee = event.getShippingFee();
        totalAmount = event.getTotalAmount();
        begin(UPDATE_TOTAL);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderTotalUpdatedEvent event) {
        if (!UPDATE_TOTAL.equals(stage)) {
            return;
        }
        totalAmount = event.getTotalAmount();
        begin(CREATE_PAYMENT);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentCreatedEvent event) {
        if (!CREATE_PAYMENT.equals(stage)) {
            return;
        }
        stopRetry();
        stage = WAIT_PAYMENT;
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentExpiredEvent event) {
        if (WAIT_PAYMENT.equals(stage)) {
            compensate("Payment expired");
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentCancelledEvent event) {
        if (WAIT_PAYMENT.equals(stage)) {
            compensate("Payment cancelled");
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCancelRequestedEvent event) {
        if (!isCompensating()) {
            compensate(event.getReason());
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentSucceededEvent event) {
        if (!WAIT_PAYMENT.equals(stage)) {
            return;
        }
        paid = true;
        begin(CREATE_SHIPPING);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShippingFailedEvent event) {
        if (CREATE_SHIPPING.equals(stage)) {
            compensate(event.getReason());
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(ShippingCreatedEvent event) {
        if (CREATE_SHIPPING.equals(stage)) {
            begin(MARK_SHIPPING);
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(PaymentRefundedEvent event) {
        if (REFUND.equals(stage)) {
            paid = false;
            begin(RELEASE);
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void on(InventoryReleasedEvent event) {
        if (RELEASE.equals(stage)) {
            begin(CANCEL);
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderCancelledEvent event) {
        stopRetry();
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void on(OrderStatusUpdatedEvent event) {
        stopRetry();
    }

    @DeadlineHandler(deadlineName = RETRY_DEADLINE)
    public void retry(String expectedStage) {
        if (!expectedStage.equals(stage)) {
            return;
        }
        if (attempts < MAX_ATTEMPTS) {
            dispatch(RETRY_DELAY);
            return;
        }
        if (isRecoveryStage()) {
            dispatch(RECOVERY_DELAY);
            return;
        }
        log.error("Checkout stage {} failed after {} attempts for order {}", stage, attempts, orderId);
        failureReason = "Service unavailable at checkout stage " + stage;
        if (RESERVE.equals(stage)) {
            begin(CANCEL);
        } else if (CREATE_SHIPPING.equals(stage) || paid) {
            begin(REFUND);
        } else {
            begin(RELEASE);
        }
    }

    private void compensate(String reason) {
        failureReason = reason;
        begin(paid ? REFUND : RELEASE);
    }

    private void begin(String nextStage) {
        stopRetry();
        stage = nextStage;
        attempts = 0;
        dispatch(RETRY_DELAY);
    }

    private void dispatch(Duration delay) {
        attempts++;
        configuration.deadlineManager().schedule(delay, RETRY_DEADLINE, stage);
        Object command = switch (stage) {
            case RESERVE -> new ReserveInventoryCommand(orderId, items, subtotalAmount, shippingAddress);
            case FEE -> new CalculateShippingFeeCommand(orderId, items, subtotalAmount, shippingAddress);
            case UPDATE_TOTAL -> new UpdateOrderTotalCommand(orderId, shippingFee, totalAmount);
            case CREATE_PAYMENT -> new CreatePaymentCommand(orderId, totalAmount);
            case CREATE_SHIPPING -> new CreateShippingCommand(orderId, shippingAddress);
            case MARK_SHIPPING -> new UpdateOrderStatusCommand(orderId, OrderStatus.SHIPPING_CREATED.name());
            case REFUND -> new RefundPaymentCommand(orderId);
            case RELEASE -> new ReleaseInventoryCommand(orderId, items);
            case CANCEL -> new CancelOrderCommand(orderId, failureReason);
            default -> throw new IllegalStateException("Unknown checkout stage " + stage);
        };
        commandGateway.send(command).exceptionally(error -> {
            log.warn("Checkout command {} attempt {} failed for order {}: {}",
                    stage, attempts, orderId, error.getMessage());
            return null;
        });
    }

    private void stopRetry() {
        configuration.deadlineManager().cancelAllWithinScope(RETRY_DEADLINE);
    }

    private boolean isCompensating() {
        return REFUND.equals(stage) || RELEASE.equals(stage) || CANCEL.equals(stage);
    }

    private boolean isRecoveryStage() {
        return isCompensating() || MARK_SHIPPING.equals(stage);
    }
}
