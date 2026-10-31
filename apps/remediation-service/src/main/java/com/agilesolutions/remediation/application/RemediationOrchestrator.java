package com.agilesolutions.remediation.application;

import com.agilesolutions.remediation.config.RemediationProperties;
import com.agilesolutions.remediation.domain.*;
import com.agilesolutions.remediation.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class RemediationOrchestrator {
    private final RemediationExecutionRepository executions;
    private final ProcessedMessageRepository processed;
    private final RemediationOutboxRepository outbox;
    private final RemediationPolicy policy;
    private final ActionDigestService digests;
    private final RemediationExecutor executor;
    private final RemediationVerifier verifier;
    private final ObjectMapper mapper;
    private final RemediationProperties props;

    public RemediationOrchestrator(RemediationExecutionRepository e, ProcessedMessageRepository p, RemediationOutboxRepository o, RemediationPolicy policy,
                                   ActionDigestService d, RemediationExecutor x, RemediationVerifier v, ObjectMapper m, RemediationProperties props) {
        executions = e;
        processed = p;
        outbox = o;
        this.policy = policy;
        digests = d;
        executor = x;
        verifier = v;
        mapper = m;
        this.props = props;
    }

    public void handleApproved(ApprovedRemediation a) {
        if (a.eventId() == null || a.eventId().isBlank()) throw new IllegalArgumentException("eventId ontbreekt");
        if (processed.existsById(a.eventId())) return;
        if (executions.existsById(a.proposalId())) {
            processed.save(new ProcessedMessage(a.eventId()));
            return;
        }
        RemediationExecution record = null;
        try {
            var type = policy.validate(a);
            String calculated = digests.digest(a);
            if (!digests.matches(a.actionDigest(), calculated))
                throw new SecurityException("approvedActionDigest komt niet overeen met goedgekeurde actie");
            record = new RemediationExecution(a.proposalId(), a.incidentId(), type, a.resourceId(), calculated, a.approvalId(), a.approvedBy(), a.approvedAt());
            executions.save(record);
            var command = new ExecutionCommand(a.proposalId(), a.incidentId(), type, a.resourceId(), a.namespace(), a.parameters() == null ? Map.of() : Map.copyOf(a.parameters()));
            record.validating();
            executions.save(record);
            record.executing();
            executions.save(record);
            ExecutionResult result = executor.execute(command);
            if (!result.accepted()) throw new IllegalStateException("Executor weigerde actie: " + result.message());
            record.executed(result.executionReference(), result.message());
            executions.save(record);
            enqueue(props.getExecutedTopic(), a, "EXECUTED", result.message(), result.executionReference());
            VerificationResult check = verifier.verify(command, result);
            record.verified(check.verified(), check.message());
            executions.save(record);
            enqueue(props.getVerifiedTopic(), a, check.verified() ? "VERIFIED" : "UNVERIFIED", check.message(), result.executionReference());
            processed.save(new ProcessedMessage(a.eventId()));
        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            if (msg.length() > 1900) msg = msg.substring(0, 1900);
            if (record != null) {
                record.failed(msg);
                executions.save(record);
            }
            enqueueFailure(a, msg);
            processed.save(new ProcessedMessage(a.eventId()));
            if (!(ex instanceof SecurityException))
                throw new IllegalStateException("Remediation-uitvoering mislukt", ex);
        }
    }

    public void handleRejected(String eventId, String proposalId) {
        if (eventId == null || processed.existsById(eventId)) return;
        processed.save(new ProcessedMessage(eventId));
    }

    private void enqueue(String topic, ApprovedRemediation a, String status, String message, String ref) throws Exception {
        Map<String, Object> p = common(a);
        p.put("status", status);
        p.put("message", message == null ? "" : message);
        p.put("executionReference", ref);
        outbox.save(new RemediationOutboxEvent(topic, a.incidentId(), mapper.writeValueAsString(p)));
    }

    private void enqueueFailure(ApprovedRemediation a, String message) {
        try {
            Map<String, Object> p = common(a);
            p.put("status", "FAILED");
            p.put("message", message);
            outbox.save(new RemediationOutboxEvent(props.getFailedTopic(), a.incidentId(), mapper.writeValueAsString(p)));
        } catch (Exception e) {
            throw new IllegalStateException("Failure-event kon niet worden opgeslagen", e);
        }
    }

    private Map<String, Object> common(ApprovedRemediation a) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("eventId", UUID.randomUUID().toString());
        p.put("schemaVersion", "1.0.0");
        p.put("occurredAt", Instant.now().toString());
        p.put("incidentId", a.incidentId());
        p.put("proposalId", a.proposalId());
        p.put("actionType", a.actionType());
        p.put("resourceId", a.resourceId());
        p.put("approvalId", a.approvalId());
        p.put("approvedBy", a.approvedBy());
        p.put("approvedActionDigest", a.actionDigest());
        p.put("correlationId", a.correlationId());
        p.put("causationId", a.eventId());
        return p;
    }
}
