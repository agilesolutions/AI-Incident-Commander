package com.agilesolutions.incident.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incident_evidence",
       uniqueConstraints = @UniqueConstraint(name = "uk_evidence_incident_event", columnNames = {"incident_id", "source_event_id"}))
public class IncidentEvidence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "incident_id", nullable = false, length = 36) private String incidentId;
    @Column(name = "source_event_id", nullable = false, length = 100) private String sourceEventId;
    @Column(nullable = false) private Instant recordedAt;

    protected IncidentEvidence() {}
    public IncidentEvidence(String incidentId, String sourceEventId) {
        this.incidentId = incidentId;
        this.sourceEventId = sourceEventId;
        this.recordedAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getIncidentId() { return incidentId; }
    public String getSourceEventId() { return sourceEventId; }
    public Instant getRecordedAt() { return recordedAt; }
}
