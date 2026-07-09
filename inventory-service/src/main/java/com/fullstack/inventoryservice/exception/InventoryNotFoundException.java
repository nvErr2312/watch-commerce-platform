package com.fullstack.inventoryservice.exception;

public class InventoryNotFoundException extends IllegalArgumentException {

    public InventoryNotFoundException(String productId) {
        super("Không tìm thấy tồn kho cho sản phẩm: " + productId);
    }
}
