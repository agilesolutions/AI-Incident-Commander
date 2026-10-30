package com.agilesolutions.incident.config;

import org.apache.avro.Schema;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AvroSchemaRegistry {
    private final ConcurrentHashMap<String, Schema> schemas = new ConcurrentHashMap<>();
    public Schema get(String name) {
        return schemas.computeIfAbsent(name, key -> {
            try (InputStream in = getClass().getResourceAsStream("/avro/" + key + ".avsc")) {
                if (in == null) throw new IllegalStateException("Avro-schema ontbreekt: " + key);
                return new Schema.Parser().parse(in);
            } catch (Exception e) {
                throw new IllegalStateException("Kan Avro-schema niet laden: " + key, e);
            }
        });
    }
}
