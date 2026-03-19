package com.example.commons.dto;

public record InventoryRequestDTO(
        Long orderId,
        Long productId,
        Integer quantity
) {}
