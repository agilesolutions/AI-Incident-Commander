package com.agilesolutions.chaos.api;

import com.agilesolutions.chaos.application.ChaosScenarioService;
import com.agilesolutions.chaos.domain.ScenarioRequest;
import com.agilesolutions.chaos.domain.ScenarioResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chaos/scenarios")
@Tag(name = "Chaos Scenarios", description = "Genereert synthetische telemetrie-events voor demonstratie en tests.")
public class ChaosScenarioController {
    private final ChaosScenarioService scenarioService;

    public ChaosScenarioController(ChaosScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Start een scenario", description = "Publiceert een synthetisch telemetrie-event naar Kafka. Voert geen echte infrastructuurwijzigingen uit.")
    public ScenarioResult runScenario(@Valid @RequestBody ScenarioRequest request) {
        return scenarioService.run(request);
    }
}
