package com.example.commons.dto;

public record ReleaseStockDTO(
        Long orderId,
        Long productId,
        Integer quantity
) {}
