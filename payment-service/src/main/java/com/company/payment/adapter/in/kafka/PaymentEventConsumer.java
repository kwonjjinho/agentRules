package com.company.payment.adapter.in.kafka;

import com.company.payment.application.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 공동구매 딜 이벤트 비동기 Kafka 컨슈머 (Choreography Saga).
 * 'deal.events.v1' 토픽으로부터 참여 이벤트를 수신하여 비동기 결제 처리를 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "deal.events.v1", groupId = "payment-service-group")
    public void consumeDealEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("type").asText();
            JsonNode data = root.path("data");

            log.info("Received Kafka CloudEvent in payment-service: type={}, id={}",
                    eventType, root.path("id").asText());

            if ("com.company.deal.joined".equals(eventType)) {
                String participantId = data.path("participantId").asText();
                String dealId = data.path("dealId").asText();
                String userId = data.path("userId").asText();
                BigDecimal totalAmount = new BigDecimal(data.path("totalAmount").asText());

                String idempotencyKey = "idemp-deal-join-" + participantId;
                paymentService.processPayment(dealId, userId, totalAmount, idempotencyKey);
            }
        } catch (Exception e) {
            log.error("Failed to process payment from deal event: error={}", e.getMessage(), e);
        }
    }
}
