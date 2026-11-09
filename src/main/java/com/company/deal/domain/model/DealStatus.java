package com.company.deal.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공동구매 딜 진행 상태")
public enum DealStatus {
    @Schema(description = "모집 대기중")
    SCHEDULED,

    @Schema(description = "모집 진행중")
    OPEN,

    @Schema(description = "최소 목표 인원/수량 달성 (공구 확정)")
    ACHIEVED,

    @Schema(description = "모집 종료 (마감)")
    CLOSED,

    @Schema(description = "목표 미달 또는 취소됨")
    CANCELLED
}
