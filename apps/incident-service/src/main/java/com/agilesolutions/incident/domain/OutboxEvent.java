package com.agilesolutions.incident.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_pending", columnList = "published_at, created_at"))
public class OutboxEvent {
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, length = 160) private String topic;
    @Column(nullable = false, length = 160) private String messageKey;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false, length = 80) private String eventType;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;
    @Column(length = 1000) private String lastError;
    private int attempts;

    protected OutboxEvent() {}
    public OutboxEvent(String topic, String messageKey, String payload, String eventType) {
        this.id = UUID.randomUUID().toString(); this.topic = topic; this.messageKey = messageKey;
        this.payload = payload; this.eventType = eventType; this.createdAt = Instant.now();
    }
    public void markPublished() { this.publishedAt = Instant.now(); this.lastError = null; }
    public void markFailed(Throwable error) {
        this.attempts++;
        this.lastError = error == null ? "Unknown error" : String.valueOf(error.getMessage());
        if (lastError.length() > 990) lastError = lastError.substring(0, 990);
    }
    public String getId() { return id; }
    public String getTopic() { return topic; }
    public String getMessageKey() { return messageKey; }
    public String getPayload() { return payload; }
    public String getEventType() { return eventType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public int getAttempts() { return attempts; }
}
