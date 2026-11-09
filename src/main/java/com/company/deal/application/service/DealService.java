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

        Deal saved = dealRepository.save(deal);
        log.info("공동구매 딜 생성 완료: dealId={}, productId={}", dealId, request.productId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void openDeal(String dealId) {
        Deal deal = findDealById(dealId);
        deal.open();
        log.info("공동구매 모집 개시: dealId={}", dealId);
    }

    /**
     * 초고동시성 Redisson 분산 락 기반 공구 참여 및 Transactional Outbox 이벤트 저장
     */
    @Override
    public void joinDeal(String dealId, String userId, JoinDealRequest request) {
        String lockKey = "lock:deal:" + dealId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 최대 3초 대기, 5초 임대 (락 획득 후 트랜잭션 커밋 완료 보장)
            boolean isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("공구 참여 분산 락 획득 실패 (대기 시간 초과): dealId={}, userId={}", dealId, userId);
                throw new DealBusinessException(DealErrorCode.LOCK_ACQUISITION_FAILED);
            }

            try {
                // 트랜잭션 범위가 분산 락 내부에서 완전히 커밋되도록 보장
                transactionTemplate.executeWithoutResult(status -> {
                    Deal deal = findDealById(dealId);
                    boolean wasAchievedBefore = deal.isTargetAchieved();

                    // 1. 도메인 수량 차감 및 상태 전이 검증 (불변식 검사)
                    deal.join(request.quantity());

                    // 2. 참여자 레코드 생성 및 저장
                    String participantId = "PART-" + UUID.randomUUID().toString().substring(0, 8);
                    DealParticipant participant = new DealParticipant(
                        participantId,
                        deal.getId(),
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
                });

                // 5. Redis 캐시 무효화 (실시간 딜 데이터 갱신)
                evictDealCache(dealId);

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
