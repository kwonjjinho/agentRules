package com.company.deal.adapter.in.web.dto;

import com.company.deal.domain.model.DealStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Schema(description = "공동구매 딜 상세 응답 DTO")
public record DealResponse(
    @Schema(description = "공동구매 딜 ID", example = "DEAL-550e8400")
    String id,

    @Schema(description = "상품 ID", example = "PROD-98102")
    String productId,

    @Schema(description = "공동구매 제목", example = "실리콘밸리 프리미엄 에르고노믹 키보드 공동구매")
    String title,

    @Schema(description = "정상 판매가 (원)", example = "150000")
    BigDecimal originalPrice,

    @Schema(description = "공동구매 할인가 (원)", example = "99000")
    BigDecimal dealPrice,

    @Schema(description = "할인율 (%)", example = "34.0")
    Double discountRate,

    @Schema(description = "성사 최소 목표 수량", example = "50")
    Integer targetQuantity,

    @Schema(description = "최대 모집 가능 수량", example = "200")
    Integer maxCapacity,

    @Schema(description = "현재 참여 수량", example = "42")
    Integer currentQuantity,

    @Schema(description = "목표 달성률 (%)", example = "84.0")
    Double achievementRate,

    @Schema(description = "현재 딜 진행 상태", example = "OPEN")
    DealStatus status,

    @Schema(description = "모집 시작 일시")
    Instant startsAt,

    @Schema(description = "모집 마감 일시")
    Instant endsAt,

    @Schema(description = "딜 성사 여부 (최소 목표 달성)", example = "false")
    boolean isAchieved
) {
    public static DealResponse of(
        String id,
        String productId,
        String title,
        BigDecimal originalPrice,
        BigDecimal dealPrice,
        Integer targetQuantity,
        Integer maxCapacity,
        Integer currentQuantity,
        DealStatus status,
        Instant startsAt,
        Instant endsAt
    ) {
        double discount = originalPrice.subtract(dealPrice)
            .divide(originalPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();

        double achievement = (targetQuantity != null && targetQuantity > 0)
            ? BigDecimal.valueOf(currentQuantity)
                .divide(BigDecimal.valueOf(targetQuantity), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        boolean achieved = currentQuantity >= targetQuantity;

        return new DealResponse(
            id,
            productId,
            title,
            originalPrice,
            dealPrice,
            discount,
            targetQuantity,
            maxCapacity,
            currentQuantity,
            achievement,
            status,
            startsAt,
            endsAt,
            achieved
        );
    }
}
