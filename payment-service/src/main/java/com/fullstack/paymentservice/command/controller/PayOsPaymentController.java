package com.fullstack.paymentservice.command.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fullstack.commonservice.payment.event.PaymentCancelledEvent;
import com.fullstack.commonservice.payment.event.PaymentSucceededEvent;
import com.fullstack.paymentservice.command.model.PaymentStatus;
import com.fullstack.paymentservice.command.repository.PaymentRecordRepository;
import com.fullstack.paymentservice.payos.PayOsClient;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PayOsPaymentController {
    private final PaymentRecordRepository repository;
    private final EventGateway eventGateway;
    private final PayOsClient payOsClient;

    @PostMapping("/api/payments/payos/webhook")
    public ResponseEntity<Void> webhook(@RequestBody JsonNode payload) {
        JsonNode data = payload.path("data");
        if (!payOsClient.validSignature(data, payload.path("signature").asText(null))) {
            return ResponseEntity.badRequest().build();
        }
        if (!payload.path("success").asBoolean(false) || !"00".equals(data.path("code").asText())) {
            return ResponseEntity.ok().build();
        }
        markSucceeded(data.path("orderCode").asLong());
        return ResponseEntity.ok().build();
    }

    @org.springframework.beans.factory.annotation.Value("${app.frontend.home-url}")
    private String homeUrl;

    @GetMapping("/api/payments/payos/cancel")
    public ResponseEntity<Void> cancel(@org.springframework.web.bind.annotation.RequestParam(value = "orderCode", required = false) Long orderId) {
        if (orderId != null) {
            markCancelled(orderId);
        }
        return ResponseEntity.status(302)
                .header("Location", homeUrl + "?status=cancel")
                .build();
    }

    @GetMapping("/api/payments/payos/return")
    public ResponseEntity<Void> success(@org.springframework.web.bind.annotation.RequestParam(value = "orderCode", required = false) Long orderId) {
        return ResponseEntity.status(302)
                .header("Location", homeUrl + "?status=success")
                .build();
    }

    private void markSucceeded(Long orderId) {
        repository.findByOrderId(orderId).ifPresent(payment -> {
            if (!PaymentStatus.PENDING.name().equals(payment.getStatus())) {
                return;
            }
            payment.setStatus(PaymentStatus.SUCCEEDED.name());
            repository.save(payment);
            eventGateway.publish(new PaymentSucceededEvent(orderId, payment.getPaymentId()));
        });
    }

    private void markCancelled(Long orderId) {
        repository.findByOrderId(orderId).ifPresent(payment -> {
            if (!PaymentStatus.PENDING.name().equals(payment.getStatus())) {
                return;
            }
            payment.setStatus(PaymentStatus.CANCELLED.name());
            repository.save(payment);
            eventGateway.publish(new PaymentCancelledEvent(orderId, payment.getPaymentId()));
        });
    }
}
