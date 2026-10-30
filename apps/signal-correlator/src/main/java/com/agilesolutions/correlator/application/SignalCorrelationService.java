package com.agilesolutions.correlator.application;

import com.agilesolutions.correlator.config.CorrelatorProperties;
import com.agilesolutions.correlator.domain.IncidentCandidate;
import com.agilesolutions.correlator.domain.TelemetrySignal;
import com.agilesolutions.correlator.messaging.IncidentDetectedPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Deterministic, in-memory sliding-window detector. */
@Service
public class SignalCorrelationService {
    private final CorrelatorProperties properties;
    private final IncidentDetectedPublisher publisher;
    private final MeterRegistry meterRegistry;
    private final Clock clock;
    private final Map<String, Deque<TelemetrySignal>> windows = new ConcurrentHashMap<>();
    private final Map<String, Instant> processedEventIds = new ConcurrentHashMap<>();
    private final Map<String, Instant> activeIncidentKeys = new ConcurrentHashMap<>();

    public SignalCorrelationService(CorrelatorProperties properties, IncidentDetectedPublisher publisher,
                                    MeterRegistry meterRegistry) {
        this(properties, publisher, meterRegistry, Clock.systemUTC());
    }

    SignalCorrelationService(CorrelatorProperties properties, IncidentDetectedPublisher publisher,
                             MeterRegistry meterRegistry, Clock clock) {
        this.properties = properties;
        this.publisher = publisher;
        this.meterRegistry = meterRegistry;
        this.clock = clock;
    }

    public void accept(TelemetrySignal signal) {
        Instant now = clock.instant();
        cleanup(now);
        if (processedEventIds.putIfAbsent(signal.eventId(), now) != null) {
            meterRegistry.counter("correlator.telemetry.duplicates").increment();
            return;
        }
        meterRegistry.counter("correlator.telemetry.received").increment();
        if (!signal.isErrorSignal()) return;
        if (!properties.getEnvironment().isBlank() && !properties.getEnvironment().equals(signal.environment())) return;

        Deque<TelemetrySignal> window = windows.computeIfAbsent(signal.correlationKey(), key -> new ArrayDeque<>());
        IncidentCandidate candidate = null;
        synchronized (window) {
            window.addLast(signal);
            Instant cutoff = now.minusSeconds(properties.getWindowSeconds());
            while (!window.isEmpty() && window.peekFirst().occurredAt().isBefore(cutoff)) window.removeFirst();
            if (window.size() >= properties.getThreshold()) {
                Instant lastDetected = activeIncidentKeys.get(signal.correlationKey());
                if (lastDetected == null || lastDetected.plusSeconds(properties.getDeduplicationTtlSeconds()).isBefore(now)) {
                    candidate = createCandidate(signal, List.copyOf(window));
                    activeIncidentKeys.put(signal.correlationKey(), now);
                    window.clear();
                }
            }
        }
        if (candidate != null) {
            meterRegistry.counter("correlator.incidents.detected").increment();
            publisher.publish(candidate).exceptionally(error -> {
                meterRegistry.counter("correlator.incidents.publish.errors").increment();
                return null;
            });
        }
    }

    private IncidentCandidate createCandidate(TelemetrySignal latest, List<TelemetrySignal> evidence) {
        String severity = evidence.stream().anyMatch(s -> "CRITICAL".equals(s.severity())) ? "CRITICAL" : "HIGH";
        String correlationId = latest.correlationId() == null || latest.correlationId().isBlank()
                ? UUID.nameUUIDFromBytes(latest.correlationKey().getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString()
                : latest.correlationId();
        String incidentId = UUID.nameUUIDFromBytes((latest.correlationKey() + ":" + evidence.getFirst().eventId())
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        List<String> evidenceIds = evidence.stream().map(TelemetrySignal::eventId).toList();
        String title = "Repeated error signals for " + latest.resourceId();
        String description = "Detected " + evidence.size() + " ERROR/CRITICAL telemetry events within "
                + properties.getWindowSeconds() + " seconds. Signal types: "
                + evidence.stream().map(TelemetrySignal::signalType).distinct().toList();
        return new IncidentCandidate(incidentId, title, description, severity, latest.environment(),
                latest.resourceId(), evidenceIds, properties.getDetectorId(), correlationId, latest.eventId());
    }

    private void cleanup(Instant now) {
        Instant dedupCutoff = now.minusSeconds(properties.getDeduplicationTtlSeconds());
        processedEventIds.entrySet().removeIf(e -> e.getValue().isBefore(dedupCutoff));
        activeIncidentKeys.entrySet().removeIf(e -> e.getValue().plusSeconds(properties.getDeduplicationTtlSeconds()).isBefore(now));
        Instant windowCutoff = now.minusSeconds(properties.getWindowSeconds());
        windows.values().forEach(window -> {
            synchronized (window) {
                while (!window.isEmpty() && window.peekFirst().occurredAt().isBefore(windowCutoff)) window.removeFirst();
            }
        });
    }
}
