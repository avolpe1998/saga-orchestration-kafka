package com.example.order_service.service;

import com.example.commons.dto.*;
import com.example.commons.dto.enums.InventoryStatus;
import com.example.commons.dto.enums.PaymentStatus;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // --- STEP 1: START THE SAGA ---
    @Transactional
    public Order createOrder(Long productId, Integer quantity, Double price) {
        // 1. Save order as PENDING
        Order order = Order.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        log.info("Saga Started: Order {} created as PENDING", order.getId());

        // 2. Command Inventory Worker
        InventoryRequestDTO inventoryCmd = new InventoryRequestDTO(order.getId(), productId, quantity);
        kafkaTemplate.send("inventory-commands", inventoryCmd);

        return order;
    }

    // --- STEP 2: HANDLE INVENTORY REPLY ---
    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void handleInventoryResponse(InventoryResponseDTO responseDTO){
        Order order = orderRepository.findById(responseDTO.orderId()).orElseThrow();

        if(responseDTO.inventoryStatus() == InventoryStatus.RESERVED){
            log.info("Saga Progress: Inventory reserved for Order {}. Commanding Payment.", order.getId());
            // Inventory succeeded -> Command Payment Worker
            PaymentRequestDTO paymentCmd = new PaymentRequestDTO(order.getId(), order.getPrice());
            kafkaTemplate.send("payment-commands", paymentCmd);
        } else {
            log.warn("Saga Failed: Inventory rejected for Order {}. Rejecting Order.", order.getId());
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
        }
    }

    // --- STEP 3: HANDLE PAYMENT REPLY ---
    @KafkaListener(topics = "payment-events", groupId = "payment-group")
    public void handlePaymentResponse(PaymentResponseDTO paymentResponseDTO){
        Order order = orderRepository.findById(paymentResponseDTO.orderId()).orElseThrow();

        OrderStatus orderStatus;
        if(paymentResponseDTO.status() == PaymentStatus.APPROVED){
            log.info("Saga Completed: Payment approved for Order {}. Order APPROVED.", order.getId());
            // Payment succeeded -> End Saga with Success
            orderStatus = OrderStatus.APPROVED;
        } else {
            log.warn("Saga Failed: Payment rejected for Order {}. Triggering Compensation.", order.getId());
            // Payment failed -> End Saga with Rejection
            orderStatus = OrderStatus.REJECTED;

            // COMPENSATING TRANSACTION
            ReleaseStockDTO releaseStockDTO = new ReleaseStockDTO(order.getId(), order.getProductId(), order.getQuantity());
            kafkaTemplate.send("inventory-rollbacks", releaseStockDTO);
        }

        order.setStatus(orderStatus);
        orderRepository.save(order);
    }
}
