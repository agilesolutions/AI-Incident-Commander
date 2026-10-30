package com.agilesolutions.chaos.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScenarioRequest(
        @NotNull ScenarioType scenario,
        @Size(max = 100) String resourceId,
        @Size(max = 100) String environment,
        @Size(max = 100) String serviceName
) {
    public ScenarioRequest {
        if (resourceId == null || resourceId.isBlank()) resourceId = "checkout-service";
        if (environment == null || environment.isBlank()) environment = "demo";
        if (serviceName == null || serviceName.isBlank()) serviceName = "checkout-service";
    }
}
