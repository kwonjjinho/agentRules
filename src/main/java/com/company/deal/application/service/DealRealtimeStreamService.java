package com.company.deal.application.service;

import com.company.deal.adapter.in.web.dto.DealStreamPayload;
import com.company.deal.domain.entity.Deal;
import com.company.deal.domain.repository.DealRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 실시간 Server-Sent Events (SSE) 스트림 관리 서비스.
 * 1. 로컬 인스턴스에 연결된 클라이언트 SseEmitter 커넥션 풀을 관리합니다.
 * 2. Redisson RTopic (deal:stream:global)을 구독하여 분산 멀티 인스턴스 환경에서도 모든 클라이언트에 브로드캐스팅합니다.
 * 3. 25초 간격의 Heartbeat(Ping)를 발송하여 네트워크 중간 계층의 타임아웃을 방지합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealRealtimeStreamService {

    private static final Long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분
    private static final String TOPIC_DEAL_STREAM = "deal:stream:global";

    private final RedissonClient redissonClient;
    private final DealRepository dealRepository;
    private final ObjectMapper objectMapper;

    // dealId -> List of active SseEmitters for this deal on the local node
    private final Map<String, List<SseEmitter>> emittersByDealId = new ConcurrentHashMap<>();

    @PostConstruct
    public void initDistributedSubscription() {
        try {
            RTopic topic = redissonClient.getTopic(TOPIC_DEAL_STREAM);
            topic.addListener(String.class, (channel, message) -> {
                try {
                    DealStreamPayload payload = objectMapper.readValue(message, DealStreamPayload.class);
                    broadcastToLocalEmitters(payload.dealId(), payload);
                } catch (Exception e) {
                    log.error("Failed to parse and broadcast distributed SSE payload: {}", e.getMessage());
                }
            });
            log.info("Initialized Redisson distributed subscription for topic: {}", TOPIC_DEAL_STREAM);
        } catch (Exception e) {
            log.warn("Redisson Topic initialization skipped or running in test mode: {}", e.getMessage());
        }
    }

    /**
     * 클라이언트 SSE 구독 생성 및 초기 스냅샷 전송
     */
    public SseEmitter subscribe(String dealId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emittersByDealId.computeIfAbsent(dealId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(dealId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(dealId, emitter);
        });
        emitter.onError(e -> removeEmitter(dealId, emitter));

        // 1. 연결 확인용 초기 connected 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to deal live stream for dealId: " + dealId));
        } catch (IOException e) {
            removeEmitter(dealId, emitter);
            return emitter;
        }

        // 2. 현재 딜의 최신 상태 스냅샷 즉시 푸시
        dealRepository.findById(dealId).ifPresent(deal -> {
            DealStreamPayload initialPayload = DealStreamPayload.of(
                    deal.getId(),
                    deal.getCurrentStock(),
                    deal.getCurrentParticipants(),
                    deal.getTargetQuantity(),
                    deal.getStatus()
            );
            sendToEmitter(emitter, "snapshot", initialPayload);
        });

        return emitter;
    }

    /**
     * 딜 상태 변경 시 분산 Redis 토픽으로 이벤트 발행
     */
    public void publishUpdate(Deal deal) {
        DealStreamPayload payload = DealStreamPayload.of(
                deal.getId(),
                deal.getCurrentStock(),
                deal.getCurrentParticipants(),
                deal.getTargetQuantity(),
                deal.getStatus()
        );

        try {
            String json = objectMapper.writeValueAsString(payload);
            RTopic topic = redissonClient.getTopic(TOPIC_DEAL_STREAM);
            topic.publish(json);
        } catch (Exception e) {
            log.error("Failed to publish distributed SSE message to Redis topic: {}", e.getMessage());
            // Fallback: 로컬 Emitter에 직접 전송
            broadcastToLocalEmitters(deal.getId(), payload);
        }
    }

    /**
     * 로컬 인스턴스에 연결된 해당 dealId의 모든 SSE Emitter에 전송
     */
    public void broadcastToLocalEmitters(String dealId, DealStreamPayload payload) {
        List<SseEmitter> emitters = emittersByDealId.get(dealId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            sendToEmitter(emitter, "deal-update", payload);
        }
    }

    private void sendToEmitter(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void removeEmitter(String dealId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByDealId.get(dealId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emittersByDealId.remove(dealId);
            }
        }
    }

    /**
     * 25초 주기 Heartbeat 발송으로 커넥션 생존 유지
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        if (emittersByDealId.isEmpty()) {
            return;
        }

        emittersByDealId.forEach((dealId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("ping")
                            .data("heartbeat"));
                } catch (IOException e) {
                    emitter.complete();
                    removeEmitter(dealId, emitter);
                }
            }
        });
    }
}
