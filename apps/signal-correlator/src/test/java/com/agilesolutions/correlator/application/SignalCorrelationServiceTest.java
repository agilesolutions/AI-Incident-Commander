package com.agilesolutions.correlator.application;

import com.agilesolutions.correlator.config.CorrelatorProperties;
import com.agilesolutions.correlator.domain.TelemetrySignal;
import com.agilesolutions.correlator.messaging.IncidentDetectedPublisher;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.mockito.Mockito.*;

class SignalCorrelationServiceTest {
    private CorrelatorProperties properties;
    private IncidentDetectedPublisher publisher;
    private SignalCorrelationService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        properties = new CorrelatorProperties();
        properties.setThreshold(3);
        properties.setWindowSeconds(120);
        properties.setDeduplicationTtlSeconds(900);
        publisher = mock(IncidentDetectedPublisher.class);
        clock = Clock.fixed(Instant.parse("2026-10-30T10:00:00Z"), ZoneOffset.UTC);
        service = new SignalCorrelationService(properties, publisher, new SimpleMeterRegistry(), clock);
    }

    @Test
    void publishesIncidentAfterThresholdOfErrorSignals() {
        service.accept(signal("e1", "ERROR", "checkout"));
        service.accept(signal("e2", "ERROR", "checkout"));
        verifyNoInteractions(publisher);

        service.accept(signal("e3", "CRITICAL", "checkout"));

        verify(publisher, times(1)).publish(argThat(candidate ->
                candidate.evidenceEventIds().size() == 3
                        && candidate.severity().equals("CRITICAL")
                        && candidate.primaryResourceId().equals("checkout")));
    }

    @Test
    void ignoresDuplicateEventIds() {
        TelemetrySignal event = signal("same-id", "ERROR", "checkout");
        service.accept(event);
        service.accept(event);
        service.accept(signal("e3", "ERROR", "checkout"));
        verifyNoInteractions(publisher);
    }

    @Test
    void doesNotCorrelateDifferentResources() {
        service.accept(signal("e1", "ERROR", "checkout"));
        service.accept(signal("e2", "ERROR", "payments"));
        service.accept(signal("e3", "ERROR", "checkout"));
        verifyNoInteractions(publisher);
    }

    @Test
    void ignoresInformationalSignals() {
        service.accept(signal("e1", "WARN", "checkout"));
        service.accept(signal("e2", "ERROR", "checkout"));
        service.accept(signal("e3", "ERROR", "checkout"));
        verifyNoInteractions(publisher);
    }

    private TelemetrySignal signal(String id, String severity, String resource) {
        return new TelemetrySignal(id, clock.instant(), "chaos-simulator", "demo", resource,
                "SERVICE", "HTTP_5XX_RATE", severity, "HTTP 5xx rate", "Synthetic test signal",
                0.25, "ratio", Map.of(), null);
    }
}
