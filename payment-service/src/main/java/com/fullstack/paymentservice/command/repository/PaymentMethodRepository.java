package com.fullstack.paymentservice.command.repository;

import com.fullstack.paymentservice.command.model.PaymentMethod;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    List<PaymentMethod> findByActiveTrue();
}
