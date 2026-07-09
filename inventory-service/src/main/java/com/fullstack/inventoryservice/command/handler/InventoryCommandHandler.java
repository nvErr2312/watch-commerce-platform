package com.fullstack.inventoryservice.command.handler;

import com.fullstack.commonservice.inventory.command.ReleaseInventoryCommand;
import com.fullstack.commonservice.inventory.command.ReserveInventoryCommand;
import com.fullstack.commonservice.inventory.event.InventoryReserveFailedEvent;
import com.fullstack.commonservice.inventory.event.InventoryReleasedEvent;
import com.fullstack.commonservice.inventory.event.InventoryReservedEvent;
import com.fullstack.commonservice.order.OrderItemPayload;
import com.fullstack.inventoryservice.command.model.InventoryItem;
import com.fullstack.inventoryservice.command.model.InventoryReservation;
import com.fullstack.inventoryservice.command.model.ReservationStatus;
import com.fullstack.inventoryservice.command.repository.InventoryItemRepository;
import com.fullstack.inventoryservice.command.repository.InventoryReservationRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryCommandHandler {
    private final InventoryReservationRepository repository;
    private final InventoryItemRepository inventoryItemRepository;
    private final EventGateway eventGateway;

    @CommandHandler
    @Transactional
    public void handle(ReserveInventoryCommand command) {
        Map<String, Integer> requestedQuantityByProduct = requestedQuantityByProduct(command);
        if (requestedQuantityByProduct.isEmpty()) {
            eventGateway.publish(new InventoryReserveFailedEvent(command.getOrderId(), "Inventory items are empty"));
            return;
        }
        if (repository.existsById(command.getOrderId())) {
            publishReserved(command);
            return;
        }

        Map<InventoryItem, Integer> lockedItems = new HashMap<>();
        for (Map.Entry<String, Integer> requested : requestedQuantityByProduct.entrySet()) {
            InventoryItem item = inventoryItemRepository.findByIdForUpdate(requested.getKey()).orElse(null);
            if (item == null || item.getAvailableQuantity() < requested.getValue()) {
                eventGateway.publish(new InventoryReserveFailedEvent(command.getOrderId(),
                        "Product " + requested.getKey() + " is out of stock"));
                return;
            }
            lockedItems.put(item, requested.getValue());
        }

        lockedItems.forEach((item, quantity) -> item.setAvailableQuantity(item.getAvailableQuantity() - quantity));
        inventoryItemRepository.saveAll(lockedItems.keySet());

        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(command.getOrderId());
        reservation.setStatus(ReservationStatus.RESERVED.name());
        reservation.setUpdatedAt(Instant.now());
        repository.save(reservation);
        publishReserved(command);
    }

    @CommandHandler
    @Transactional
    public void handle(ReleaseInventoryCommand command) {
        InventoryReservation reservation = repository.findById(command.getOrderId()).orElse(null);
        if (reservation == null || ReservationStatus.RELEASED.name().equals(reservation.getStatus())) {
            eventGateway.publish(new InventoryReleasedEvent(command.getOrderId()));
            return;
        }

        for (Map.Entry<String, Integer> requested : requestedQuantityByProduct(command.getItems()).entrySet()) {
            inventoryItemRepository.findByIdForUpdate(requested.getKey()).ifPresent(item -> {
                item.setAvailableQuantity(item.getAvailableQuantity() + requested.getValue());
                inventoryItemRepository.save(item);
            });
        }
        reservation.setStatus(ReservationStatus.RELEASED.name());
        reservation.setUpdatedAt(Instant.now());
        repository.save(reservation);
        eventGateway.publish(new InventoryReleasedEvent(command.getOrderId()));
    }

    private Map<String, Integer> requestedQuantityByProduct(ReserveInventoryCommand command) {
        Map<String, Integer> quantities = new HashMap<>();
        return requestedQuantityByProduct(command.getItems());
    }

    private Map<String, Integer> requestedQuantityByProduct(Iterable<OrderItemPayload> items) {
        Map<String, Integer> quantities = new HashMap<>();
        if (items == null) {
            return quantities;
        }
        for (OrderItemPayload item : items) {
            if (item.getProductId() != null && item.getQuantity() > 0) {
                quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            }
        }
        return quantities;
    }

    private void publishReserved(ReserveInventoryCommand command) {
        eventGateway.publish(new InventoryReservedEvent(
                command.getOrderId(),
                command.getItems(),
                command.getSubtotalAmount(),
                command.getShippingAddress()));
    }
}
