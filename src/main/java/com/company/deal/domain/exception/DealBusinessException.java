package com.company.deal.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DealBusinessException extends RuntimeException {

    private final DealErrorCode errorCode;
    private final HttpStatus status;

    public DealBusinessException(DealErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
    }
}
