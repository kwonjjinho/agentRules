package com.company.deal.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DealErrorCode {
    DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "DEAL_404_01", "공동구매 딜을 찾을 수 없습니다."),
    DEAL_NOT_OPEN(HttpStatus.BAD_REQUEST, "DEAL_400_01", "현재 참여 가능한 상태의 공동구매가 아닙니다."),
    DEAL_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "DEAL_400_02", "이미 마감된 공동구매입니다."),
    INVALID_PRICE_POLICY(HttpStatus.BAD_REQUEST, "DEAL_400_03", "공동구매 가격은 정상 판매가보다 낮아야 합니다."),
    INVALID_TARGET_QUANTITY(HttpStatus.BAD_REQUEST, "DEAL_400_04", "최소 목표 수량은 최대 제한 수량을 초과할 수 없습니다."),
    EXCEED_MAX_CAPACITY(HttpStatus.CONFLICT, "DEAL_409_01", "공동구매 최대 모집 가능 수량을 초과하였습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "DEAL_409_02", "동시 참여 요청이 많아 처리에 실패했습니다. 잠시 후 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
