package com.fullstack.paymentservice.command.controller;

import com.fullstack.commonservice.payment.event.PaymentCancelledEvent;
import com.fullstack.commonservice.payment.event.PaymentExpiredEvent;
import com.fullstack.commonservice.payment.event.PaymentSucceededEvent;
import com.fullstack.paymentservice.command.model.PaymentRecord;
import com.fullstack.paymentservice.command.model.PaymentStatus;
import com.fullstack.paymentservice.command.repository.PaymentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MockPaymentActionController {
    private final PaymentRecordRepository repository;
    private final EventGateway eventGateway;

    @PostMapping("/api/mock/payments/{paymentId}/success")
    public void success(@PathVariable String paymentId) {
        update(paymentId, PaymentStatus.SUCCEEDED);
    }

    @PostMapping("/api/mock/payments/{paymentId}/expire")
    public void expire(@PathVariable String paymentId) {
        update(paymentId, PaymentStatus.EXPIRED);
    }

    @PostMapping("/api/mock/payments/{paymentId}/cancel")
    public void cancel(@PathVariable String paymentId) {
        update(paymentId, PaymentStatus.CANCELLED);
    }

    private void update(String paymentId, PaymentStatus status) {
        PaymentRecord payment = repository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus(status.name());
        repository.save(payment);
        if (status == PaymentStatus.SUCCEEDED) {
            eventGateway.publish(new PaymentSucceededEvent(payment.getOrderId(), paymentId));
        } else if (status == PaymentStatus.EXPIRED) {
            eventGateway.publish(new PaymentExpiredEvent(payment.getOrderId(), paymentId));
        } else if (status == PaymentStatus.CANCELLED) {
            eventGateway.publish(new PaymentCancelledEvent(payment.getOrderId(), paymentId));
        }
    }
}
