package com.example.order_service.entity;

public enum OrderStatus {
    PENDING,   // Order created, waiting for saga to finish
    APPROVED,  // Inventory reserved AND payment successful
    REJECTED   // Inventory failed OR payment failed
}
