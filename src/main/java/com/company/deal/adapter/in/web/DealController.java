package com.company.deal.adapter.in.web;

import com.company.deal.adapter.in.web.dto.CreateDealRequest;
import com.company.deal.adapter.in.web.dto.DealResponse;
import com.company.deal.adapter.in.web.dto.JoinDealRequest;
import com.company.deal.application.port.in.DealCommandUseCase;
import com.company.deal.application.port.in.DealQueryUseCase;
import com.company.deal.domain.model.DealStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group Buying Deal", description = "공동구매 딜 라이프사이클 관리 및 참여 API")
@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class DealController {

    private final DealCommandUseCase dealCommandUseCase;
    private final DealQueryUseCase dealQueryUseCase;

    @Operation(summary = "공동구매 딜 등록", description = "판매자/관리자가 새로운 공동구매 상품 딜을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "공동구매 딜 생성 성공",
            content = @Content(schema = @Schema(implementation = DealResponse.class))),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패 또는 잘못된 가격 정책",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@Valid @RequestBody CreateDealRequest request) {
        DealResponse response = dealCommandUseCase.createDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "공동구매 모집 개시", description = "대기 상태의 공동구매 딜을 모집 진행중(OPEN) 상태로 변경합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "모집 개시 성공"),
        @ApiResponse(responseCode = "400", description = "모집 대기 상태가 아님",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "딜을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{dealId}/open")
    public ResponseEntity<Void> openDeal(@PathVariable String dealId) {
        dealCommandUseCase.openDeal(dealId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "공동구매 참여 (초고동시성 분산 락 적용)",
        description = "게이트웨이가 검증한 X-User-Id 헤더를 통해 본인 확인 후, Redisson 분산 락 기반으로 수량을 안전하게 차감하고 공구에 참여합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "공구 참여 성공"),
        @ApiResponse(responseCode = "400", description = "모집중인 딜이 아니거나 마감기한 초과",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "딜을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "최대 모집 한도 초과 또는 동시성 락 타임아웃",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{dealId}/join")
    public ResponseEntity<Void> joinDeal(
        @PathVariable String dealId,
        // Gateway Token Relay: 게이트웨이가 검증한 신뢰 헤더 바인딩 (IDOR 취약점 차단)
        @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody JoinDealRequest request
    ) {
        dealCommandUseCase.joinDeal(dealId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공동구매 수동 마감", description = "진행중인 공동구매 딜을 종료(CLOSED) 상태로 전환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "마감 완료"),
        @ApiResponse(responseCode = "404", description = "딜을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{dealId}/close")
    public ResponseEntity<Void> closeDeal(@PathVariable String dealId) {
        dealCommandUseCase.closeDeal(dealId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "공동구매 딜 단건 상세 조회", description = "딜 상세 정보, 실시간 할인율, 참여 수량 및 목표 달성률을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = DealResponse.class))),
        @ApiResponse(responseCode = "404", description = "딜을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{dealId}")
    public ResponseEntity<DealResponse> getDeal(@PathVariable String dealId) {
        DealResponse response = dealQueryUseCase.getDeal(dealId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공동구매 딜 상태별 페이징 목록 조회", description = "상태(SCHEDULED, OPEN, ACHIEVED, CLOSED) 조건별 딜 목록을 페이징 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<DealResponse>> getDealsByStatus(
        @RequestParam(defaultValue = "OPEN") DealStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<DealResponse> responses = dealQueryUseCase.getDealsByStatus(status, pageable);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "실시간 활성 공동구매 딜 목록 조회", description = "현재 참여 가능한 OPEN/ACHIEVED 상태의 활성 딜 목록을 마감 임박순으로 조회합니다.")
    @GetMapping("/active")
    public ResponseEntity<List<DealResponse>> getActiveDeals() {
        List<DealResponse> responses = dealQueryUseCase.getActiveDeals();
        return ResponseEntity.ok(responses);
    }
}
