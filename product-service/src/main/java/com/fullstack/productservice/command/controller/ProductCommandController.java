package com.fullstack.productservice.command.controller;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fullstack.commonservice.bmad.nguoi3.command.product.ChangePriceCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.CreateProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.DeleteProductCommand;
import com.fullstack.commonservice.bmad.nguoi3.command.product.UpdateProductCommand;
import com.fullstack.commonservice.response.ResponseData;
import com.fullstack.productservice.dto.ChangePriceRequest;
import com.fullstack.productservice.dto.CreateProductRequest;
import com.fullstack.productservice.dto.UpdateProductRequest;

@RestController
@RequestMapping("/api/products")
public class ProductCommandController {

    private final CommandGateway commandGateway;

    public ProductCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public ResponseEntity<ResponseData<String>> createProduct(@RequestBody CreateProductRequest request)
            throws ExecutionException, InterruptedException {
        String productId = UUID.randomUUID().toString();

        commandGateway.send(new CreateProductCommand(
                productId,
                request.getName(),
                request.getBrand(),
                request.getCategory(),
                request.getDescription(),
                request.getPrice(),
                request.getImageUrl())).get();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData<>("SUCCESS", "Tạo sản phẩm thành công", productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<String>> updateProduct(
            @PathVariable("id") String productId,
            @RequestBody UpdateProductRequest request) throws ExecutionException, InterruptedException {

        commandGateway.send(new UpdateProductCommand(
                productId,
                request.getName(),
                request.getBrand(),
                request.getCategory(),
                request.getDescription(),
                request.getImageUrl())).get();

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Cập nhật sản phẩm thành công", productId));
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<ResponseData<String>> changePrice(
            @PathVariable("id") String productId,
            @RequestBody ChangePriceRequest request) throws ExecutionException, InterruptedException {

        commandGateway.send(new ChangePriceCommand(productId, request.getNewPrice())).get();

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Cập nhật giá thành công", productId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<String>> deleteProduct(@PathVariable("id") String productId)
            throws ExecutionException, InterruptedException {

        commandGateway.send(new DeleteProductCommand(productId)).get();

        return ResponseEntity.ok(new ResponseData<>("SUCCESS", "Xóa sản phẩm thành công", productId));
    }
}
