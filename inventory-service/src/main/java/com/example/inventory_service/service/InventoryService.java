package com.example.inventory_service.service;

import com.example.commons.dto.InventoryRequestDTO;
import com.example.commons.dto.InventoryResponseDTO;
import com.example.commons.dto.enums.InventoryStatus;
import com.example.inventory_service.entity.Product;
import com.example.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "inventory-commands", groupId = "inventory-group")
    @Transactional
    public void processInventory(InventoryRequestDTO requestDTO){
        log.info("Received inventory request for Order ID: {}", requestDTO.orderId());

        // 1. Check if we have the product and enough stock

        // This single line is now 100% thread-safe and atomic at the database level!
        int updatedRows = productRepository.reserveStock(requestDTO.productId(), requestDTO.quantity());

        InventoryStatus status;
        if (updatedRows > 0) {
            status = InventoryStatus.RESERVED;
            log.info("Stock reserved for Order ID: {}", requestDTO.orderId());
        } else {
            status = InventoryStatus.REJECTED;
            log.warn("Stock rejected for Order ID: {}", requestDTO.orderId());
        }

        InventoryResponseDTO response = new InventoryResponseDTO(requestDTO.orderId(), status);
        kafkaTemplate.send("inventory-events", response);
    }
}
