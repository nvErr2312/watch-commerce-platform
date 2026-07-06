package com.fullstack.adminservice.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.adminservice.dto.DashboardSummaryResponse;
import com.fullstack.adminservice.dto.ProductInventoryView;
import com.fullstack.adminservice.query.client.InventoryQueryClient;
import com.fullstack.adminservice.query.client.ProductQueryClient;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventorySummaryDto;
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
    public ResponseData<DashboardSummaryResponse> dashboard() throws ExecutionException, InterruptedException {
        List<ProductSummaryDto> products = productQueryClient.findAll(0, 100).get();

        List<ProductInventoryView> views = products.stream()
                .map(this::withInventory)
                .toList();

        return new ResponseData<>("SUCCESS", "Lấy dashboard thành công",
                new DashboardSummaryResponse(views.size(), views));
    }

    private ProductInventoryView withInventory(ProductSummaryDto product) {
        InventorySummaryDto inventory;
        try {
            inventory = inventoryQueryClient.findByProductId(product.getProductId()).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            inventory = new InventorySummaryDto(null, product.getProductId(), 0, 0, 0);
        }

        return new ProductInventoryView(
                product.getProductId(), product.getName(), product.getBrand(), product.getCategory(),
                product.getPrice(), product.getImageUrl(),
                inventory.getStockQuantity(), inventory.getReservedQuantity(), inventory.getAvailableQuantity());
    }
}
