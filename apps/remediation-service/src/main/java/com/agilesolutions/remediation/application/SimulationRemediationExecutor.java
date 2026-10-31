package com.agilesolutions.remediation.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "remediation.execution-mode", havingValue = "simulation", matchIfMissing = true)
public class SimulationRemediationExecutor implements RemediationExecutor {
    public ExecutionResult execute(ExecutionCommand c) {
        return new ExecutionResult(true, "simulation-" + UUID.randomUUID(), "Simulatie: actie geaccepteerd, maar niet op een cluster uitgevoerd.");
    }
}
