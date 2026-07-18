package com.fullstack.paymentservice.command.repository;

import com.fullstack.paymentservice.command.model.PaymentRecord;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {
    Optional<PaymentRecord> findByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentRecord p where p.orderId = :orderId")
    Optional<PaymentRecord> findByOrderIdForUpdate(@Param("orderId") Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<PaymentRecord> findByStatusAndExpiresAtBefore(String status, Instant now);
}
