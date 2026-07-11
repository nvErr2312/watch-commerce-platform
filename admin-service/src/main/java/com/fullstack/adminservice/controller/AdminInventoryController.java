package com.fullstack.adminservice.controller;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.dto.InventoryAdjustmentRequest;
import com.fullstack.adminservice.query.client.InventoryQueryClient;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.inventory.command.AdjustInventoryCommand;
import com.fullstack.commonservice.response.ResponseData;

/**
 * Read-only: Inventory Service's Command side (reserve/release for checkout)
 * is owned by Nguoi 4 and has no "manual admin adjust" handler, so that
 * capability (FR18) has no home right now - flagged for the team, not
 * silently faked here.
 */
@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {

    private final InventoryQueryClient inventoryQueryClient;
    private final CommandGateway commandGateway;

    public AdminInventoryController(InventoryQueryClient inventoryQueryClient, CommandGateway commandGateway) {
        this.inventoryQueryClient = inventoryQueryClient;
        this.commandGateway = commandGateway;
    }

    @GetMapping
    public ResponseData<List<InventoryItemDto>> list() {
        return new ResponseData<>("SUCCESS", "Lấy danh sách tồn kho thành công", await(inventoryQueryClient.findAll()));
    }

    @GetMapping("/{productId}")
    public ResponseData<InventoryItemDto> get(@PathVariable String productId) {
        return new ResponseData<>("SUCCESS", "Lấy tồn kho thành công", await(inventoryQueryClient.findByProductId(productId)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ResponseData<String>> adjust(
            @PathVariable UUID productId, @Valid @RequestBody InventoryAdjustmentRequest request) {
        try {
            commandGateway.send(new AdjustInventoryCommand(productId, request.getAvailableQuantity())).get();
            return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Cập nhật tồn kho thành công", productId.toString()));
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
