package com.agilesolutions.incident.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_events", uniqueConstraints = @UniqueConstraint(name = "uk_processed_event_id", columnNames = "event_id"))
public class ProcessedEvent {
    @Id @Column(name = "event_id", length = 100) private String eventId;
    @Column(nullable = false, length = 100) private String eventType;
    @Column(nullable = false) private Instant processedAt;
    protected ProcessedEvent() {}
    public ProcessedEvent(String eventId, String eventType) {
        this.eventId = eventId; this.eventType = eventType; this.processedAt = Instant.now();
    }
}
