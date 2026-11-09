package com.company.deal.domain.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "공동구매 참여 발생 이벤트 페이로드")
public record DealJoinedEvent(
    @Schema(description = "참여자 ID", example = "PART-109281")
    String participantId,

    @Schema(description = "공구 딜 ID", example = "DEAL-550e8400")
    String dealId,

    @Schema(description = "사용자 ID", example = "USER-882190")
    String userId,

    @Schema(description = "신청 수량", example = "2")
    Integer quantity,

    @Schema(description = "공구 단가", example = "99000")
    BigDecimal dealPrice,

    @Schema(description = "총 주문 금액", example = "198000")
    BigDecimal totalAmount,

    @Schema(description = "참여 일시")
    Instant joinedAt
) {}
