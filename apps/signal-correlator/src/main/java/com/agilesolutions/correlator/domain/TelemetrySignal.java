package com.agilesolutions.correlator.domain;

import org.apache.avro.generic.GenericRecord;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public record TelemetrySignal(
        String eventId, Instant occurredAt, String source, String environment,
        String resourceId, String resourceType, String signalType, String severity,
        String name, String description, Double value, String unit,
        Map<String, String> labels, String correlationId) {

    public static TelemetrySignal from(GenericRecord record) {
        Object rawValue = record.get("value");
        Double numericValue = rawValue == null ? null : ((Number) rawValue).doubleValue();
        Object rawLabels = record.get("labels");
        Map<String, String> labels = rawLabels instanceof Map<?, ?> map
                ? map.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()))
                : Map.of();
        Object rawCorrelation = record.get("correlationId");
        return new TelemetrySignal(
                record.get("eventId").toString(), Instant.parse(record.get("occurredAt").toString()),
                record.get("source").toString(), record.get("environment").toString(),
                record.get("resourceId").toString(), record.get("resourceType").toString(),
                record.get("signalType").toString(), record.get("severity").toString(),
                record.get("name").toString(), record.get("description").toString(), numericValue,
                record.get("unit") == null ? null : record.get("unit").toString(), labels,
                rawCorrelation == null ? null : rawCorrelation.toString());
    }

    public boolean isErrorSignal() {
        return "ERROR".equals(severity) || "CRITICAL".equals(severity);
    }

    public String correlationKey() { return environment + ":" + resourceId; }
}
