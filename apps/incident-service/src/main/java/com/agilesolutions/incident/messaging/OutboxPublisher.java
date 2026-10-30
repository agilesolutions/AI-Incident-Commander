package com.agilesolutions.incident.messaging;

import com.agilesolutions.incident.persistence.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper mapper;
    private final Map<String, Schema> schemas;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, Object> kafka,
                           ObjectMapper mapper, com.agilesolutions.incident.config.AvroSchemaRegistry registry) {
        this.repository = repository; this.kafka = kafka; this.mapper = mapper;
        this.schemas = Map.of(
            "IncidentUpdated", registry.get("incident-updated"),
            "InvestigationRequested", registry.get("investigation-requested"));
    }

    @Scheduled(fixedDelayString = "${incident.outbox.poll-delay-ms:1000}")
    public void publishPending() {
        for (var event : repository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                Map<String, Object> values = mapper.readValue(event.getPayload(), Map.class);
                Schema schema = schemas.get(event.getEventType());
                GenericRecord record = new GenericData.Record(schema);
                values.forEach((key, value) -> {
                    if (schema.getField(key) != null) record.put(key, value);
                });
                kafka.send(event.getTopic(), event.getMessageKey(), record).get();
                markPublished(event.getId());
            } catch (Exception ex) {
                markFailed(event.getId(), ex);
            }
        }
    }

    @Transactional
    protected void markPublished(String id) {
        repository.findById(id).ifPresent(e -> { e.markPublished(); repository.save(e); });
    }

    @Transactional
    protected void markFailed(String id, Throwable error) {
        repository.findById(id).ifPresent(e -> { e.markFailed(error); repository.save(e); });
    }
}
