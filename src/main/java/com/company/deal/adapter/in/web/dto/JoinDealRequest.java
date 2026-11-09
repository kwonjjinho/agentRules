package com.company.deal.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공동구매 참여(구매 예약) 요청 DTO")
public record JoinDealRequest(
    @Schema(description = "신청 구매 수량", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "구매 수량은 필수입니다.")
    @Min(value = 1, message = "최소 1개 이상 구매 신청해야 합니다.")
    @Max(value = 10, message = "1인당 1회 최대 10개까지 신청 가능합니다.")
    Integer quantity
) {}
