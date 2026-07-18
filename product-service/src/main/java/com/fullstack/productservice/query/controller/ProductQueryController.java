package com.fullstack.productservice.query.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.commonservice.bmad.nguoi3.query.product.FindAllProductsQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.product.FindProductByIdQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.product.SearchProductQuery;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.productservice.common.AxonExceptions;
import com.fullstack.productservice.dto.ProductListResult;
import com.fullstack.productservice.dto.ProductResponse;
import com.fullstack.productservice.query.entity.ProductView;

@RestController
@RequestMapping("/api/v1/products")
public class ProductQueryController {

    private final QueryGateway queryGateway;

    public ProductQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{id}")
    public ResponseData<ProductView> getById(@PathVariable("id") String productId) {
        ProductView view = await(queryGateway
                .query(new FindProductByIdQuery(productId), ResponseTypes.instanceOf(ProductView.class)));

        return new ResponseData<>("SUCCESS", "Lấy sản phẩm thành công", view);
    }

    @GetMapping
    public ResponseData<List<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductListResult result = await(queryGateway
                .query(new FindAllProductsQuery(page, size), ResponseTypes.instanceOf(ProductListResult.class)));

        return new ResponseData<>("SUCCESS", "Lấy danh sách sản phẩm thành công", result.getProducts());
    }

    @GetMapping("/search")
    public ResponseData<List<ProductResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductListResult result = await(queryGateway
                .query(new SearchProductQuery(keyword, brand, category, page, size),
                        ResponseTypes.instanceOf(ProductListResult.class)));

        return new ResponseData<>("SUCCESS", "Tìm kiếm sản phẩm thành công", result.getProducts());
    }

    private <T> T await(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
