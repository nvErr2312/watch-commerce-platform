package com.fullstack.productservice.query.projection;

import java.util.List;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryListResult;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductCreatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductDeletedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductPriceChangedEvent;
import com.fullstack.commonservice.bmad.nguoi3.event.product.ProductUpdatedEvent;
import com.fullstack.commonservice.bmad.nguoi3.query.product.FindAllProductSummariesQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.product.FindAllProductsQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.product.FindProductByIdQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.product.SearchProductQuery;
import com.fullstack.productservice.dto.ProductListResult;
import com.fullstack.productservice.dto.ProductResponse;
import com.fullstack.productservice.exception.ProductNotFoundException;
import com.fullstack.productservice.query.entity.ProductView;
import com.fullstack.productservice.query.repository.ProductViewRepository;

@Component
public class ProductProjection {

    private final ProductViewRepository repository;

    public ProductProjection(ProductViewRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductView view = new ProductView();
        view.setId(event.getProductId());
        view.setName(event.getName());
        view.setBrand(event.getBrand());
        view.setCategory(event.getCategory());
        view.setDescription(event.getDescription());
        view.setPrice(event.getPrice());
        view.setImageUrl(event.getImageUrl());
        view.setDeleted(false);
        view.setCreatedAt(event.getCreatedAt());
        view.setUpdatedAt(event.getCreatedAt());

        repository.save(view);
    }

    @EventHandler
    public void on(ProductUpdatedEvent event) {
        repository.findById(event.getProductId()).ifPresent(view -> {
            view.setName(event.getName());
            view.setBrand(event.getBrand());
            view.setCategory(event.getCategory());
            view.setDescription(event.getDescription());
            view.setImageUrl(event.getImageUrl());
            view.setUpdatedAt(event.getUpdatedAt());
            repository.save(view);
        });
    }

    @EventHandler
    public void on(ProductPriceChangedEvent event) {
        repository.findById(event.getProductId()).ifPresent(view -> {
            view.setPrice(event.getNewPrice());
            view.setUpdatedAt(event.getChangedAt());
            repository.save(view);
        });
    }

    @EventHandler
    public void on(ProductDeletedEvent event) {
        repository.findById(event.getProductId()).ifPresent(view -> {
            view.setDeleted(true);
            view.setUpdatedAt(event.getDeletedAt());
            repository.save(view);
        });
    }

    @QueryHandler
    public ProductView handle(FindProductByIdQuery query) {
        return repository.findById(query.getProductId())
                .filter(view -> !view.isDeleted())
                .orElseThrow(() -> new ProductNotFoundException(query.getProductId()));
    }

    @QueryHandler
    public ProductListResult handle(FindAllProductsQuery query) {
        Pageable pageable = PageRequest.of(Math.max(query.getPage(), 0), query.getSize() > 0 ? query.getSize() : 20);
        List<ProductResponse> products = repository.findByDeletedFalse(pageable).getContent()
                .stream().map(this::toResponse).toList();
        return new ProductListResult(products);
    }

    @QueryHandler
    public ProductListResult handle(SearchProductQuery query) {
        Pageable pageable = PageRequest.of(Math.max(query.getPage(), 0), query.getSize() > 0 ? query.getSize() : 20);
        String keyword = query.getKeyword() == null ? "" : query.getKeyword();

        List<ProductResponse> products = repository
                .findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndBrandContainingIgnoreCase(
                        keyword, keyword, pageable)
                .getContent()
                .stream().map(this::toResponse).toList();
        return new ProductListResult(products);
    }

    /**
     * Dedicated query type (not an overload of FindAllProductsQuery) so Admin
     * Service can request the shared DTO response type over Axon's distributed
     * query bus without ambiguity between two handlers on the same query class.
     */
    @QueryHandler
    public ProductSummaryListResult handle(FindAllProductSummariesQuery query) {
        Pageable pageable = PageRequest.of(Math.max(query.getPage(), 0), query.getSize() > 0 ? query.getSize() : 20);
        List<ProductSummaryDto> summaries = repository.findByDeletedFalse(pageable).getContent()
                .stream().map(this::toSummary).toList();
        return new ProductSummaryListResult(summaries);
    }

    private ProductSummaryDto toSummary(ProductView view) {
        return new ProductSummaryDto(
                view.getId(), view.getName(), view.getBrand(), view.getCategory(),
                view.getPrice(), view.getImageUrl());
    }

    private ProductResponse toResponse(ProductView view) {
        return new ProductResponse(
                view.getId(), view.getName(), view.getBrand(), view.getCategory(), view.getDescription(),
                view.getPrice(), view.getImageUrl(), view.isDeleted(), view.getCreatedAt(), view.getUpdatedAt());
    }
}
