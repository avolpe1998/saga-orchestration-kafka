package com.example.commons.dto;

public record PaymentRequestDTO(
        Long orderId,
        Double amount
) {}
