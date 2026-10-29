package com.agilesolutions.chaos.application;

import com.agilesolutions.chaos.config.SimulatorProperties;
import com.agilesolutions.chaos.domain.ScenarioRequest;
import com.agilesolutions.chaos.domain.ScenarioResult;
import com.agilesolutions.chaos.domain.ScenarioType;
import com.agilesolutions.chaos.messaging.TelemetryEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChaosScenarioServiceTest {
    private TelemetryEventPublisher publisher;
    private ChaosScenarioService service;

    @BeforeEach
    void setUp() {
        publisher = mock(TelemetryEventPublisher.class);
        when(publisher.publish(any())).thenReturn(CompletableFuture.completedFuture(null));
        service = new ChaosScenarioService(publisher, new SimulatorProperties());
    }

    @Test
    void publishesRequestedScenarioAndReturnsSubmission() {
        ScenarioResult result = service.run(new ScenarioRequest(ScenarioType.HIGH_LATENCY,
                "payments-service", "test", "payments-service"));

        assertThat(result.scenario()).isEqualTo(ScenarioType.HIGH_LATENCY);
        assertThat(result.resourceId()).isEqualTo("payments-service");
        assertThat(result.status()).isEqualTo("SUBMITTED");
        verify(publisher).publish(any());
    }

    @Test
    void suppliesDefaultsForMissingOptionalFields() {
        ScenarioRequest request = new ScenarioRequest(ScenarioType.NORMAL_RECOVERY, null, null, null);
        assertThat(request.resourceId()).isEqualTo("checkout-service");
        assertThat(request.environment()).isEqualTo("demo");
        assertThat(request.serviceName()).isEqualTo("checkout-service");
    }
}
