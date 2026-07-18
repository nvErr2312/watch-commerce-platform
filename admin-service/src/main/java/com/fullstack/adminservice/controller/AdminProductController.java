package com.fullstack.adminservice.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.validation.Valid;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.dto.AdminProductRequest;
import com.fullstack.adminservice.query.client.ProductQueryClient;
import com.fullstack.commonservice.bmad.nguoi3.command.product.ChangePriceCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.CreateProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.DeleteProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.UpdateProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryDto;
import com.fullstack.commonservice.response.ResponseData;

/**
 * Proxies admin product management onto the same Command/Query bus Product
 * Service listens on (FR14) - Admin Service never touches Product Service's
 * database directly (FR35).
 */
@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {

    private final CommandGateway commandGateway;
    private final ProductQueryClient productQueryClient;

    public AdminProductController(CommandGateway commandGateway, ProductQueryClient productQueryClient) {
        this.commandGateway = commandGateway;
        this.productQueryClient = productQueryClient;
    }

    @GetMapping
    public ResponseData<List<ProductSummaryDto>> list() {
        return new ResponseData<>("SUCCESS", "Lấy danh sách sản phẩm thành công",
                await(productQueryClient.findAll(0, 100)));
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> create(@Valid @RequestBody AdminProductRequest request) {
        String productId = UUID.randomUUID().toString();

        sendAndWait(new CreateProductCommand(
                productId, request.getName(), request.getBrand(), request.getCategory(),
                request.getDescription(), request.getPrice(), request.getImageUrl()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>("SUCCESS", "Tạo sản phẩm thành công", productId));
    }

    @PutMapping("/{id}")
    public ResponseData<String> update(@PathVariable("id") String productId,
            @Valid @RequestBody AdminProductRequest request) {
        sendAndWait(new UpdateProductCommand(
                productId, request.getName(), request.getBrand(), request.getCategory(),
                request.getDescription(), request.getImageUrl()));

        return new ResponseData<>("SUCCESS", "Cập nhật sản phẩm thành công", productId);
    }

    @PutMapping("/{id}/price")
    public ResponseData<String> changePrice(@PathVariable("id") String productId, @RequestBody BigDecimal newPrice) {
        sendAndWait(new ChangePriceCommand(productId, newPrice));

        return new ResponseData<>("SUCCESS", "Cập nhật giá thành công", productId);
    }

    @DeleteMapping("/{id}")
    public ResponseData<String> delete(@PathVariable("id") String productId) {
        sendAndWait(new DeleteProductCommand(productId));

        return new ResponseData<>("SUCCESS", "Xóa sản phẩm thành công", productId);
    }

    private void sendAndWait(Object command) {
        try {
            commandGateway.send(command).get();
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
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
