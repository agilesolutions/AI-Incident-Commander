package com.agilesolutions.incident.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents", indexes = {
    @Index(name = "idx_incidents_status", columnList = "status"),
    @Index(name = "idx_incidents_resource", columnList = "primary_resource_id"),
    @Index(name = "idx_incidents_detected_at", columnList = "detected_at")
})
public class Incident {
    @Id
    @Column(name = "incident_id", nullable = false, updatable = false, length = 36)
    private String incidentId;
    @Column(nullable = false, length = 240) private String title;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private IncidentSeverity severity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private IncidentStatus status;
    @Column(nullable = false, length = 120) private String environment;
    @Column(name = "primary_resource_id", nullable = false, length = 240) private String primaryResourceId;
    @Column(nullable = false, length = 120) private String detectorId;
    @Column(nullable = false, length = 100) private String correlationId;
    @Column(length = 100) private String causationId;
    @Column(nullable = false) private Instant detectedAt;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    @Version private long version;

    protected Incident() {}

    public Incident(String incidentId, String title, String description, IncidentSeverity severity,
                    String environment, String primaryResourceId, String detectorId,
                    String correlationId, String causationId, Instant detectedAt) {
        this.incidentId = incidentId == null || incidentId.isBlank() ? UUID.randomUUID().toString() : incidentId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = IncidentStatus.NEW;
        this.environment = environment;
        this.primaryResourceId = primaryResourceId;
        this.detectorId = detectorId;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.detectedAt = detectedAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void transitionTo(IncidentStatus next) {
        boolean allowed = switch (status) {
            case NEW -> next == IncidentStatus.INVESTIGATION_REQUESTED || next == IncidentStatus.RESOLVED;
            case INVESTIGATION_REQUESTED -> next == IncidentStatus.INVESTIGATING || next == IncidentStatus.INVESTIGATION_COMPLETED;
            case INVESTIGATING -> next == IncidentStatus.INVESTIGATION_COMPLETED || next == IncidentStatus.RESOLVED;
            case INVESTIGATION_COMPLETED -> next == IncidentStatus.RESOLVED || next == IncidentStatus.INVESTIGATION_REQUESTED;
            case RESOLVED -> next == IncidentStatus.CLOSED || next == IncidentStatus.INVESTIGATION_REQUESTED;
            case CLOSED -> false;
        };
        if (!allowed) throw new InvalidIncidentTransitionException(status, next);
        status = next;
        updatedAt = Instant.now();
    }

    public String getIncidentId() { return incidentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public String getEnvironment() { return environment; }
    public String getPrimaryResourceId() { return primaryResourceId; }
    public String getDetectorId() { return detectorId; }
    public String getCorrelationId() { return correlationId; }
    public String getCausationId() { return causationId; }
    public Instant getDetectedAt() { return detectedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
