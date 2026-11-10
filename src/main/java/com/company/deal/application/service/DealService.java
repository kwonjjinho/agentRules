package com.company.deal.application.service;

import com.company.deal.adapter.in.web.dto.CreateDealRequest;
import com.company.deal.adapter.in.web.dto.DealResponse;
import com.company.deal.adapter.in.web.dto.JoinDealRequest;
import com.company.deal.application.port.in.DealCommandUseCase;
import com.company.deal.application.port.in.DealQueryUseCase;
import com.company.deal.domain.entity.Deal;
import com.company.deal.domain.entity.DealOutboxEvent;
import com.company.deal.domain.entity.DealParticipant;
import com.company.deal.domain.event.CloudEventEnvelope;
import com.company.deal.domain.event.DealAchievedEvent;
import com.company.deal.domain.event.DealJoinedEvent;
import com.company.deal.domain.exception.DealBusinessException;
import com.company.deal.domain.exception.DealErrorCode;
import com.company.deal.domain.model.DealStatus;
import com.company.deal.domain.repository.DealOutboxEventRepository;
import com.company.deal.domain.repository.DealParticipantRepository;
import com.company.deal.domain.repository.DealRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealService implements DealCommandUseCase, DealQueryUseCase {

    private final DealRepository dealRepository;
    private final DealParticipantRepository dealParticipantRepository;
    private final DealOutboxEventRepository outboxEventRepository;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;
    private final DealRealtimeStreamService streamService;

    @Override
    @Transactional
    public DealResponse createDeal(CreateDealRequest request) {
        String dealId = "DEAL-" + UUID.randomUUID().toString().substring(0, 8);

        Deal deal = new Deal(
            dealId,
            request.productId(),
            request.title(),
            request.originalPrice(),
            request.dealPrice(),
            request.targetQuantity(),
            request.maxCapacity(),
            request.startsAt(),
            request.endsAt()
        );

        Deal savedDeal = dealRepository.save(deal);
        log.info("공동구매 딜 생성 완료: dealId={}, title={}", savedDeal.getId(), savedDeal.getTitle());
        return mapToResponse(savedDeal);
    }

    @Override
    public void joinDeal(String dealId, String userId, JoinDealRequest request) {
        String lockKey = "lock:deal:" + dealId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("분산 락 획득 타임아웃: dealId={}, userId={}", dealId, userId);
                throw new DealBusinessException(DealErrorCode.LOCK_ACQUISITION_FAILED);
            }

            Deal targetDeal;
            try {
                targetDeal = transactionTemplate.execute(status -> {
                    Deal deal = findDealById(dealId);

                    // 1. 도메인 비즈니스 불변식 검증 및 재고 차감
                    boolean wasAchievedBefore = deal.isTargetAchieved();
                    deal.join(userId, request.quantity());

                    // 2. 참여자 엔티티 생성 및 영속화
                    String participantId = UUID.randomUUID().toString();
                    DealParticipant participant = new DealParticipant(
                        participantId,
                        deal,
                        userId,
                        request.quantity(),
                        deal.getDealPrice()
                    );
                    dealParticipantRepository.save(participant);

                    // 3. Transactional Outbox: 참여 이벤트 영속화
                    saveOutboxEvent(
                        "Deal",
                        deal.getId(),
                        "com.company.deal.joined",
                        new DealJoinedEvent(
                            participantId,
                            deal.getId(),
                            userId,
                            request.quantity(),
                            deal.getDealPrice(),
                            participant.getTotalAmount(),
                            Instant.now()
                        )
                    );

                    // 4. 최소 목표 수량 최초 달성 시 DealAchievedEvent 영속화 (비동기 결제/주문 연동 트리거)
                    if (!wasAchievedBefore && deal.isTargetAchieved()) {
                        saveOutboxEvent(
                            "Deal",
                            deal.getId(),
                            "com.company.deal.target-achieved",
                            new DealAchievedEvent(
                                deal.getId(),
                                deal.getProductId(),
                                deal.getCurrentQuantity(),
                                deal.getTargetQuantity(),
                                deal.getDealPrice(),
                                Instant.now()
                            )
                        );
                        log.info("🎉 공구 목표 수량 달성! Outbox 이벤트 발행 대기: dealId={}", deal.getId());
                    }

                    log.info("공구 참여 성공: dealId={}, userId={}, qty={}, currentTotal={}/{}",
                        dealId, userId, request.quantity(), deal.getCurrentQuantity(), deal.getMaxCapacity());
                    return deal;
                });

                // 5. Redis 캐시 무효화 (실시간 딜 데이터 갱신)
                evictDealCache(dealId);

                // 6. 실시간 스트림 브로드캐스트 (SSE 및 Redis Pub/Sub)
                if (targetDeal != null && streamService != null) {
                    streamService.publishUpdate(targetDeal);
                }

            } finally {
                // 트랜잭션 커밋 완료 후 안전하게 분산 락 해제
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DealBusinessException(DealErrorCode.LOCK_ACQUISITION_FAILED);
        }
    }

    @Override
    @Transactional
    public void closeDeal(String dealId) {
        Deal deal = findDealById(dealId);
        deal.close();
        evictDealCache(dealId);
        if (streamService != null) {
            streamService.publishUpdate(deal);
        }
        log.info("공동구매 수동 마감 처리: dealId={}", dealId);
    }

    @Override
    @Transactional(readOnly = true)
    public DealResponse getDeal(String dealId) {
        Deal deal = findDealById(dealId);
        return mapToResponse(deal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DealResponse> getDealsByStatus(DealStatus status, Pageable pageable) {
        return dealRepository.findByStatus(status, pageable)
            .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DealResponse> getActiveDeals() {
        return dealRepository.findActiveDeals(Instant.now()).stream()
            .map(this::mapToResponse)
            .toList();
    }

    private Deal findDealById(String dealId) {
        return dealRepository.findById(dealId)
            .orElseThrow(() -> new DealBusinessException(DealErrorCode.DEAL_NOT_FOUND));
    }

    private void evictDealCache(String dealId) {
        String cacheKey = "deal:detail:" + dealId;
        redisTemplate.delete(cacheKey);
    }

    private <T> void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, T data) {
        try {
            CloudEventEnvelope<T> envelope = CloudEventEnvelope.of(eventType, data);
            String payloadJson = objectMapper.writeValueAsString(envelope);

            DealOutboxEvent outboxEvent = new DealOutboxEvent(
                UUID.randomUUID().toString(),
                aggregateType,
                aggregateId,
                eventType,
                payloadJson
            );
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Outbox 이벤트 JSON 직렬화 실패: eventType={}", eventType, e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }

    private DealResponse mapToResponse(Deal deal) {
        return DealResponse.of(
            deal.getId(),
            deal.getProductId(),
            deal.getTitle(),
            deal.getOriginalPrice(),
            deal.getDealPrice(),
            deal.getTargetQuantity(),
            deal.getMaxCapacity(),
            deal.getCurrentQuantity(),
            deal.getStatus(),
            deal.getStartsAt(),
            deal.getEndsAt()
        );
    }
}
