package com.company.deal.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "공동구매 딜 생성 요청 DTO")
public record CreateDealRequest(
    @Schema(description = "상품 ID", example = "PROD-98102", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "상품 ID는 필수 입력 값입니다.")
    String productId,

    @Schema(description = "공동구매 제목/상품명", example = "실리콘밸리 프리미엄 에르고노믹 키보드 공동구매", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "공동구매 제목은 필수입니다.")
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하이어야 합니다.")
    String title,

    @Schema(description = "정상 판매가 (원)", example = "150000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "정상 판매가는 필수입니다.")
    @Positive(message = "정상 판매가는 양수이어야 합니다.")
    BigDecimal originalPrice,

    @Schema(description = "공동구매 할인가 (원)", example = "99000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "공동구매 할인가격은 필수입니다.")
    @Positive(message = "공동구매 할인가격은 양수이어야 합니다.")
    BigDecimal dealPrice,

    @Schema(description = "공동구매 성사 최소 목표 수량", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "최소 목표 수량은 필수입니다.")
    @Min(value = 1, message = "최소 목표 수량은 1개 이상이어야 합니다.")
    Integer targetQuantity,

    @Schema(description = "최대 모집 가능 한도 수량 (품절 기준)", example = "200", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "최대 한도 수량은 필수입니다.")
    @Min(value = 1, message = "최대 한도 수량은 1개 이상이어야 합니다.")
    Integer maxCapacity,

    @Schema(description = "공동구매 모집 시작 일시 (UTC)", example = "2026-11-09T13:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "시작 일시는 필수입니다.")
    Instant startsAt,

    @Schema(description = "공동구매 모집 마감 일시 (UTC)", example = "2026-11-16T13:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "종료 일시는 필수입니다.")
    @Future(message = "종료 일시는 현재 시간 이후이어야 합니다.")
    Instant endsAt
) {
    public CreateDealRequest {
        if (dealPrice != null && originalPrice != null && dealPrice.compareTo(originalPrice) >= 0) {
            throw new IllegalArgumentException("공동구매 가격은 정상 판매가보다 저렴해야 합니다.");
        }
        if (targetQuantity != null && maxCapacity != null && targetQuantity > maxCapacity) {
            throw new IllegalArgumentException("최소 목표 수량은 최대 한도 수량을 초과할 수 없습니다.");
        }
    }
}
