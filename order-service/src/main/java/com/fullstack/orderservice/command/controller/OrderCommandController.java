package com.fullstack.orderservice.command.controller;

import com.fullstack.commonservice.order.OrderItemPayload;
import com.fullstack.commonservice.order.command.ConfirmReceivedCommand;
import com.fullstack.commonservice.order.command.CreateOrderCommand;
import com.fullstack.commonservice.order.command.RequestOrderCancelCommand;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.advice.ResourceNotFoundException;
import com.fullstack.commonservice.security.jwt.JwtClaims;
import com.fullstack.orderservice.command.model.OrderStatus;
import com.fullstack.orderservice.dto.request.CreateOrderRequest;
import com.fullstack.orderservice.dto.response.OrderResponse;
import com.fullstack.orderservice.query.model.OrderReadModel;
import com.fullstack.orderservice.query.projection.OrderReadModelRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderCommandController {
    private static final Duration CONFIRM_DELAY = Duration.ofMinutes(2);
    private final CommandGateway commandGateway;
    private final OrderReadModelRepository repository;

    @PostMapping("/api/v1/orders")
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

    @PostMapping("/api/v1/orders/{id}/cancel")
    public ResponseEntity<ResponseData<Void>> cancel(@PathVariable Long id) {
        commandGateway.sendAndWait(new RequestOrderCancelCommand(id, "User cancelled order"));
        return ResponseEntity.ok(new ResponseData<>("ORDER_CANCEL_REQUESTED", "Order cancel requested", null));
    }

    @PostMapping("/api/v1/orders/{id}/confirm-received")
    public ResponseEntity<ResponseData<Void>> confirmReceived(@PathVariable Long id, Authentication authentication) {
        OrderReadModel order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        JwtClaims claims = (JwtClaims) authentication.getPrincipal();
        if (!Long.valueOf(claims.userId()).equals(order.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!OrderStatus.SHIPPING_CREATED.name().equals(order.getStatus())) {
            throw new IllegalArgumentException("Đơn hàng chưa ở trạng thái đang vận chuyển");
        }
        if (order.getUpdatedAt() == null) {
            throw new IllegalArgumentException("Chưa xác định được thời điểm bắt đầu vận chuyển");
        }
        Instant availableAt = order.getUpdatedAt().plus(CONFIRM_DELAY);
        if (Instant.now().isBefore(availableAt)) {
            throw new IllegalArgumentException("Chỉ có thể xác nhận đã nhận hàng sau " + availableAt);
        }
        commandGateway.sendAndWait(new ConfirmReceivedCommand(id));
        return ResponseEntity.ok(new ResponseData<>("ORDER_COMPLETED", "Order completed", null));
    }

    private Long newOrderId() {
        return UUID.randomUUID().getMostSignificantBits() & ((1L << 53) - 1);
    }
}
