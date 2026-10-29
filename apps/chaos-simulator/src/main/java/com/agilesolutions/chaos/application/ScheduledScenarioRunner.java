package com.agilesolutions.chaos.application;

import com.agilesolutions.chaos.config.SimulatorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledScenarioRunner {
    private static final Logger log = LoggerFactory.getLogger(ScheduledScenarioRunner.class);
    private final SimulatorProperties properties;
    private final ChaosScenarioService scenarioService;

    public ScheduledScenarioRunner(SimulatorProperties properties, ChaosScenarioService scenarioService) {
        this.properties = properties;
        this.scenarioService = scenarioService;
    }

    @Scheduled(fixedDelayString = "${simulator.interval-ms:30000}")
    public void runScheduledScenario() {
        if (!properties.isEnabled()) return;
        try {
            log.info("Running configured demo chaos scenario");
            scenarioService.runDefaultScenario();
        } catch (RuntimeException exception) {
            log.error("Scheduled chaos scenario failed", exception);
        }
    }
}
