package com.fullstack.adminservice.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.dto.DashboardSummaryResponse;
import com.fullstack.adminservice.query.client.InventoryQueryClient;
import com.fullstack.adminservice.query.client.ProductQueryClient;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.product.ProductSummaryDto;
import com.fullstack.commonservice.response.ResponseData;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final ProductQueryClient productQueryClient;
    private final InventoryQueryClient inventoryQueryClient;

    public AdminDashboardController(ProductQueryClient productQueryClient,
            InventoryQueryClient inventoryQueryClient) {
        this.productQueryClient = productQueryClient;
        this.inventoryQueryClient = inventoryQueryClient;
    }

    @GetMapping("/dashboard")
    public ResponseData<DashboardSummaryResponse> dashboard() {
        List<ProductSummaryDto> products = await(productQueryClient.findAll(0, 100));
        List<InventoryItemDto> inventoryItems = await(inventoryQueryClient.findAll());

        return new ResponseData<>("SUCCESS", "Lấy dashboard thành công",
                new DashboardSummaryResponse(products.size(), products, inventoryItems.size(), inventoryItems));
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
