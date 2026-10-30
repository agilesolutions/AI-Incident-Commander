package com.agilesolutions.investigation.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "investigation_outbox", indexes = @Index(name = "idx_investigation_outbox_pending", columnList = "published_at, created_at"))
public class InvestigationOutboxEvent {
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, length = 160) private String topic;
    @Column(nullable = false, length = 160) private String messageKey;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;
    @Column(length = 2000) private String lastError;
    private int attempts;
    protected InvestigationOutboxEvent() {}
    public InvestigationOutboxEvent(String topic, String key, String payload) {
        id = UUID.randomUUID().toString(); this.topic = topic; messageKey = key; this.payload = payload; createdAt = Instant.now();
    }
    public void published() { publishedAt = Instant.now(); lastError = null; }
    public void failed(Throwable t) {
        attempts++; lastError = t == null ? "Unknown error" : String.valueOf(t.getMessage());
        if (lastError.length() > 1990) lastError = lastError.substring(0, 1990);
    }
    public String getId() { return id; }
    public String getTopic() { return topic; }
    public String getMessageKey() { return messageKey; }
    public String getPayload() { return payload; }
}
