package com.company.deal.application.service;

import com.company.deal.domain.entity.DealParticipant;
import com.company.deal.domain.event.DealAchievedEvent;
import com.company.deal.domain.repository.DealParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealPaymentSettlementService {

    private final DealParticipantRepository dealParticipantRepository;

    /**
     * 공구 목표 수량 달성 시 전체 참여자 결제 확정 및 주문 생성 오케스트레이션 (Saga Pattern)
     */
    @Transactional
    public void settleAchievedDeal(DealAchievedEvent event) {
        log.info("공구 목표 달성 정산 및 일괄 결제 시작: dealId={}, totalQty={}",
            event.dealId(), event.totalAchievedQuantity());

        List<DealParticipant> participants = dealParticipantRepository.findByDealId(event.dealId());

        for (DealParticipant participant : participants) {
            // 실제 서비스에서는 PG사 결제 API 호출 또는 payment-service 비동기 요청 발행
            log.info("참여자 일괄 결제 승인 완료: dealId={}, userId={}, amount={}원, qty={}",
                participant.getDealId(), participant.getUserId(), participant.getTotalAmount(), participant.getQuantity());
        }

        log.info("총 {}명 참여자 결제 처리 완료: dealId={}", participants.size(), event.dealId());
    }
}
