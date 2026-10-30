package com.agilesolutions.investigation.messaging;

import com.agilesolutions.investigation.config.AvroSchemaRegistry;
import com.agilesolutions.investigation.persistence.InvestigationOutboxRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Component
public class InvestigationOutboxPublisher {
    private final InvestigationOutboxRepository repository;
    private final KafkaTemplate<String,Object> kafka;
    private final ObjectMapper mapper;
    private final Schema schema;

    public InvestigationOutboxPublisher(InvestigationOutboxRepository repository, KafkaTemplate<String,Object> kafka,
            ObjectMapper mapper, AvroSchemaRegistry registry) {
        this.repository = repository; this.kafka = kafka; this.mapper = mapper;
        this.schema = registry.get("investigation-completed");
    }

    @Scheduled(fixedDelayString = "${investigation.outbox.poll-delay-ms:1000}")
    public void publishPending() {
        List<com.agilesolutions.investigation.domain.InvestigationOutboxEvent> pending =
            repository.findTop30ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (var event : pending) {
            try {
                Map<String,Object> values = mapper.readValue(event.getPayload(), new TypeReference<>() {});
                GenericRecord record = new GenericData.Record(schema);
                for (Schema.Field field : schema.getFields()) {
                    Object value = values.get(field.name());
                    if (value instanceof List<?> list && field.schema().getType() == Schema.Type.ARRAY)
                        value = list.stream().map(Object::toString).toList();
                    if (value != null) record.put(field.name(), value);
                }
                kafka.send(event.getTopic(), event.getMessageKey(), record).get();
                markPublished(event.getId());
            } catch (Exception ex) {
                markFailed(event.getId(), ex);
            }
        }
    }

    @Transactional
    protected void markPublished(String id) {
        repository.findById(id).ifPresent(e -> { e.published(); repository.save(e); });
    }
    @Transactional
    protected void markFailed(String id, Throwable ex) {
        repository.findById(id).ifPresent(e -> { e.failed(ex); repository.save(e); });
    }
}
