package com.fullstack.paymentservice.command.handler;

import com.fullstack.commonservice.payment.command.CreatePaymentCommand;
import com.fullstack.commonservice.payment.command.RefundPaymentCommand;
import com.fullstack.commonservice.payment.event.PaymentCreatedEvent;
import com.fullstack.commonservice.payment.event.PaymentRefundedEvent;
import com.fullstack.paymentservice.command.model.PaymentMethod;
import com.fullstack.paymentservice.command.model.PaymentRecord;
import com.fullstack.paymentservice.command.model.PaymentStatus;
import com.fullstack.paymentservice.command.repository.PaymentMethodRepository;
import com.fullstack.paymentservice.command.repository.PaymentRecordRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCommandHandler {
    private final EventGateway eventGateway;
    private final PaymentRecordRepository repository;
    private final PaymentMethodRepository methodRepository;

    @Value("${app.payment.base-url}")
    private String paymentBaseUrl;

    @CommandHandler
    public void handle(CreatePaymentCommand command) {
        PaymentRecord payment = repository.findByOrderId(command.getOrderId()).orElseGet(() -> {
            String paymentId = UUID.randomUUID().toString();
            String baseUrl = methodRepository.findById("QR")
                    .filter(PaymentMethod::isActive)
                    .map(PaymentMethod::getBaseUrl)
                    .orElse(paymentBaseUrl);
            PaymentRecord record = new PaymentRecord();
            record.setPaymentId(paymentId);
            record.setOrderId(command.getOrderId());
            record.setAmount(command.getAmount());
            record.setPaymentUrl(baseUrl + "/" + paymentId);
            record.setStatus(PaymentStatus.PENDING.name());
            record.setCreatedAt(Instant.now());
            return repository.save(record);
        });
        eventGateway.publish(new PaymentCreatedEvent(
                command.getOrderId(),
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getPaymentUrl()));
    }

    @CommandHandler
    public void handle(RefundPaymentCommand command) {
        repository.findByOrderId(command.getOrderId()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.REFUNDED.name());
            repository.save(payment);
            eventGateway.publish(new PaymentRefundedEvent(payment.getOrderId(), payment.getPaymentId()));
        });
    }
}
