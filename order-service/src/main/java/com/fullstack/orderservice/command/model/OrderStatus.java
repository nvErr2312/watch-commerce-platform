package com.fullstack.orderservice.command.model;

public enum OrderStatus {
    PENDING_SHIPPING_FEE,
    PENDING_PAYMENT,
    SHIPPING_CREATED,
    COMPLETED,
    CANCELLED
}
