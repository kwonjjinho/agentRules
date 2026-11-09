package com.company.auth.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_404_01", "사용자를 찾을 수 없습니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_400_01", "현재 비밀번호가 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_400_02", "기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "AUTH_409_01", "이미 사용 중인 아이디입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
