package com.agilesolutions.remediation.config;

import io.confluent.kafka.serializers.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.*;

@Configuration
public class KafkaConfiguration {
    @Bean
    ConsumerFactory<String, Object> remediationConsumerFactory(KafkaProperties k, RemediationProperties p) {
        Map<String, Object> m = new HashMap<>(k.buildConsumerProperties());
        m.put(ConsumerConfig.GROUP_ID_CONFIG, p.getConsumerGroup());
        m.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        m.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        m.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        m.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        m.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);
        m.put("schema.registry.url", p.getSchemaRegistryUrl());
        m.put("specific.avro.reader", false);
        return new DefaultKafkaConsumerFactory<>(m);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(ConsumerFactory<String, Object> cf) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        f.setConsumerFactory(cf);
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return f;
    }

    @Bean
    ProducerFactory<String, Object> remediationProducerFactory(KafkaProperties k, RemediationProperties p) {
        Map<String, Object> m = new HashMap<>(k.buildProducerProperties());
        m.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        m.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        m.put("schema.registry.url", p.getSchemaRegistryUrl());
        m.put("auto.register.schemas", true);
        return new DefaultKafkaProducerFactory<>(m);
    }

    @Bean
    KafkaTemplate<String, Object> remediationKafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}
