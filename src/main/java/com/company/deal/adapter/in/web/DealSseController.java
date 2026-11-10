package com.company.deal.adapter.in.web;

import com.company.deal.application.service.DealRealtimeStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 공동구매 딜 실시간 스트리밍(SSE) 컨트롤러.
 * 프론트엔드(Next.js)에 실시간 재고 차감 및 딜 상태 변경 이벤트를 푸시합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
@Tag(name = "Deal Realtime Stream", description = "Server-Sent Events (SSE) 실시간 딜 현황 스트리밍 API")
public class DealSseController {

    private final DealRealtimeStreamService realtimeStreamService;

    @Operation(
            summary = "공동구매 실시간 재고 및 상태 SSE 스트림 구독",
            description = "특정 딜의 실시간 재고(Stock), 참여자 수, 진행 상태를 Server-Sent Events로 구독합니다."
    )
    @ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공 (text/event-stream)")
    @GetMapping(value = "/{dealId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeDealStream(
            @Parameter(description = "구독할 딜 고유 ID", required = true)
            @PathVariable String dealId,
            HttpServletResponse response
    ) {
        // Nginx / 프록시 환경에서 SSE 버퍼링 방지 및 무캐시 헤더 주입
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");

        log.info("Client subscribed to SSE deal stream: dealId={}", dealId);
        return realtimeStreamService.subscribe(dealId);
    }
}
