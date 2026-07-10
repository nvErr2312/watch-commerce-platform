package com.fullstack.paymentservice.command.handler;

import com.fullstack.commonservice.payment.command.CreatePaymentCommand;
import com.fullstack.commonservice.payment.command.RefundPaymentCommand;
import com.fullstack.commonservice.payment.event.PaymentCreatedEvent;
import com.fullstack.commonservice.payment.event.PaymentRefundedEvent;
import com.fullstack.paymentservice.command.model.PaymentRecord;
import com.fullstack.paymentservice.command.model.PaymentStatus;
import com.fullstack.paymentservice.command.repository.PaymentRecordRepository;
import com.fullstack.paymentservice.payos.PayOsClient;
import com.fullstack.paymentservice.payos.PayOsPaymentLink;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCommandHandler {
    private final EventGateway eventGateway;
    private final PaymentRecordRepository repository;
    private final PayOsClient payOsClient;

    @CommandHandler
    public void handle(CreatePaymentCommand command) {
        PaymentRecord payment = repository.findByOrderId(command.getOrderId()).orElseGet(() -> {
            PayOsPaymentLink link = payOsClient.createPaymentLink(command.getOrderId(), command.getAmount());
            PaymentRecord record = new PaymentRecord();
            record.setPaymentId(link.paymentLinkId());
            record.setOrderId(command.getOrderId());
            record.setAmount(command.getAmount());
            record.setPaymentUrl(link.checkoutUrl());
            record.setStatus(PaymentStatus.PENDING.name());
            record.setCreatedAt(Instant.now());
            record.setExpiresAt(link.expiresAt());
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
