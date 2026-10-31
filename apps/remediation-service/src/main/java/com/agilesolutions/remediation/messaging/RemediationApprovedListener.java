package com.agilesolutions.remediation.messaging;

import com.agilesolutions.remediation.application.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
public class RemediationApprovedListener {
    private final RemediationOrchestrator orchestrator;
    private final MeterRegistry metrics;

    public RemediationApprovedListener(RemediationOrchestrator o, MeterRegistry m) {
        orchestrator = o;
        metrics = m;
    }

    @KafkaListener(topics = "${remediation.approved-topic:incident.remediation.approved.v1}")
    public void approved(GenericRecord r) {
        orchestrator.handleApproved(map(r));
        metrics.counter("remediation.approvals.consumed").increment();
    }

    @KafkaListener(topics = "${remediation.rejected-topic:incident.remediation.rejected.v1}")
    public void rejected(GenericRecord r) {
        orchestrator.handleRejected(str(r, "eventId"), str(r, "proposalId"));
        metrics.counter("remediation.rejections.consumed").increment();
    }

    private ApprovedRemediation map(GenericRecord r) {
        Map<String, String> params = new LinkedHashMap<>();
        Object raw = r.get("parameters");
        if (raw instanceof Map<?, ?> m) m.forEach((k, v) -> params.put(String.valueOf(k), String.valueOf(v)));
        return new ApprovedRemediation(str(r, "eventId"), str(r, "proposalId"), str(r, "incidentId"), str(r, "actionType"), str(r, "resourceId"), str(r, "namespace"), params,
                str(r, "approvedActionDigest"), str(r, "approvalId"), str(r, "approvedBy"), Instant.parse(str(r, "approvedAt")), Instant.parse(str(r, "validUntil")), nullable(r, "correlationId"), nullable(r, "causationId"));
    }

    private String str(GenericRecord r, String f) {
        Object v = r.get(f);
        if (v == null) throw new IllegalArgumentException("Verplicht veld ontbreekt: " + f);
        return v.toString();
    }

    private String nullable(GenericRecord r, String f) {
        Object v = r.get(f);
        return v == null ? null : v.toString();
    }
}
