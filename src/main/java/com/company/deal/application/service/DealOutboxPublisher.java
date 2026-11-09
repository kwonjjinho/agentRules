package com.company.deal.application.service;

import com.company.deal.domain.entity.DealOutboxEvent;
import com.company.deal.domain.repository.DealOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealOutboxPublisher {

    private final DealOutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String DEAL_EVENTS_TOPIC = "deal.events.v1";

    /**
     * 1초 주기로 미발행된 Outbox 이벤트를 조회하여 Kafka로 릴레이 (Transactional Outbox)
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<DealOutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(PageRequest.of(0, 50));

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (DealOutboxEvent event : pendingEvents) {
            try {
                // Partition Key로 aggregateId (dealId)를 전달하여 동일 딜 이벤트의 순서 보장
                kafkaTemplate.send(DEAL_EVENTS_TOPIC, event.getAggregateId(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            event.markAsPublished();
                            log.debug("Outbox 이벤트 Kafka 발행 성공: id={}, type={}, dealId={}",
                                event.getId(), event.getEventType(), event.getAggregateId());
                        } else {
                            log.error("Outbox 이벤트 Kafka 발행 실패: id={}, type={}",
                                event.getId(), event.getEventType(), ex);
                        }
                    });
            } catch (Exception e) {
                log.error("Kafka 전송 예외 발생: id={}", event.getId(), e);
            }
        }
    }
}
