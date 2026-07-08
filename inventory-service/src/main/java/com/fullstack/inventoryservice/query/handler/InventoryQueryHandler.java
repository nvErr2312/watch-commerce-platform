package com.fullstack.inventoryservice.query.handler;

import java.util.List;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemListResult;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindAllInventoryItemsQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryItemByProductIdQuery;
import com.fullstack.inventoryservice.command.model.InventoryItem;
import com.fullstack.inventoryservice.command.repository.InventoryItemRepository;
import com.fullstack.inventoryservice.exception.InventoryNotFoundException;

/**
 * Read-only queries against InventoryItemRepository (Nguoi 4's Command-side
 * table). There is no separate read-model/projection here - Command and
 * Query share the same Postgres table, since the Command side isn't event
 * sourced (see InventoryCommandHandler: plain @Transactional read-modify-write,
 * not an Axon @Aggregate).
 */
@Component
public class InventoryQueryHandler {

    private final InventoryItemRepository repository;

    public InventoryQueryHandler(InventoryItemRepository repository) {
        this.repository = repository;
    }

    @QueryHandler
    public InventoryItemDto handle(FindInventoryItemByProductIdQuery query) {
        InventoryItem item = repository.findById(query.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException(String.valueOf(query.getProductId())));
        return toDto(item);
    }

    @QueryHandler
    public InventoryItemListResult handle(FindAllInventoryItemsQuery query) {
        List<InventoryItemDto> items = repository.findAll().stream().map(this::toDto).toList();
        return new InventoryItemListResult(items);
    }

    private InventoryItemDto toDto(InventoryItem item) {
        return new InventoryItemDto(item.getProductId(), item.getAvailableQuantity());
    }
}
