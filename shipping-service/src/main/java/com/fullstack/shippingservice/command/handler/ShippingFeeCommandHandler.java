package com.fullstack.shippingservice.command.handler;

import com.fullstack.commonservice.shipping.command.CalculateShippingFeeCommand;
import com.fullstack.commonservice.shipping.event.ShippingFeeCalculatedEvent;
import com.fullstack.shippingservice.command.model.ShippingFeeCalculation;
import com.fullstack.shippingservice.command.model.ShippingFeeRule;
import com.fullstack.shippingservice.command.repository.ShippingFeeCalculationRepository;
import com.fullstack.shippingservice.command.repository.ShippingFeeRuleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingFeeCommandHandler {
    private final EventGateway eventGateway;
    private final ShippingFeeCalculationRepository repository;
    private final ShippingFeeRuleRepository ruleRepository;

    @CommandHandler
    public void handle(CalculateShippingFeeCommand command) {
        ShippingFeeCalculation calculation = repository.findById(command.getOrderId()).orElseGet(() -> {
            BigDecimal shippingFee = feeFor(command.getShippingAddress());
            ShippingFeeCalculation record = new ShippingFeeCalculation();
            record.setOrderId(command.getOrderId());
            record.setSubtotalAmount(command.getSubtotalAmount());
            record.setShippingFee(shippingFee);
            record.setTotalAmount(command.getSubtotalAmount().add(shippingFee));
            record.setShippingAddress(command.getShippingAddress());
            record.setCreatedAt(Instant.now());
            return repository.save(record);
        });
        eventGateway.publish(new ShippingFeeCalculatedEvent(
                command.getOrderId(),
                calculation.getSubtotalAmount(),
                calculation.getShippingFee(),
                calculation.getTotalAmount()));
    }

    private BigDecimal feeFor(String shippingAddress) {
        String address = shippingAddress == null ? "" : shippingAddress.toUpperCase(Locale.ROOT);
        return ruleRepository.findAll().stream()
                .filter(rule -> !"DEFAULT".equals(rule.getRegionCode()))
                .filter(rule -> address.contains(rule.getRegionCode())
                        || address.contains(rule.getRegionName().toUpperCase(Locale.ROOT)))
                .findFirst()
                .or(() -> ruleRepository.findById("DEFAULT"))
                .map(ShippingFeeRule::getFee)
                .orElse(BigDecimal.valueOf(45000));
    }
}
