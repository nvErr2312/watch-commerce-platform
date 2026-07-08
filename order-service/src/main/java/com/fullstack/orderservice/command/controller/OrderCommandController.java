package com.fullstack.orderservice.command.controller;

import com.fullstack.commonservice.order.OrderItemPayload;
import com.fullstack.commonservice.order.command.ConfirmReceivedCommand;
import com.fullstack.commonservice.order.command.CreateOrderCommand;
import com.fullstack.commonservice.order.command.RequestOrderCancelCommand;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.orderservice.command.model.OrderStatus;
import com.fullstack.orderservice.dto.request.CreateOrderRequest;
import com.fullstack.orderservice.dto.response.OrderResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderCommandController {
    private final CommandGateway commandGateway;

    @PostMapping("/api/orders")
    public ResponseEntity<ResponseData<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = newOrderId();
        List<OrderItemPayload> items = request.getItems().stream()
                .map(item -> new OrderItemPayload(item.getProductId(), item.getQuantity(), item.getUnitPrice()))
                .toList();
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        commandGateway.sendAndWait(new CreateOrderCommand(
                orderId,
                request.getUserId(),
                items,
                totalAmount,
                request.getShippingAddress()));

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseData<>(
                "ORDER_CREATED",
                "Order created",
                OrderResponse.builder()
                        .orderId(orderId)
                        .userId(request.getUserId())
                        .status(OrderStatus.PENDING_SHIPPING_FEE.name())
                        .totalAmount(totalAmount)
                        .shippingAddress(request.getShippingAddress())
                        .build()));
    }

    @PostMapping("/api/orders/{id}/cancel")
    public ResponseEntity<ResponseData<Void>> cancel(@PathVariable Long id) {
        commandGateway.sendAndWait(new RequestOrderCancelCommand(id, "User cancelled order"));
        return ResponseEntity.ok(new ResponseData<>("ORDER_CANCEL_REQUESTED", "Order cancel requested", null));
    }

    @PostMapping("/api/orders/{id}/confirm-received")
    public ResponseEntity<ResponseData<Void>> confirmReceived(@PathVariable Long id) {
        commandGateway.sendAndWait(new ConfirmReceivedCommand(id));
        return ResponseEntity.ok(new ResponseData<>("ORDER_COMPLETED", "Order completed", null));
    }

    private Long newOrderId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}
