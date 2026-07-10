package com.fullstack.paymentservice.command.job;

import com.fullstack.commonservice.payment.event.PaymentExpiredEvent;
import com.fullstack.paymentservice.command.model.PaymentStatus;
import com.fullstack.paymentservice.command.repository.PaymentRecordRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentExpiryJob {
    private final PaymentRecordRepository repository;
    private final EventGateway eventGateway;

    @Scheduled(fixedDelayString = "${app.payment.expire-scan-delay-ms:60000}")
    @Transactional
    public void expirePendingPayments() {
        repository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING.name(), Instant.now())
                .forEach(payment -> {
                    payment.setStatus(PaymentStatus.EXPIRED.name());
                    repository.save(payment);
                    eventGateway.publish(new PaymentExpiredEvent(payment.getOrderId(), payment.getPaymentId()));
                });
    }
}
