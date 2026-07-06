package com.fullstack.adminservice.controller;

import java.util.concurrent.ExecutionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseData<InventorySummaryDto> get(@PathVariable String productId)
            throws ExecutionException, InterruptedException {
        return new ResponseData<>("SUCCESS", "Lấy tồn kho thành công",
                inventoryQueryClient.findByProductId(productId).get());
    }

    @PutMapping("/{productId}")
    public ResponseData<String> adjust(@PathVariable String productId, @RequestBody int newStockQuantity)
            throws ExecutionException, InterruptedException {
        InventorySummaryDto inventory = inventoryQueryClient.findByProductId(productId).get();
        if (inventory.getInventoryId() == null) {
            throw new IllegalArgumentException("Không tìm thấy tồn kho cho sản phẩm: " + productId);
        }

        commandGateway.send(new AdjustStockCommand(inventory.getInventoryId(), newStockQuantity)).get();

        return new ResponseData<>("SUCCESS", "Cập nhật tồn kho thành công", productId);
    }
}
