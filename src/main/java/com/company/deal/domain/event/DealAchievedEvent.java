package com.company.deal.domain.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "공동구매 목표 수량 달성 이벤트 페이로드 (비동기 결제/주문 트리거)")
public record DealAchievedEvent(
    @Schema(description = "공구 딜 ID", example = "DEAL-550e8400")
    String dealId,

    @Schema(description = "상품 ID", example = "PROD-98102")
    String productId,

    @Schema(description = "최종 달성 누적 수량", example = "52")
    Integer totalAchievedQuantity,

    @Schema(description = "목표 수량", example = "50")
    Integer targetQuantity,

    @Schema(description = "공동구매 확정 단가", example = "99000")
    BigDecimal dealPrice,

    @Schema(description = "목표 달성 일시")
    Instant achievedAt
) {}
