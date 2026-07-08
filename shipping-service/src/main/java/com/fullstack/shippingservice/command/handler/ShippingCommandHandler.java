package com.fullstack.shippingservice.command.handler;

import com.fullstack.commonservice.shipping.command.CreateShippingCommand;
import com.fullstack.commonservice.shipping.event.ShippingCreatedEvent;
import com.fullstack.commonservice.shipping.event.ShippingFailedEvent;
import com.fullstack.shippingservice.command.model.Shipment;
import com.fullstack.shippingservice.command.model.ShipmentStatus;
import com.fullstack.shippingservice.command.repository.ShipmentRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingCommandHandler {
    private final ShipmentRepository repository;
    private final EventGateway eventGateway;

    @CommandHandler
    public void handle(CreateShippingCommand command) {
        if ("FAIL".equalsIgnoreCase(command.getShippingAddress())) {
            eventGateway.publish(new ShippingFailedEvent(command.getOrderId(), "Mock shipping failed"));
            return;
        }
        Shipment shipment = repository.findById(command.getOrderId()).orElseGet(() -> {
            Shipment record = new Shipment();
            record.setOrderId(command.getOrderId());
            record.setTrackingCode("MOCK-" + command.getOrderId());
            record.setStatus(ShipmentStatus.CREATED.name());
            record.setCreatedAt(Instant.now());
            return repository.save(record);
        });
        eventGateway.publish(new ShippingCreatedEvent(command.getOrderId(), shipment.getTrackingCode()));
    }
}
