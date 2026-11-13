package com.company.payment.application.service;

import com.company.payment.adapter.in.web.dto.PaymentResponse;
import com.company.payment.adapter.out.feign.DealServiceClient;
import com.company.payment.adapter.out.feign.dto.DealDto;
import com.company.payment.domain.entity.Payment;
import com.company.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 멱등성(Idempotency) 보장 결제 비즈니스 서비스.
 * 동일한 idempotencyKey로 중복 결제 요청이 인입될 경우 기존 결제 결과를 즉시 반환하여 이중 승인을 원천 차단합니다.
 * OpenFeign + Resilience4j를 통해 Deal 마이크로서비스의 상태를 안전하게 검증합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DealServiceClient dealServiceClient;

    @Transactional
    public PaymentResponse processPayment(String dealId, String userId, BigDecimal amount, String idempotencyKey) {
        // 1. 멱등키 사전 조회 (Idempotent Check)
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingPayment.isPresent()) {
            log.info("중복 결제 요청 감지 - 기존 결제 반환: idempotencyKey={}, paymentId={}",
                    idempotencyKey, existingPayment.get().getId());
            return PaymentResponse.from(existingPayment.get());
        }

        // 2. OpenFeign + Resilience4j: 대상 딜 상태 동기 검증
        try {
            DealDto deal = dealServiceClient.getDeal(dealId);
            if (deal != null && "CANCELLED".equals(deal.status())) {
                log.warn("취소된 딜에 대한 결제 시도 차단: dealId={}", dealId);
                throw new IllegalStateException("취소된 공동구매 딜에 대해서는 결제할 수 없습니다.");
            }
            if (deal != null && deal.isFallback()) {
                log.warn("DealService 장애로 Fallback 응답 적용: dealId={}, 결제는 정상 진행", dealId);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.warn("딜 상태 동기 검증 실패(서킷 오픈/장애) - Fallback 정책에 따라 결제 진행: {}", e.getMessage());
        }

        // 3. 신규 결제 레코드 생성
        String paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        Payment payment = new Payment(paymentId, dealId, userId, amount, idempotencyKey);

        // 4. 외부 결제 게이트웨이(PG) 승인 연동 시뮬레이션
        try {
            String pgTxId = "PG-" + UUID.randomUUID().toString();
            payment.complete(pgTxId);
            log.info("결제 승인 성공: paymentId={}, dealId={}, userId={}, amount={}, pgTxId={}",
                    paymentId, dealId, userId, amount, pgTxId);
        } catch (Exception e) {
            log.error("PG 결제 승인 실패: paymentId={}, error={}", paymentId, e.getMessage());
            payment.fail("PG승인 거절: " + e.getMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId, String userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다: " + paymentId));

        // IDOR 보안 검증: 본인 결제 내역만 열람 가능
        if (!payment.getUserId().equals(userId)) {
            throw new SecurityException("본인의 결제 내역만 조회할 수 있습니다.");
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(String paymentId, String userId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다: " + paymentId));

        if (!payment.getUserId().equals(userId)) {
            throw new SecurityException("본인의 결제만 환불 요청할 수 있습니다.");
        }

        payment.refund(reason);
        log.info("결제 환불 완료: paymentId={}, userId={}, reason={}", paymentId, userId, reason);
        return PaymentResponse.from(payment);
    }
}
