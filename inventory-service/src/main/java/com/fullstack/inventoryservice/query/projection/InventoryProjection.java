package com.fullstack.inventoryservice.query.projection;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventorySummaryDto;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.InventoryCreatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockAdjustedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockDeductedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReleasedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReservedEvent;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.CheckStockAvailabilityQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryByProductIdQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventorySummaryByProductIdQuery;
import com.fullstack.inventoryservice.exception.InventoryNotFoundException;
import com.fullstack.inventoryservice.query.entity.InventoryView;
import com.fullstack.inventoryservice.query.repository.InventoryViewRepository;

@Slf4j
@Component
public class InventoryProjection {

    private final InventoryViewRepository repository;

    public InventoryProjection(InventoryViewRepository repository) {
        this.repository = repository;
    }

    /**
     * Idempotent by design: the read-model is rebuilt from scratch (H2 in-memory)
     * every service restart by replaying the full event history from Axon Server
     * (which never resets). If the event log ever contains two InventoryCreatedEvent
     * for the same productId (e.g. from repeated dev/test runs), a hard insert would
     * violate the unique constraint on product_id and PERMANENTLY stall the
     * TrackingEventProcessor for this projection - every event after that point,
     * including brand new ones, would silently never reach the read model. Skipping
     * (with a warning) keeps replay resilient instead of wedging the whole projection.
     */
    @EventHandler
    public void on(InventoryCreatedEvent event) {
        if (repository.findByProductId(event.getProductId()).isPresent()) {
            log.warn("Bỏ qua InventoryCreatedEvent trùng cho productId {} (inventoryId {}) - đã có tồn kho",
                    event.getProductId(), event.getInventoryId());
            return;
        }

        InventoryView view = new InventoryView();
        view.setId(event.getInventoryId());
        view.setProductId(event.getProductId());
        view.setStockQuantity(event.getInitialQuantity());
        view.setReservedQuantity(0);
        view.setUpdatedAt(event.getCreatedAt());

        repository.save(view);
    }

    @EventHandler
    public void on(StockReservedEvent event) {
        repository.findById(event.getInventoryId()).ifPresent(view -> {
            view.setReservedQuantity(view.getReservedQuantity() + event.getQuantity());
            view.setUpdatedAt(event.getReservedAt());
            repository.save(view);
        });
    }

    @EventHandler
    public void on(StockDeductedEvent event) {
        repository.findById(event.getInventoryId()).ifPresent(view -> {
            view.setStockQuantity(view.getStockQuantity() - event.getQuantity());
            view.setReservedQuantity(view.getReservedQuantity() - event.getQuantity());
            view.setUpdatedAt(event.getDeductedAt());
            repository.save(view);
        });
    }

    @EventHandler
    public void on(StockReleasedEvent event) {
        repository.findById(event.getInventoryId()).ifPresent(view -> {
            view.setReservedQuantity(view.getReservedQuantity() - event.getQuantity());
            view.setUpdatedAt(event.getReleasedAt());
            repository.save(view);
        });
    }

    @EventHandler
    public void on(StockAdjustedEvent event) {
        repository.findById(event.getInventoryId()).ifPresent(view -> {
            view.setStockQuantity(event.getNewStockQuantity());
            view.setUpdatedAt(event.getAdjustedAt());
            repository.save(view);
        });
    }

    @QueryHandler
    public InventoryView handle(FindInventoryByProductIdQuery query) {
        return repository.findByProductId(query.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException(query.getProductId()));
    }

    @QueryHandler
    public boolean handle(CheckStockAvailabilityQuery query) {
        return repository.findByProductId(query.getProductId())
                .map(view -> (view.getStockQuantity() - view.getReservedQuantity()) >= query.getRequestedQuantity())
                .orElse(false);
    }

    /**
     * Dedicated query type (not an overload of FindInventoryByProductIdQuery)
     * so Admin Service can request the shared DTO response type over Axon's
     * distributed query bus without ambiguity between two handlers on the
     * same query class.
     */
    @QueryHandler
    public InventorySummaryDto handle(FindInventorySummaryByProductIdQuery query) {
        InventoryView view = repository.findByProductId(query.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException(query.getProductId()));
        return new InventorySummaryDto(
                view.getId(), view.getProductId(), view.getStockQuantity(), view.getReservedQuantity(),
                view.getStockQuantity() - view.getReservedQuantity());
    }
}
