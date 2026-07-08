package com.fullstack.inventoryservice.command.controller;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.commonservice.bmad.nguoi3.command.inventory.AdjustStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.CreateInventoryCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.DeductStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.ReleaseStockCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.inventory.ReserveStockCommand;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.inventoryservice.common.AxonExceptions;
import com.fullstack.inventoryservice.dto.AdjustStockRequest;
import com.fullstack.inventoryservice.dto.CreateInventoryRequest;
import com.fullstack.inventoryservice.dto.StockQuantityRequest;
import com.fullstack.inventoryservice.exception.InventoryNotFoundException;
import com.fullstack.inventoryservice.query.entity.InventoryView;
import com.fullstack.inventoryservice.query.repository.InventoryViewRepository;

@RestController
@RequestMapping("/api/inventory")
public class InventoryCommandController {

    private final CommandGateway commandGateway;
    private final InventoryViewRepository inventoryViewRepository;

    public InventoryCommandController(CommandGateway commandGateway,
            InventoryViewRepository inventoryViewRepository) {
        this.commandGateway = commandGateway;
        this.inventoryViewRepository = inventoryViewRepository;
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> create(@RequestBody CreateInventoryRequest request) {
        String inventoryId = UUID.randomUUID().toString();

        sendAndWait(new CreateInventoryCommand(inventoryId, request.getProductId(), request.getInitialQuantity()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>("SUCCESS", "Khởi tạo tồn kho thành công", inventoryId));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ResponseData<String>> reserve(
            @RequestParam("productId") String productId,
            @RequestBody StockQuantityRequest request) {
        String inventoryId = resolveInventoryId(productId);

        sendAndWait(new ReserveStockCommand(inventoryId, request.getOrderId(), request.getQuantity()));

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Giữ hàng thành công", inventoryId));
    }

    @PostMapping("/deduct")
    public ResponseEntity<ResponseData<String>> deduct(
            @RequestParam("productId") String productId,
            @RequestBody StockQuantityRequest request) {
        String inventoryId = resolveInventoryId(productId);

        sendAndWait(new DeductStockCommand(inventoryId, request.getOrderId(), request.getQuantity()));

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Trừ kho thành công", inventoryId));
    }

    @PostMapping("/release")
    public ResponseEntity<ResponseData<String>> release(
            @RequestParam("productId") String productId,
            @RequestBody StockQuantityRequest request) {
        String inventoryId = resolveInventoryId(productId);

        sendAndWait(new ReleaseStockCommand(inventoryId, request.getOrderId(), request.getQuantity()));

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Hoàn kho thành công", inventoryId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ResponseData<String>> adjust(
            @PathVariable String productId,
            @RequestBody AdjustStockRequest request) {
        String inventoryId = resolveInventoryId(productId);

        sendAndWait(new AdjustStockCommand(inventoryId, request.getNewStockQuantity()));

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Cập nhật tồn kho thành công", inventoryId));
    }

    private String resolveInventoryId(String productId) {
        InventoryView view = inventoryViewRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
        return view.getId();
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
}
