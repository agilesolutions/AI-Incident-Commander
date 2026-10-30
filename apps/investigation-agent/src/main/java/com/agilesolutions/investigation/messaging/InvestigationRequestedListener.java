package com.agilesolutions.investigation.messaging;

import com.agilesolutions.investigation.agent.InvestigationRequest;
import com.agilesolutions.investigation.application.InvestigationOrchestrator;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class InvestigationRequestedListener {
    private final InvestigationOrchestrator orchestrator;
    private final MeterRegistry metrics;
    public InvestigationRequestedListener(InvestigationOrchestrator orchestrator, MeterRegistry metrics) {
        this.orchestrator = orchestrator; this.metrics = metrics;
    }

    @KafkaListener(topics = "${investigation.requested-topic:incident.investigation.requested.v1}")
    public void onMessage(GenericRecord record) {
        List<String> ids = new ArrayList<>();
        Object evidence = record.get("evidenceEventIds");
        if (evidence instanceof Iterable<?> values) values.forEach(v -> ids.add(String.valueOf(v)));
        InvestigationRequest request = new InvestigationRequest(
            str(record, "eventId"), str(record, "incidentId"), str(record, "priority"),
            str(record, "objective"), ids, str(record, "correlationId"),
            nullable(record, "causationId"), str(record, "occurredAt")
        );
        orchestrator.handle(request);
        metrics.counter("investigation.requests.processed").increment();
    }
    private String str(GenericRecord r, String field) {
        Object v = r.get(field);
        if (v == null) throw new IllegalArgumentException("Verplicht veld ontbreekt: " + field);
        return v.toString();
    }
    private String nullable(GenericRecord r, String field) {
        Object v = r.get(field); return v == null ? null : v.toString();
    }
}
