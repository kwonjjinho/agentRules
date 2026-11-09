package com.company.deal.domain.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "CloudEvents 1.0 표준 이벤트 엔벨로프")
public record CloudEventEnvelope<T>(
    @Schema(description = "CloudEvents 규격 버전", example = "1.0")
    String specversion,

    @Schema(description = "이벤트 고유 식별자 (UUID)", example = "a5f8e210-91c2-4820-b472-8821901ab210")
    String id,

    @Schema(description = "이벤트 발행 출처 URI", example = "https://service.company.com/deal-service")
    String source,

    @Schema(description = "이벤트 유형", example = "com.company.deal.joined")
    String type,

    @Schema(description = "이벤트 발생 일시 (UTC)")
    Instant time,

    @Schema(description = "데이터 콘텐츠 타입", example = "application/json")
    String datacontenttype,

    @Schema(description = "실제 도메인 페이로드 데이터")
    T data
) {
    public static <T> CloudEventEnvelope<T> of(String type, T data) {
        return new CloudEventEnvelope<>(
            "1.0",
            UUID.randomUUID().toString(),
            "https://service.company.com/deal-service",
            type,
            Instant.now(),
            "application/json",
            data
        );
    }
}
