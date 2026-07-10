package com.fullstack.shippingservice.command.controller;

import com.fullstack.shippingservice.command.model.ShippingFeeRule;
import com.fullstack.shippingservice.command.repository.ShippingFeeRuleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShippingFeeController {
    private final ShippingFeeRuleRepository repository;

    @GetMapping("/api/shipping/fees")
    public List<ShippingFeeRule> shippingFees() {
        return repository.findAll();
    }
}
