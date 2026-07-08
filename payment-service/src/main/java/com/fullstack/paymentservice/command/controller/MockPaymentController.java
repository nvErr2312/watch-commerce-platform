package com.fullstack.paymentservice.command.controller;

import com.fullstack.paymentservice.command.model.PaymentMethod;
import com.fullstack.paymentservice.command.repository.PaymentMethodRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MockPaymentController {
    private final PaymentMethodRepository repository;

    @GetMapping("/api/mock/payment-methods")
    public List<PaymentMethod> paymentMethods() {
        return repository.findByActiveTrue();
    }
}
