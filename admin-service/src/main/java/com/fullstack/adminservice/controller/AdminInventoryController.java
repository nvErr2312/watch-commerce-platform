package com.fullstack.adminservice.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.query.client.InventoryQueryClient;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.AdjustStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventorySummaryDto;
import com.fullstack.commonservice.response.ResponseData;

/**
 * Proxies admin inventory management (FR18) - Admin Service never touches
 * Inventory Service's database directly (FR35).
 */
@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {

    private final CommandGateway commandGateway;
    private final InventoryQueryClient inventoryQueryClient;

    public AdminInventoryController(CommandGateway commandGateway, InventoryQueryClient inventoryQueryClient) {
        this.commandGateway = commandGateway;
        this.inventoryQueryClient = inventoryQueryClient;
    }

    @GetMapping("/{productId}")
    public ResponseData<InventorySummaryDto> get(@PathVariable String productId) {
        return new ResponseData<>("SUCCESS", "Lấy tồn kho thành công",
                await(inventoryQueryClient.findByProductId(productId)));
    }

    @PutMapping("/{productId}")
    public ResponseData<String> adjust(@PathVariable String productId, @RequestBody int newStockQuantity) {
        InventorySummaryDto inventory = await(inventoryQueryClient.findByProductId(productId));
        if (inventory.getInventoryId() == null) {
            throw new IllegalArgumentException("Không tìm thấy tồn kho cho sản phẩm: " + productId);
        }

        try {
            commandGateway.send(new AdjustStockCommand(inventory.getInventoryId(), newStockQuantity)).get();
        } catch (ExecutionException e) {
            throw AxonExceptions.unwrap(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }

        return new ResponseData<>("SUCCESS", "Cập nhật tồn kho thành công", productId);
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
