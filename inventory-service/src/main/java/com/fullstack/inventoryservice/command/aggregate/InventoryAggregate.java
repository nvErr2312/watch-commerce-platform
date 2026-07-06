package com.fullstack.inventoryservice.command.aggregate;

import java.time.Instant;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.fullstack.commonservice.bmad.nguoi3.command.inventory.AdjustStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.CreateInventoryCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.DeductStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.ReleaseStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.ReserveStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.InventoryCreatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockAdjustedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockDeductedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReleasedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReservedEvent;

/**
 * Axon serializes commands for the same aggregateIdentifier through a single
 * thread, so reserve/deduct/release requests racing on one productId cannot
 * oversell stock (NFR4) without any manual DB locking.
 */
@Aggregate
public class InventoryAggregate {

    @AggregateIdentifier
    private String inventoryId;

    private String productId;
    private int stockQuantity;
    private int reservedQuantity;

    protected InventoryAggregate() {
        // Required by Axon for event sourcing replay
    }

    @CommandHandler
    public InventoryAggregate(CreateInventoryCommand command) {
        if (command.getInitialQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho ban đầu không được âm");
        }

        AggregateLifecycle.apply(new InventoryCreatedEvent(
                command.getInventoryId(),
                command.getProductId(),
                command.getInitialQuantity(),
                Instant.now()));
    }

    @CommandHandler
    public void handle(ReserveStockCommand command) {
        int available = stockQuantity - reservedQuantity;
        if (available < command.getQuantity()) {
            throw new IllegalArgumentException(
                    "Không đủ tồn kho để giữ hàng cho sản phẩm " + productId
                            + " (còn " + available + ", yêu cầu " + command.getQuantity() + ")");
        }

        AggregateLifecycle.apply(new StockReservedEvent(
                inventoryId, productId, command.getOrderId(), command.getQuantity(), Instant.now()));
    }

    @CommandHandler
    public void handle(DeductStockCommand command) {
        if (reservedQuantity < command.getQuantity()) {
            throw new IllegalArgumentException(
                    "Không thể trừ kho vượt quá số lượng đã giữ cho sản phẩm " + productId);
        }

        AggregateLifecycle.apply(new StockDeductedEvent(
                inventoryId, productId, command.getOrderId(), command.getQuantity(), Instant.now()));
    }

    @CommandHandler
    public void handle(ReleaseStockCommand command) {
        if (reservedQuantity < command.getQuantity()) {
            throw new IllegalArgumentException(
                    "Không thể hoàn kho vượt quá số lượng đã giữ cho sản phẩm " + productId);
        }

        AggregateLifecycle.apply(new StockReleasedEvent(
                inventoryId, productId, command.getOrderId(), command.getQuantity(), Instant.now()));
    }

    @CommandHandler
    public void handle(AdjustStockCommand command) {
        if (command.getNewStockQuantity() < reservedQuantity) {
            throw new IllegalArgumentException(
                    "Số lượng tồn kho mới không được nhỏ hơn số lượng đang giữ (" + reservedQuantity + ")");
        }

        AggregateLifecycle.apply(new StockAdjustedEvent(
                inventoryId, productId, stockQuantity, command.getNewStockQuantity(), Instant.now()));
    }

    @EventSourcingHandler
    public void on(InventoryCreatedEvent event) {
        this.inventoryId = event.getInventoryId();
        this.productId = event.getProductId();
        this.stockQuantity = event.getInitialQuantity();
        this.reservedQuantity = 0;
    }

    @EventSourcingHandler
    public void on(StockReservedEvent event) {
        this.reservedQuantity += event.getQuantity();
    }

    @EventSourcingHandler
    public void on(StockDeductedEvent event) {
        this.stockQuantity -= event.getQuantity();
        this.reservedQuantity -= event.getQuantity();
    }

    @EventSourcingHandler
    public void on(StockReleasedEvent event) {
        this.reservedQuantity -= event.getQuantity();
    }

    @EventSourcingHandler
    public void on(StockAdjustedEvent event) {
        this.stockQuantity = event.getNewStockQuantity();
    }
}
