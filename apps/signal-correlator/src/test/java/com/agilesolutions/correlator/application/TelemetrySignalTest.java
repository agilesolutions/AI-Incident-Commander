package com.agilesolutions.correlator.application;

import com.agilesolutions.correlator.domain.TelemetrySignal;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TelemetrySignalTest {
    @Test
    void mapsSharedAvroTelemetryContract() throws Exception {
        Schema schema;
        try (InputStream in = getClass().getResourceAsStream("/avro/telemetry-event.avsc")) {
            schema = new Schema.Parser().parse(in);
        }
        GenericRecord record = new GenericData.Record(schema);
        record.put("eventId", "evt-1");
        record.put("schemaVersion", "1.0.0");
        record.put("occurredAt", "2026-10-30T10:00:00Z");
        record.put("source", "chaos-simulator");
        record.put("environment", "demo");
        record.put("resourceId", "checkout");
        record.put("resourceType", "SERVICE");
        record.put("signalType", "HTTP_5XX_RATE");
        record.put("severity", new GenericData.EnumSymbol(schema.getField("severity").schema(), "ERROR"));
        record.put("name", "HTTP 5xx rate");
        record.put("description", "Elevated errors");
        record.put("value", 0.25d);
        record.put("unit", "ratio");
        record.put("labels", Map.of("region", "test"));
        record.put("correlationId", null);

        TelemetrySignal signal = TelemetrySignal.from(record);
        assertThat(signal.eventId()).isEqualTo("evt-1");
        assertThat(signal.isErrorSignal()).isTrue();
        assertThat(signal.labels()).containsEntry("region", "test");
    }
}
