package com.company.deal.adapter.in.kafka;

import com.company.deal.application.service.DealPaymentSettlementService;
import com.company.deal.domain.entity.ProcessedEvent;
import com.company.deal.domain.event.CloudEventEnvelope;
import com.company.deal.domain.event.DealAchievedEvent;
import com.company.deal.domain.repository.ProcessedEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealEventConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final DealPaymentSettlementService paymentSettlementService;
    private final ObjectMapper objectMapper;

    /**
     * 멱등성(Idempotent) 카프카 이벤트 컨슈머:
     * - 중복 메시지 수신 시 processed_events 조회로 멱등 처리 (이중 결제/주문 방지)
     * - 수동 커밋(MANUAL_IMMEDIATE)으로 처리 완결 보장
     */
    @KafkaListener(
        topics = "deal.events.v1",
        groupId = "deal-settlement-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeDealEvents(
        @Header(KafkaHeaders.RECEIVED_KEY) String partitionKey,
        @Payload String payloadJson,
        Acknowledgment acknowledgment
    ) {
        try {
            CloudEventEnvelope<Object> rawEnvelope = objectMapper.readValue(
                payloadJson,
                new TypeReference<>() {}
            );

            String eventId = rawEnvelope.id();
            String eventType = rawEnvelope.type();

            // 1. 멱등성 검증 (Idempotency Key Check)
            if (processedEventRepository.existsByEventId(eventId)) {
                log.warn("이미 처리 완료된 이벤트 스킵(중복 수신 방어): eventId={}, type={}, partitionKey={}",
                    eventId, eventType, partitionKey);
                acknowledgment.acknowledge();
                return;
            }

            // 2. 이벤트 유형별 비즈니스 분기
            if ("com.company.deal.target-achieved".equals(eventType)) {
                CloudEventEnvelope<DealAchievedEvent> achievedEnvelope = objectMapper.readValue(
                    payloadJson,
                    new TypeReference<>() {}
                );
                paymentSettlementService.settleAchievedDeal(achievedEnvelope.data());
            }

            // 3. 멱등성 이벤트 처리 이력 기록
            processedEventRepository.save(new ProcessedEvent(eventId, eventType));

            // 4. 수동 오프셋 커밋
            acknowledgment.acknowledge();
            log.info("Kafka 이벤트 멱등 처리 성공: eventId={}, type={}, partitionKey={}",
                eventId, eventType, partitionKey);

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 중 예외 발생 (에러 핸들러로 위임): key={}, payload={}",
                partitionKey, payloadJson, e);
            throw new RuntimeException("이벤트 처리 실패", e);
        }
    }
}
