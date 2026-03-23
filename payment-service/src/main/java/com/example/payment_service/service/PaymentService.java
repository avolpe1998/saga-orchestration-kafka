package com.example.payment_service.service;

import com.example.commons.dto.PaymentRequestDTO;
import com.example.commons.dto.PaymentResponseDTO;
import com.example.commons.dto.enums.PaymentStatus;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final RestClient restClient = RestClient.create();

    @KafkaListener(topics = "payment-commands", groupId = "payment-group")
    public void processPayment(PaymentRequestDTO paymentRequestDTO) {
        log.info("Received payment request for Order ID: {}", paymentRequestDTO.orderId());

        PaymentStatus status;
        try {
            // Call our WireMock Bank container
            restClient.get()
                    .uri("http://localhost:8080/bank/charge?amount=" + paymentRequestDTO.amount())
                    .retrieve()
                    .toBodilessEntity(); // We just care if it returns 200 OK
            status = PaymentStatus.APPROVED;
            log.info("Payment approved for Order ID: {}", paymentRequestDTO.orderId());
        } catch (Exception e) {
            // If WireMock returns 400 Bad Request, an exception is thrown
            status = PaymentStatus.REJECTED;
            log.warn("Payment failed for Order ID: {}", paymentRequestDTO.orderId(), e);
        }

        com.example.payment_service.entity.PaymentStatus dbStatus = mapToEntityStatus(status);

        // Save to database
        Payment payment = Payment.builder()
                .orderId(paymentRequestDTO.orderId())
                .amount(paymentRequestDTO.amount())
                .status(dbStatus)
                .build();
        paymentRepository.save(payment);

        // Publish the result back to the orchestrator
        PaymentResponseDTO response = new PaymentResponseDTO(paymentRequestDTO.orderId(), status);
        kafkaTemplate.send("payment-events", response);
    }

    private com.example.payment_service.entity.PaymentStatus mapToEntityStatus(PaymentStatus status) {
        if (status == PaymentStatus.APPROVED) {
            return com.example.payment_service.entity.PaymentStatus.SUCCESS;
        } else {
            return com.example.payment_service.entity.PaymentStatus.FAILED;
        }
    }
}
