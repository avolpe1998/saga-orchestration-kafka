package com.example.commons.dto;

import com.example.commons.dto.enums.InventoryStatus;

public record InventoryResponseDTO(
        Long orderId,
        InventoryStatus inventoryStatus
) {}
