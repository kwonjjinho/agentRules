package com.company.payment.adapter.in.web;

import com.company.payment.adapter.in.web.dto.PaymentResponse;
import com.company.payment.adapter.in.web.dto.RefundRequest;
import com.company.payment.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 결제 내역 조회 및 환불 처리 REST 컨트롤러.
 * Zero-Trust 보안 표준: Gateway로부터 주입된 'X-User-Id' 헤더를 통해 사용자 컨텍스트를 식별하며, IDOR 공격을 차단합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "결제 내역 조회 및 환불 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "내 결제 내역 목록 조회", description = "인증된 사용자의 공동구매 결제 이력을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "결제 목록 조회 성공")
    @GetMapping("/my")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId
    ) {
        log.info("Request getMyPayments for userId={}", userId);
        List<PaymentResponse> payments = paymentService.getMyPayments(userId);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "결제 상세 단건 조회", description = "특정 결제 건의 상세 상태 및 PG 거래 번호를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "결제 단건 조회 성공")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "조회할 결제 ID", required = true)
            @PathVariable String paymentId,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId
    ) {
        PaymentResponse response = paymentService.getPayment(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 환불/취소 요청", description = "완료된 결제 건에 대해 환불 처리를 수행합니다.")
    @ApiResponse(responseCode = "200", description = "환불 처리 성공")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @Parameter(description = "환불할 결제 ID", required = true)
            @PathVariable String paymentId,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody RefundRequest request
    ) {
        log.info("Request refundPayment: paymentId={}, userId={}, reason={}", paymentId, userId, request.reason());
        PaymentResponse response = paymentService.refundPayment(paymentId, userId, request.reason());
        return ResponseEntity.ok(response);
    }
}
