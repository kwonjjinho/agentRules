package com.company.deal.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "deal_participants", indexes = {
    @Index(name = "idx_participants_deal_id", columnList = "deal_id"),
    @Index(name = "idx_participants_user_id", columnList = "user_id"),
    @Index(name = "idx_participants_deal_user", columnList = "deal_id, user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DealParticipant {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "deal_id", length = 36, nullable = false)
    private String dealId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "deal_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal dealPrice;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public DealParticipant(String id, String dealId, String userId, Integer quantity, BigDecimal dealPrice) {
        this.id = id;
        this.dealId = dealId;
        this.userId = userId;
        this.quantity = quantity;
        this.dealPrice = dealPrice;
        this.totalAmount = dealPrice.multiply(BigDecimal.valueOf(quantity));
        this.joinedAt = Instant.now();
    }
}
