package com.fullstack.adminservice.query.client;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryListResult;
import com.fullstack.commonservice.bmad.nguoi3.query.product.FindAllProductSummariesQuery;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProductQueryClient {

    private final QueryGateway queryGateway;

    public ProductQueryClient(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackFindAll")
    @TimeLimiter(name = "productService")
    public CompletableFuture<List<ProductSummaryDto>> findAll(int page, int size) {
        return queryGateway.query(
                new FindAllProductSummariesQuery(page, size),
                ResponseTypes.instanceOf(ProductSummaryListResult.class))
                .thenApply(ProductSummaryListResult::getProducts);
    }

    private CompletableFuture<List<ProductSummaryDto>> fallbackFindAll(int page, int size, Throwable ex) {
        log.warn("Product Service không phản hồi, trả danh sách rỗng cho dashboard: {}", ex.getMessage());
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
}
