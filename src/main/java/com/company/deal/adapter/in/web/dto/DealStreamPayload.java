package com.company.deal.adapter.in.web.dto;

import com.company.deal.domain.model.DealStatus;

import java.time.Instant;

/**
 * 실시간 SSE(Server-Sent Events)로 클라이언트에 스트리밍되는 딜 상태 페이로드.
 */
public record DealStreamPayload(
        String dealId,
        int currentStock,
        int currentParticipants,
        int targetQuantity,
        DealStatus status,
        Instant timestamp
) {
    public static DealStreamPayload of(
            String dealId,
            int currentStock,
            int currentParticipants,
            int targetQuantity,
            DealStatus status
    ) {
        return new DealStreamPayload(
                dealId,
                currentStock,
                currentParticipants,
                targetQuantity,
                status,
                Instant.now()
        );
    }
}
