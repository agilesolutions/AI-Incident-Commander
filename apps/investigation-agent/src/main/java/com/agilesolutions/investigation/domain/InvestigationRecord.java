package com.agilesolutions.investigation.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "investigations", indexes = @Index(name = "idx_investigations_incident", columnList = "incident_id"))
public class InvestigationRecord {
    @Id @Column(name = "event_id", length = 100) private String eventId;
    @Column(name = "incident_id", nullable = false, length = 36) private String incidentId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InvestigationStatus status;
    @Column(nullable = false, length = 120) private String correlationId;
    @Column(nullable = false) private Instant receivedAt;
    private Instant completedAt;
    @Column(columnDefinition = "text") private String reportJson;
    @Column(length = 2000) private String errorMessage;
    @Version private long version;

    protected InvestigationRecord() {}
    public InvestigationRecord(String eventId, String incidentId, String correlationId) {
        this.eventId = eventId; this.incidentId = incidentId; this.correlationId = correlationId;
        this.status = InvestigationStatus.RECEIVED; this.receivedAt = Instant.now();
    }
    public void start() { status = InvestigationStatus.RUNNING; }
    public void complete(String json) { status = InvestigationStatus.COMPLETED; reportJson = json; completedAt = Instant.now(); }
    public void fail(String message) { status = InvestigationStatus.FAILED; errorMessage = message; completedAt = Instant.now(); }
    public String getEventId() { return eventId; }
    public String getIncidentId() { return incidentId; }
    public String getCorrelationId() { return correlationId; }
    public InvestigationStatus getStatus() { return status; }
    public String getReportJson() { return reportJson; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
