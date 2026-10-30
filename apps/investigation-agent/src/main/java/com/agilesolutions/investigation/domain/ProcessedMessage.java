package com.agilesolutions.investigation.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_investigation_messages")
public class ProcessedMessage {
    @Id @Column(name = "event_id", length = 100) private String eventId;
    @Column(nullable = false) private Instant processedAt;
    protected ProcessedMessage() {}
    public ProcessedMessage(String eventId) { this.eventId = eventId; this.processedAt = Instant.now(); }
}
