package com.fullstack.inventoryservice.query.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemDto;
import com.fullstack.commonservice.bmad.nguoi3.dto.inventory.InventoryItemListResult;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindAllInventoryItemsQuery;
import com.fullstack.commonservice.bmad.nguoi3.query.inventory.FindInventoryItemByProductIdQuery;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.inventoryservice.common.AxonExceptions;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryQueryController {

    private final QueryGateway queryGateway;

    public InventoryQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{productId}")
    public ResponseData<InventoryItemDto> getByProductId(@PathVariable String productId) {
        InventoryItemDto item = await(queryGateway.query(
                new FindInventoryItemByProductIdQuery(productId), ResponseTypes.instanceOf(InventoryItemDto.class)));

        return new ResponseData<>("SUCCESS", "Lấy tồn kho thành công", item);
    }

    @GetMapping
    public ResponseData<List<InventoryItemDto>> getAll() {
        InventoryItemListResult result = await(queryGateway.query(
                new FindAllInventoryItemsQuery(), ResponseTypes.instanceOf(InventoryItemListResult.class)));

        return new ResponseData<>("SUCCESS", "Lấy danh sách tồn kho thành công", result.getItems());
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
