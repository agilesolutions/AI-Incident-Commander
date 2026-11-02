package com.agilesolutions.chaos.application;

import com.agilesolutions.chaos.config.SimulatorProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ScheduledScenarioRunner {

    private final SimulatorProperties properties;
    private final ChaosScenarioService scenarioService;

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
