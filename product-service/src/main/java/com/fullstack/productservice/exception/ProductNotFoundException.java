package com.fullstack.productservice.exception;

public class ProductNotFoundException extends IllegalArgumentException {

    public ProductNotFoundException(String productId) {
        super("Không tìm thấy sản phẩm với id: " + productId);
    }
}
