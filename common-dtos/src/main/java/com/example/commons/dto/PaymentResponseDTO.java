package com.example.commons.dto;

import com.example.commons.dto.enums.PaymentStatus;

public record PaymentResponseDTO(
        Long orderId,
        PaymentStatus status
) {
}
