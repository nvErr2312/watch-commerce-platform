package com.fullstack.orderservice.query.controller;

import com.fullstack.commonservice.advice.ResourceNotFoundException;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.commonservice.security.jwt.JwtClaims;
import com.fullstack.orderservice.dto.response.OrderResponse;
import com.fullstack.orderservice.command.model.OrderStatus;
import com.fullstack.orderservice.query.model.OrderReadModel;
import com.fullstack.orderservice.query.projection.OrderReadModelRepository;
import java.util.List;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderQueryController {
    private static final Duration CONFIRM_DELAY = Duration.ofMinutes(2);
    private final OrderReadModelRepository repository;

    @GetMapping("/api/v1/orders")
    public ResponseEntity<ResponseData<List<OrderResponse>>> getAllOrders(Authentication authentication) {
        JwtClaims claims = (JwtClaims) authentication.getPrincipal();
        if (!"ADMIN".equals(claims.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(new ResponseData<>(
                "ORDERS_FOUND", "Orders found", repository.findAll().stream().map(this::toResponse).toList()));
    }

    @GetMapping("/api/v1/orders/me")
    public ResponseEntity<ResponseData<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        JwtClaims claims = (JwtClaims) authentication.getPrincipal();
        Long userId = Long.parseLong(claims.userId());
        List<OrderReadModel> orders = repository.findByUserIdOrderByUpdatedAtDesc(userId);
        List<OrderResponse> responseList = orders.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new ResponseData<>("ORDERS_FOUND", "Orders found", responseList));
    }

    @GetMapping("/api/v1/orders/{id}")
    public ResponseEntity<ResponseData<OrderResponse>> getById(@PathVariable Long id, Authentication authentication) {
        OrderReadModel order = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        JwtClaims claims = (JwtClaims) authentication.getPrincipal();
        if (!"ADMIN".equals(claims.role()) && !Long.valueOf(claims.userId()).equals(order.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(new ResponseData<>("ORDER_FOUND", "Order found", toResponse(order)));
    }

    private OrderResponse toResponse(OrderReadModel order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentUrl(order.getPaymentUrl())
                .confirmAvailableAt(OrderStatus.SHIPPING_CREATED.name().equals(order.getStatus())
                        && order.getUpdatedAt() != null ? order.getUpdatedAt().plus(CONFIRM_DELAY) : null)
                .build();
    }
}
