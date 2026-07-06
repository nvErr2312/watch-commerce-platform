package com.fullstack.adminservice.query.client;

import java.util.concurrent.CompletableFuture;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventorySummaryDto;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryByProductIdQuery;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InventoryQueryClient {

    private final QueryGateway queryGateway;

    public InventoryQueryClient(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackFindByProductId")
    @TimeLimiter(name = "inventoryService")
    public CompletableFuture<InventorySummaryDto> findByProductId(String productId) {
        return queryGateway.query(
                new FindInventoryByProductIdQuery(productId),
                ResponseTypes.instanceOf(InventorySummaryDto.class));
    }

    private CompletableFuture<InventorySummaryDto> fallbackFindByProductId(String productId, Throwable ex) {
        log.warn("Inventory Service không phản hồi cho sản phẩm {}: {}", productId, ex.getMessage());
        return CompletableFuture.completedFuture(new InventorySummaryDto(null, productId, 0, 0, 0));
    }
}
