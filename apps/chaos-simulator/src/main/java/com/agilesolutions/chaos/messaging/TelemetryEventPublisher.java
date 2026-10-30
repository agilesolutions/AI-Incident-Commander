package com.agilesolutions.chaos.messaging;

import com.agilesolutions.chaos.config.SimulatorProperties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class TelemetryEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimulatorProperties properties;
    private final Schema schema;

    public TelemetryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                   SimulatorProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
        try (InputStream input = getClass().getResourceAsStream("/avro/telemetry-event.avsc")) {
            if (input == null) throw new IllegalStateException("Missing Avro schema: telemetry-event.avsc");
            this.schema = new Schema.Parser().parse(input);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load TelemetryEvent Avro schema", e);
        }
    }

    public CompletableFuture<?> publish(TelemetrySignal signal) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("eventId", signal.eventId().toString());
        record.put("schemaVersion", "1.0.0");
        record.put("occurredAt", signal.occurredAt().toString());
        record.put("source", properties.getSource());
        record.put("environment", signal.environment());
        record.put("resourceId", signal.resourceId());
        record.put("resourceType", "SERVICE");
        record.put("signalType", signal.signalType());
        record.put("severity", signal.severity().name());
        record.put("name", signal.name());
        record.put("description", signal.description());
        record.put("value", signal.value());
        record.put("unit", signal.unit());
        record.put("labels", signal.labels());
        record.put("correlationId", signal.correlationId() == null ? null : signal.correlationId().toString());
        return kafkaTemplate.send(properties.getTopic(), signal.resourceId(), record);
    }

    public record TelemetrySignal(UUID eventId, Instant occurredAt, String environment,
                                  String resourceId, String signalType, String name,
                                  String description, double value, String unit,
                                  Map<String, String> labels,
                                  com.agilesolutions.chaos.domain.TelemetrySeverity severity,
                                  UUID correlationId) { }
}
