package com.agilesolutions.investigation.application;

import com.agilesolutions.investigation.agent.*;
import com.agilesolutions.investigation.config.AgentProperties;
import com.agilesolutions.investigation.domain.*;
import com.agilesolutions.investigation.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class InvestigationOrchestrator {
    private final InvestigationRepository investigations;
    private final ProcessedMessageRepository processed;
    private final InvestigationOutboxRepository outbox;
    private final InvestigationEngine engine;
    private final ObjectMapper mapper;
    private final AgentProperties properties;

    public InvestigationOrchestrator(InvestigationRepository investigations, ProcessedMessageRepository processed,
            InvestigationOutboxRepository outbox, InvestigationEngine engine, ObjectMapper mapper, AgentProperties properties) {
        this.investigations = investigations; this.processed = processed; this.outbox = outbox;
        this.engine = engine; this.mapper = mapper; this.properties = properties;
    }

    public void handle(InvestigationRequest request) {
        if (request.eventId() == null || request.eventId().isBlank())
            throw new IllegalArgumentException("eventId ontbreekt");
        if (processed.existsById(request.eventId())) return;

        InvestigationRecord record = investigations.findById(request.eventId())
            .orElseGet(() -> new InvestigationRecord(request.eventId(), request.incidentId(),
                request.correlationId() == null ? request.incidentId() : request.correlationId()));
        record.start();
        investigations.save(record);

        try {
            InvestigationReport report = engine.investigate(request);
            String reportJson = mapper.writeValueAsString(report);
            complete(request, record, report, reportJson);
        } catch (Exception ex) {
            fail(request, record, ex);
            throw new IllegalStateException("Incidentonderzoek mislukt; Kafka kan opnieuw proberen", ex);
        }
    }

    @Transactional
    protected void complete(InvestigationRequest request, InvestigationRecord record,
                            InvestigationReport report, String reportJson) throws Exception {
        record.complete(reportJson);
        investigations.save(record);
        processed.save(new ProcessedMessage(request.eventId()));
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("schemaVersion", "1.0.0");
        payload.put("occurredAt", Instant.now().toString());
        payload.put("incidentId", request.incidentId());
        payload.put("status", "COMPLETED");
        payload.put("summary", report.summary());
        payload.put("probableRootCause", report.probableRootCause());
        payload.put("confidence", report.confidence());
        payload.put("supportingEvidence", report.supportingEvidence());
        payload.put("uncertainties", report.uncertainties());
        payload.put("recommendedActions", report.recommendedActions());
        payload.put("severityAssessment", report.severityAssessment());
        payload.put("humanReviewRequired", true);
        payload.put("correlationId", request.correlationId());
        payload.put("causationId", request.eventId());
        outbox.save(new InvestigationOutboxEvent(properties.getCompletedTopic(), request.incidentId(),
            mapper.writeValueAsString(payload)));
    }

    @Transactional
    protected void fail(InvestigationRequest request, InvestigationRecord record, Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        if (message.length() > 1900) message = message.substring(0, 1900);
        record.fail(message);
        investigations.save(record);
    }
}
