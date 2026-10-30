package com.agilesolutions.correlator.messaging;

import com.agilesolutions.correlator.application.SignalCorrelationService;
import com.agilesolutions.correlator.domain.TelemetrySignal;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryEventListener {
    private final SignalCorrelationService correlationService;
    private final MeterRegistry meterRegistry;

    public TelemetryEventListener(SignalCorrelationService correlationService, MeterRegistry meterRegistry) {
        this.correlationService = correlationService;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "${correlator.input-topic}", containerFactory = "kafkaListenerContainerFactory")
    public void onTelemetry(GenericRecord record) {
        try {
            correlationService.accept(TelemetrySignal.from(record));
        } catch (RuntimeException ex) {
            meterRegistry.counter("correlator.telemetry.processing.errors").increment();
            throw ex;
        }
    }
}
