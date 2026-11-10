package com.company.payment.adapter.in.web.dto;

import com.company.payment.domain.entity.Payment;
import com.company.payment.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String dealId,
        String userId,
        BigDecimal amount,
        PaymentStatus status,
        String idempotencyKey,
        String pgTransactionId,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getDealId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getIdempotencyKey(),
                payment.getPgTransactionId(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
