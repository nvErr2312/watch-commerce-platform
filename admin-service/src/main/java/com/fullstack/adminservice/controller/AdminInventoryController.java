package com.fullstack.adminservice.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.adminservice.common.AxonExceptions;
import com.fullstack.adminservice.query.client.InventoryQueryClient;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
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

    public AdminInventoryController(InventoryQueryClient inventoryQueryClient) {
        this.inventoryQueryClient = inventoryQueryClient;
    }

    @GetMapping
    public ResponseData<List<InventoryItemDto>> list() {
        return new ResponseData<>("SUCCESS", "Lấy danh sách tồn kho thành công", await(inventoryQueryClient.findAll()));
    }

    @GetMapping("/{productId}")
    public ResponseData<InventoryItemDto> get(@PathVariable String productId) {
        return new ResponseData<>("SUCCESS", "Lấy tồn kho thành công", await(inventoryQueryClient.findByProductId(productId)));
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
