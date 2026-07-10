package com.fullstack.productservice.command.aggregate;

import java.math.BigDecimal;
import java.time.Instant;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.fullstack.commonservice.bmad.nguoi3.command.product.ChangePriceCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.CreateProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.DeleteProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.UpdateProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductCreatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductDeletedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductPriceChangedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductUpdatedEvent;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;

    private String name;
    private String brand;
    private String category;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean deleted;

    protected ProductAggregate() {
        // Required by Axon for event sourcing replay
    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand command) {
        if (command.getPrice() == null || command.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không hợp lệ");
        }
        if (command.getName() == null || command.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }

        AggregateLifecycle.apply(new ProductCreatedEvent(
                command.getProductId(),
                command.getName(),
                command.getBrand(),
                command.getCategory(),
                command.getDescription(),
                command.getPrice(),
                command.getImageUrl(),
                Instant.now()));
    }

    @CommandHandler
    public void handle(UpdateProductCommand command) {
        if (deleted) {
            throw new IllegalArgumentException("Không thể cập nhật sản phẩm đã bị xóa");
        }

        AggregateLifecycle.apply(new ProductUpdatedEvent(
                command.getProductId(),
                command.getName(),
                command.getBrand(),
                command.getCategory(),
                command.getDescription(),
                command.getImageUrl(),
                Instant.now()));
    }

    @CommandHandler
    public void handle(ChangePriceCommand command) {
        if (deleted) {
            throw new IllegalArgumentException("Không thể đổi giá sản phẩm đã bị xóa");
        }
        if (command.getNewPrice() == null || command.getNewPrice().signum() < 0) {
            throw new IllegalArgumentException("Giá mới không hợp lệ");
        }

        AggregateLifecycle.apply(new ProductPriceChangedEvent(
                command.getProductId(),
                this.price,
                command.getNewPrice(),
                Instant.now()));
    }

    @CommandHandler
    public void handle(DeleteProductCommand command) {
        if (deleted) {
            throw new IllegalArgumentException("Sản phẩm đã bị xóa trước đó");
        }

        AggregateLifecycle.apply(new ProductDeletedEvent(command.getProductId(), Instant.now()));
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent event) {
        this.productId = event.getProductId();
        this.name = event.getName();
        this.brand = event.getBrand();
        this.category = event.getCategory();
        this.description = event.getDescription();
        this.price = event.getPrice();
        this.imageUrl = event.getImageUrl();
        this.deleted = false;
    }

    @EventSourcingHandler
    public void on(ProductUpdatedEvent event) {
        this.name = event.getName();
        this.brand = event.getBrand();
        this.category = event.getCategory();
        this.description = event.getDescription();
        this.imageUrl = event.getImageUrl();
    }

    @EventSourcingHandler
    public void on(ProductPriceChangedEvent event) {
        this.price = event.getNewPrice();
    }

    @EventSourcingHandler
    public void on(ProductDeletedEvent event) {
        this.deleted = true;
    }
}
