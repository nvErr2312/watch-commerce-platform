package com.fullstack.paymentservice.command.repository;

import com.fullstack.paymentservice.command.model.PaymentRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {
    Optional<PaymentRecord> findByOrderId(Long orderId);
}
