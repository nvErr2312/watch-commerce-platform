package com.fullstack.inventoryservice.command.aggregate;

import java.time.Instant;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fullstack.commonservice.bmad.nguoi3.command.inventory.CreateInventoryCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.DeductStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.ReleaseStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.ReserveStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.InventoryCreatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockDeductedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReleasedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.inventory.StockReservedEvent;

class InventoryAggregateTest {

    private static final String INVENTORY_ID = "inv-1";
    private static final String PRODUCT_ID = "prod-1";
    private static final String ORDER_ID = "order-1";

    private FixtureConfiguration<InventoryAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(InventoryAggregate.class);
        // Aggregate stamps events with Instant.now(), which is non-deterministic —
        // exclude Instant-typed fields from the expectEvents equality check.
        fixture.registerFieldFilter(field -> !Instant.class.equals(field.getType()));
    }

    @Test
    void createsInventory() {
        fixture.givenNoPriorActivity()
                .when(new CreateInventoryCommand(INVENTORY_ID, PRODUCT_ID, 10))
                .expectEvents(new InventoryCreatedEvent(INVENTORY_ID, PRODUCT_ID, 10, null));
    }

    @Test
    void reservesStockWhenEnoughAvailable() {
        fixture.given(new InventoryCreatedEvent(INVENTORY_ID, PRODUCT_ID, 10, null))
                .when(new ReserveStockCommand(INVENTORY_ID, ORDER_ID, 5))
                .expectEvents(new StockReservedEvent(INVENTORY_ID, PRODUCT_ID, ORDER_ID, 5, null));
    }

    @Test
    void rejectsReserveWhenNotEnoughStock() {
        fixture.given(new InventoryCreatedEvent(INVENTORY_ID, PRODUCT_ID, 10, null))
                .when(new ReserveStockCommand(INVENTORY_ID, ORDER_ID, 11))
                .expectException(IllegalArgumentException.class);
    }

    @Test
    void rejectsSecondReserveThatWouldOversell() {
        // Two ReserveStockCommand racing on the same aggregate must not both succeed
        // once combined reservation exceeds stock (NFR4) — Axon serializes commands
        // per aggregate instance, so this sequential test models that guarantee.
        fixture.given(
                new InventoryCreatedEvent(INVENTORY_ID, PRODUCT_ID, 10, null),
                new StockReservedEvent(INVENTORY_ID, PRODUCT_ID, "order-0", 8, null))
                .when(new ReserveStockCommand(INVENTORY_ID, ORDER_ID, 5))
                .expectException(IllegalArgumentException.class);
    }

    @Test
    void deductsReservedStock() {
        fixture.given(
                new InventoryCreatedEvent(INVENTORY_ID, PRODUCT_ID, 10, null),
                new StockReservedEvent(INVENTORY_ID, PRODUCT_ID, ORDER_ID, 5, null))
                .when(new DeductStockCommand(INVENTORY_ID, ORDER_ID, 5))
                .expectEvents(new StockDeductedEvent(INVENTORY_ID, PRODUCT_ID, ORDER_ID, 5, null));
    }

    @Test
    void releasesReservedStockOnCompensation() {
        fixture.given(
                new InventoryCreatedEvent(INVENTORY_ID, PRODUCT_ID, 10, null),
                new StockReservedEvent(INVENTORY_ID, PRODUCT_ID, ORDER_ID, 5, null))
                .when(new ReleaseStockCommand(INVENTORY_ID, ORDER_ID, 5))
                .expectEvents(new StockReleasedEvent(INVENTORY_ID, PRODUCT_ID, ORDER_ID, 5, null));
    }
}
