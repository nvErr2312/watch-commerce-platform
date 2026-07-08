package com.fullstack.adminservice.query.client;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemListResult;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindAllInventoryItemsQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryItemByProductIdQuery;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;

/**
 * productId here is Long (Inventory Service's checkout-reservation catalog,
 * owned by Nguoi 4) - NOT the same identity as Product Service's String/UUID
 * productId. See InventoryItemDto Javadoc.
 */
@Slf4j
@Component
public class InventoryQueryClient {

    private final QueryGateway queryGateway;

    public InventoryQueryClient(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackFindAll")
    @TimeLimiter(name = "inventoryService")
    public CompletableFuture<List<InventoryItemDto>> findAll() {
        return queryGateway
                .query(new FindAllInventoryItemsQuery(), ResponseTypes.instanceOf(InventoryItemListResult.class))
                .thenApply(InventoryItemListResult::getItems);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackFindByProductId")
    @TimeLimiter(name = "inventoryService")
    public CompletableFuture<InventoryItemDto> findByProductId(Long productId) {
        return queryGateway.query(
                new FindInventoryItemByProductIdQuery(productId),
                ResponseTypes.instanceOf(InventoryItemDto.class));
    }

    private CompletableFuture<List<InventoryItemDto>> fallbackFindAll(Throwable ex) {
        log.warn("Inventory Service không phản hồi, trả danh sách rỗng: {}", ex.getMessage());
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    private CompletableFuture<InventoryItemDto> fallbackFindByProductId(Long productId, Throwable ex) {
        log.warn("Inventory Service không phản hồi cho productId {}: {}", productId, ex.getMessage());
        return CompletableFuture.completedFuture(new InventoryItemDto(productId, 0));
    }
}
