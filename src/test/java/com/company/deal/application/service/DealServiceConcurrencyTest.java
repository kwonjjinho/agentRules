package com.company.deal.application.service;

import com.company.deal.adapter.in.web.dto.JoinDealRequest;
import com.company.deal.domain.entity.Deal;
import com.company.deal.domain.exception.DealBusinessException;
import com.company.deal.domain.repository.DealParticipantRepository;
import com.company.deal.domain.repository.DealRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DealServiceConcurrencyTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private DealParticipantRepository dealParticipantRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private DealService dealService;

    @Test
    @DisplayName("50명이 최대 한도 30개인 딜에 동시 참여 시, 정확히 30명만 성공하고 20명은 예외 처리되어 초과 판매가 방지된다")
    void joinDeal_HighConcurrency_PreventsOverSelling() throws InterruptedException {
        // given: 최대 한도 30개, 현재 수량 0개인 활성 공구 딜 준비
        String dealId = "DEAL-CONCURRENT-01";
        Deal deal = new Deal(
            dealId,
            "PROD-KEYBOARD",
            "초고동시성 검증 키보드",
            BigDecimal.valueOf(150000),
            BigDecimal.valueOf(99000),
            10, // 최소 성사 수량
            30, // 최대 모집 한도 (품절 기준)
            Instant.now().minus(1, ChronoUnit.HOURS),
            Instant.now().plus(7, ChronoUnit.DAYS)
        );
        deal.open(); // 모집중(OPEN) 상태로 전이

        given(redissonClient.getLock(anyString())).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);
        given(dealRepository.findById(dealId)).willReturn(Optional.of(deal));
        lenient().when(redisTemplate.delete(anyString())).thenReturn(true);

        // TransactionTemplate 동작 시 내부 람다 콜백을 동기 실행하도록 모킹
        lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        int totalThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        // when: 50개의 스레드가 동시에 1개씩 공구 참여 요청
        for (int i = 0; i < totalThreads; i++) {
            final String userId = "USER-" + i;
            executorService.submit(() -> {
                try {
                    dealService.joinDeal(dealId, userId, new JoinDealRequest(1));
                    successCount.incrementAndGet();
                } catch (DealBusinessException e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then: 최대 모집 한도인 30명만 성공하고, 나머지 20명은 차단 검증
        assertThat(successCount.get()).isEqualTo(30);
        assertThat(failureCount.get()).isEqualTo(20);
        assertThat(deal.getCurrentQuantity()).isEqualTo(30);
        assertThat(deal.isTargetAchieved()).isTrue();
    }
}
