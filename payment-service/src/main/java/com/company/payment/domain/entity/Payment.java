package com.company.payment.domain.entity;

import com.company.payment.domain.model.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 결제 도메인 애그리거트 엔티티 (Database-per-Service: payment_db).
 * 멱등키(idempotencyKey) 유니크 인덱스를 통해 중복 결제 및 이중 청구를 원천 방지합니다.
 */
@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_payments_user_id", columnList = "user_id"),
                @Index(name = "idx_payments_deal_id", columnList = "deal_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "deal_id", nullable = false, length = 64)
    private String dealId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "pg_transaction_id", length = 128)
    private String pgTransactionId;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Payment(String id, String dealId, String userId, BigDecimal amount, String idempotencyKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (dealId == null || dealId.isBlank()) {
            throw new IllegalArgumentException("Deal ID is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        this.id = id;
        this.dealId = dealId;
        this.userId = userId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void complete(String pgTransactionId) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be completed. Current: " + this.status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.updatedAt = Instant.now();
    }

    public void fail(String reason) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING payments can be marked as failed. Current: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public void refund(String reason) {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only COMPLETED payments can be refunded. Current: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }
}
