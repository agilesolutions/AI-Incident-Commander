package com.agilesolutions.correlator.messaging;

import com.agilesolutions.correlator.config.CorrelatorProperties;
import com.agilesolutions.correlator.domain.IncidentCandidate;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
public class IncidentDetectedPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CorrelatorProperties properties;
    private final MeterRegistry meterRegistry;
    private final Schema schema;

    public IncidentDetectedPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                     CorrelatorProperties properties, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        try (InputStream in = getClass().getResourceAsStream("/avro/incident-detected.avsc")) {
            if (in == null) throw new IllegalStateException("Missing incident-detected.avsc");
            this.schema = new Schema.Parser().parse(in);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load IncidentDetected Avro schema", ex);
        }
    }

    public CompletableFuture<?> publish(IncidentCandidate incident) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("eventId", java.util.UUID.randomUUID().toString());
        record.put("schemaVersion", "1.0.0");
        record.put("occurredAt", Instant.now().toString());
        record.put("incidentId", incident.incidentId());
        record.put("title", incident.title());
        record.put("description", incident.description());
        record.put("severity", incident.severity());
        record.put("environment", incident.environment());
        record.put("primaryResourceId", incident.primaryResourceId());
        record.put("evidenceEventIds", incident.evidenceEventIds());
        record.put("detectorId", incident.detectorId());
        record.put("correlationId", incident.correlationId());
        record.put("causationId", incident.causationId());
        return kafkaTemplate.send(properties.getOutputTopic(), incident.incidentId(), record)
                .whenComplete((result, error) -> {
                    if (error == null) meterRegistry.counter("correlator.incidents.published").increment();
                    else meterRegistry.counter("correlator.incidents.publish.errors").increment();
                });
    }
}
