package com.company.deal.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "deal_outbox_events", indexes = {
    @Index(name = "idx_outbox_published_created", columnList = "published, created_at"),
    @Index(name = "idx_outbox_aggregate_id", columnList = "aggregate_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DealOutboxEvent {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "aggregate_type", length = 50, nullable = false)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 36, nullable = false)
    private String aggregateId;

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    public DealOutboxEvent(String id, String aggregateType, String aggregateId, String eventType, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.published = false;
        this.createdAt = Instant.now();
    }

    public void markAsPublished() {
        this.published = true;
        this.publishedAt = Instant.now();
    }
}
