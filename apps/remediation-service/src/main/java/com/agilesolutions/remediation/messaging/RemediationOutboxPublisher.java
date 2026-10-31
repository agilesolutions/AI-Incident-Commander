package com.agilesolutions.remediation.messaging;

import com.agilesolutions.remediation.config.AvroSchemaRegistry;
import com.agilesolutions.remediation.persistence.RemediationOutboxRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RemediationOutboxPublisher {
    private final RemediationOutboxRepository repo;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper mapper;
    private final Map<String, Schema> schemas;

    public RemediationOutboxPublisher(RemediationOutboxRepository r, KafkaTemplate<String, Object> k, ObjectMapper m, AvroSchemaRegistry s) {
        repo = r;
        kafka = k;
        mapper = m;
        schemas = Map.of("executed", s.get("remediation-executed"), "failed", s.get("remediation-failed"), "verified", s.get("remediation-verified"));
    }

    @Scheduled(fixedDelayString = "${remediation.outbox-poll-delay-ms:1000}")
    public void publishPending() {
        for (var event : repo.findTop30ByPublishedAtIsNullOrderByCreatedAtAsc())
            try {
                String kind = event.getTopic().endsWith("executed.v1") ? "executed" : event.getTopic().endsWith("failed.v1") ? "failed" : "verified";
                Map<String, Object> values = mapper.readValue(event.getPayload(), new TypeReference<>() {
                });
                Schema schema = schemas.get(kind);
                GenericRecord record = new GenericData.Record(schema);
                for (Schema.Field f : schema.getFields()) {
                    Object v = values.get(f.name());
                    if (v != null) record.put(f.name(), v);
                }
                kafka.send(event.getTopic(), event.getMessageKey(), record).get();
                repo.findById(event.getId()).ifPresent(e -> {
                    e.published();
                    repo.save(e);
                });
            } catch (Exception ex) {
                repo.findById(event.getId()).ifPresent(e -> {
                    e.failed(ex);
                    repo.save(e);
                });
            }
    }
}
