package com.fullstack.inventoryservice.query.projection;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.event.inventory.InventoryCreatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockAdjustedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockDeductedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReleasedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReservedEvent;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.CheckStockAvailabilityQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryByProductIdQuery;
import com.fullstack.inventoryservice.exception.InventoryNotFoundException;
import com.fullstack.inventoryservice.query.entity.InventoryView;
import com.fullstack.inventoryservice.query.repository.InventoryViewRepository;

@Component
public class InventoryProjection {

    private final InventoryViewRepository repository;

    public InventoryProjection(InventoryViewRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(InventoryCreatedEvent event) {
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
}
