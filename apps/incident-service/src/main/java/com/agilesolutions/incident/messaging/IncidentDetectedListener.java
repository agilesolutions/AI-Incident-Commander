package com.agilesolutions.incident.messaging;

import com.agilesolutions.incident.application.IncidentApplicationService;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class IncidentDetectedListener {
    private final IncidentApplicationService service;
    private final MeterRegistry metrics;
    public IncidentDetectedListener(IncidentApplicationService service, MeterRegistry metrics) {
        this.service = service; this.metrics = metrics;
    }

    @KafkaListener(topics = "${incident.detected-topic:incident.detected.v1}")
    public void onIncidentDetected(GenericRecord event) {
        service.acceptDetectedEvent(event);
        metrics.counter("incident.events.consumed", "type", "detected").increment();
    }
}
