package com.company.deal.domain.entity;

import com.company.deal.domain.exception.DealBusinessException;
import com.company.deal.domain.exception.DealErrorCode;
import com.company.deal.domain.model.DealStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "deals", indexes = {
    @Index(name = "idx_deals_status_ends_at", columnList = "status, ends_at"),
    @Index(name = "idx_deals_product_id", columnList = "product_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Deal {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "product_id", length = 50, nullable = false)
    private String productId;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "original_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "deal_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal dealPrice;

    @Column(name = "target_quantity", nullable = false)
    private Integer targetQuantity;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DealStatus status;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Deal(
        String id,
        String productId,
        String title,
        BigDecimal originalPrice,
        BigDecimal dealPrice,
        Integer targetQuantity,
        Integer maxCapacity,
        Instant startsAt,
        Instant endsAt
    ) {
        this.id = id;
        this.productId = productId;
        this.title = title;
        this.originalPrice = originalPrice;
        this.dealPrice = dealPrice;
        this.targetQuantity = targetQuantity;
        this.maxCapacity = maxCapacity;
        this.currentQuantity = 0;
        this.status = DealStatus.SCHEDULED;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 공동구매 모집 시작
     */
    public void open() {
        if (this.status != DealStatus.SCHEDULED) {
            throw new DealBusinessException(DealErrorCode.DEAL_NOT_OPEN);
        }
        this.status = DealStatus.OPEN;
        this.updatedAt = Instant.now();
    }

    /**
     * 공동구매 참여 (수량 증가 및 상태 전이 검증)
     */
    public void join(int quantity) {
        if (this.status != DealStatus.OPEN && this.status != DealStatus.ACHIEVED) {
            throw new DealBusinessException(DealErrorCode.DEAL_NOT_OPEN);
        }
        if (Instant.now().isAfter(this.endsAt)) {
            this.status = DealStatus.CLOSED;
            this.updatedAt = Instant.now();
            throw new DealBusinessException(DealErrorCode.DEAL_ALREADY_CLOSED);
        }
        if (this.currentQuantity + quantity > this.maxCapacity) {
            throw new DealBusinessException(DealErrorCode.EXCEED_MAX_CAPACITY);
        }

        this.currentQuantity += quantity;

        // 최소 목표 수량 달성 시 ACHIEVED 상태로 자동 전이
        if (this.currentQuantity >= this.targetQuantity && this.status == DealStatus.OPEN) {
            this.status = DealStatus.ACHIEVED;
        }

        this.updatedAt = Instant.now();
    }

    /**
     * 공동구매 마감 (모집 종료)
     */
    public void close() {
        if (this.status == DealStatus.CLOSED || this.status == DealStatus.CANCELLED) {
            return;
        }
        this.status = DealStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    /**
     * 공동구매 취소
     */
    public void cancel() {
        if (this.status == DealStatus.CLOSED) {
            throw new DealBusinessException(DealErrorCode.DEAL_ALREADY_CLOSED);
        }
        this.status = DealStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public boolean isTargetAchieved() {
        return this.currentQuantity >= this.targetQuantity;
    }
}
