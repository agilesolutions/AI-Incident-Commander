package com.agilesolutions.chaos.application;

import com.agilesolutions.chaos.config.SimulatorProperties;
import com.agilesolutions.chaos.domain.ScenarioRequest;
import com.agilesolutions.chaos.domain.ScenarioResult;
import com.agilesolutions.chaos.domain.ScenarioType;
import com.agilesolutions.chaos.domain.TelemetrySeverity;
import com.agilesolutions.chaos.messaging.TelemetryEventPublisher;
import com.agilesolutions.chaos.messaging.TelemetryEventPublisher.TelemetrySignal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ChaosScenarioService {
    private final TelemetryEventPublisher publisher;
    private final SimulatorProperties properties;

    public ChaosScenarioService(TelemetryEventPublisher publisher, SimulatorProperties properties) {
        this.publisher = publisher;
        this.properties = properties;
    }

    public ScenarioResult run(ScenarioRequest request) {
        UUID runId = UUID.randomUUID();
        Instant now = Instant.now();
        String resource = request.resourceId();
        String environment = request.environment();
        ScenarioDefinition definition = definition(request.scenario());
        TelemetrySignal signal = new TelemetrySignal(
                UUID.randomUUID(), now, environment, resource,
                definition.signalType(), definition.name(), definition.description(),
                definition.value(), definition.unit(),
                Map.of("scenario", request.scenario().name(), "service", request.serviceName(), "runId", runId.toString()),
                definition.severity(), runId);
        CompletableFuture<?> send = publisher.publish(signal);
        // Do not block the HTTP request on Kafka broker acknowledgement.
        send.whenComplete((result, error) -> {
            if (error != null) org.slf4j.LoggerFactory.getLogger(getClass())
                    .error("Failed to publish telemetry for scenario {} (runId={})", request.scenario(), runId, error);
        });
        return new ScenarioResult(runId, request.scenario(), resource, now, "SUBMITTED",
                "Publicatieverzoek ingediend; controleer de applicatielogs voor de Kafka-bevestiging of eventuele fouten.");
    }

    public ScenarioResult runDefaultScenario() {
        return run(new ScenarioRequest(ScenarioType.HIGH_ERROR_RATE, "checkout-service",
                properties.getEnvironment(), "checkout-service"));
    }

    private ScenarioDefinition definition(ScenarioType scenario) {
        return switch (scenario) {
            case HIGH_ERROR_RATE -> new ScenarioDefinition("HTTP_ERROR_RATE", "Verhoogd HTTP 5xx-percentage",
                    "Gesimuleerde stijging van HTTP 5xx-fouten na een deployment.", 18.5, "%", TelemetrySeverity.CRITICAL);
            case HIGH_LATENCY -> new ScenarioDefinition("HTTP_LATENCY", "Verhoogde HTTP-latency",
                    "Gesimuleerde stijging van de p95-responstijd.", 2400, "ms", TelemetrySeverity.ERROR);
            case FAILED_DEPLOYMENT -> new ScenarioDefinition("DEPLOYMENT_FAILURE", "Deployment mislukt",
                    "Gesimuleerde mislukte deployment; er wordt geen echte deployment uitgevoerd.", 1, "failure", TelemetrySeverity.ERROR);
            case RESOURCE_PRESSURE -> new ScenarioDefinition("CPU_UTILIZATION", "Hoge CPU-belasting",
                    "Gesimuleerde hoge CPU-belasting van een applicatieservice.", 96, "%", TelemetrySeverity.CRITICAL);
            case NORMAL_RECOVERY -> new ScenarioDefinition("HTTP_ERROR_RATE", "Herstel van foutpercentage",
                    "Gesimuleerde terugkeer naar normale foutpercentages.", 0.2, "%", TelemetrySeverity.INFO);
        };
    }

    private record ScenarioDefinition(String signalType, String name, String description,
                                      double value, String unit, TelemetrySeverity severity) { }
}
