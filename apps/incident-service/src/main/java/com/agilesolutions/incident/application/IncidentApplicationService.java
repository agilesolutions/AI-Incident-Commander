package com.agilesolutions.incident.application;

import com.agilesolutions.incident.config.IncidentProperties;
import com.agilesolutions.incident.domain.*;
import com.agilesolutions.incident.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class IncidentApplicationService {
    private final IncidentRepository incidents;
    private final IncidentEvidenceRepository evidence;
    private final ProcessedEventRepository processed;
    private final OutboxEventRepository outbox;
    private final IncidentProperties properties;
    private final ObjectMapper mapper;

    public IncidentApplicationService(IncidentRepository incidents, IncidentEvidenceRepository evidence,
            ProcessedEventRepository processed, OutboxEventRepository outbox,
            IncidentProperties properties, ObjectMapper mapper) {
        this.incidents = incidents; this.evidence = evidence; this.processed = processed;
        this.outbox = outbox; this.properties = properties; this.mapper = mapper;
    }

    @Transactional
    public void acceptDetectedEvent(GenericRecord event) {
        String eventId = required(event, "eventId");
        if (processed.existsById(eventId)) return;
        String incidentId = required(event, "incidentId");
        if (incidents.existsById(incidentId)) {
            processed.save(new ProcessedEvent(eventId, "IncidentDetected"));
            return;
        }
        Incident incident = new Incident(incidentId, required(event, "title"),
            required(event, "description"), IncidentSeverity.valueOf(required(event, "severity")),
            required(event, "environment"), required(event, "primaryResourceId"),
            required(event, "detectorId"), required(event, "correlationId"),
            nullable(event, "causationId"), parseInstant(required(event, "occurredAt")));
        IncidentStatus initialStatus = incident.getStatus();
        incident.transitionTo(IncidentStatus.INVESTIGATION_REQUESTED);
        incidents.save(incident);
        Object ids = event.get("evidenceEventIds");
        if (ids instanceof Iterable<?> iterable) {
            for (Object id : iterable) {
                String sourceId = String.valueOf(id);
                if (!sourceId.isBlank() && !evidence.existsByIncidentIdAndSourceEventId(incidentId, sourceId))
                    evidence.save(new IncidentEvidence(incidentId, sourceId));
            }
        }
        processed.save(new ProcessedEvent(eventId, "IncidentDetected"));
        enqueue("IncidentUpdated", properties.getUpdatedTopic(), incidentId,
            eventId, incidentId, initialStatus.name(), incident.getStatus().name(),
            "Incident gedetecteerd door " + incident.getDetectorId() + "; onderzoek aangevraagd",
            incident.getCorrelationId(), eventId);
        enqueueInvestigation(incident, eventId);
    }

    @Transactional
    public Incident requestInvestigation(String incidentId, String objective) {
        Incident incident = getOrThrow(incidentId);
        IncidentStatus previous = incident.getStatus();
        incident.transitionTo(IncidentStatus.INVESTIGATION_REQUESTED);
        incidents.save(incident);
        String eventId = UUID.randomUUID().toString();
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("eventId", eventId); payload.put("schemaVersion", "1.0.0");
        payload.put("occurredAt", Instant.now().toString()); payload.put("incidentId", incidentId);
        payload.put("priority", incident.getSeverity().name()); payload.put("objective", objective);
        payload.put("evidenceEventIds", evidence.findByIncidentIdOrderByRecordedAtAsc(incidentId)
                .stream().map(IncidentEvidence::getSourceEventId).toList());
        payload.put("correlationId", incident.getCorrelationId()); payload.put("causationId", incident.getCausationId());
        saveOutbox(properties.getInvestigationRequestedTopic(), incidentId, "InvestigationRequested", payload);
        enqueue("IncidentUpdated", properties.getUpdatedTopic(), incidentId, eventId, incidentId,
                previous.name(), incident.getStatus().name(), "Onderzoek aangevraagd", incident.getCorrelationId(), eventId);
        return incident;
    }

    @Transactional
    public Incident transition(String incidentId, IncidentStatus next, String summary) {
        Incident incident = getOrThrow(incidentId);
        IncidentStatus previous = incident.getStatus();
        incident.transitionTo(next);
        incidents.save(incident);
        enqueue("IncidentUpdated", properties.getUpdatedTopic(), incidentId,
                UUID.randomUUID().toString(), incidentId, previous.name(), next.name(),
                summary == null ? "Status gewijzigd naar " + next : summary,
                incident.getCorrelationId(), incident.getCausationId());
        return incident;
    }

    @Transactional(readOnly = true)
    public Incident getOrThrow(String id) {
        return incidents.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<IncidentEvidence> evidence(String id) {
        getOrThrow(id);
        return evidence.findByIncidentIdOrderByRecordedAtAsc(id);
    }

    @Transactional(readOnly = true)
    public List<Incident> list(IncidentStatus status) {
        return status == null ? incidents.findAll() : incidents.findTop100ByStatusOrderByDetectedAtDesc(status);
    }

    private void enqueueInvestigation(Incident incident, String causationId) {
        String eventId = UUID.randomUUID().toString();
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("eventId", eventId); payload.put("schemaVersion", "1.0.0");
        payload.put("occurredAt", Instant.now().toString()); payload.put("incidentId", incident.getIncidentId());
        payload.put("priority", incident.getSeverity().name());
        payload.put("objective", "Onderzoek de waarschijnlijke oorzaak en verzamel controleerbaar bewijs.");
        payload.put("evidenceEventIds", evidence.findByIncidentIdOrderByRecordedAtAsc(incident.getIncidentId())
                .stream().map(IncidentEvidence::getSourceEventId).toList());
        payload.put("correlationId", incident.getCorrelationId()); payload.put("causationId", causationId);
        saveOutbox(properties.getInvestigationRequestedTopic(), incident.getIncidentId(), "InvestigationRequested", payload);
    }

    private void enqueue(String type, String topic, String key, String eventId, String incidentId,
                         String previous, String status, String summary, String correlationId, String causationId) {
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("eventId", eventId); payload.put("schemaVersion", "1.0.0");
        payload.put("occurredAt", Instant.now().toString()); payload.put("incidentId", incidentId);
        payload.put("previousStatus", previous); payload.put("status", status); payload.put("summary", summary);
        payload.put("correlationId", correlationId); payload.put("causationId", causationId);
        saveOutbox(topic, key, type, payload);
    }

    private void saveOutbox(String topic, String key, String type, Map<String,Object> payload) {
        try { outbox.save(new OutboxEvent(topic, key, mapper.writeValueAsString(payload), type)); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Outbox-payload kan niet worden geserialiseerd", e); }
    }
    private static String required(GenericRecord r, String field) {
        Object value = r.get(field);
        if (value == null || value.toString().isBlank()) throw new IllegalArgumentException("Verplicht eventveld ontbreekt: " + field);
        return value.toString();
    }
    private static String nullable(GenericRecord r, String field) {
        Object value = r.get(field); return value == null ? null : value.toString();
    }
    private static Instant parseInstant(String value) {
        try { return Instant.parse(value); } catch (Exception e) { throw new IllegalArgumentException("Ongeldige occurredAt: " + value, e); }
    }
}
