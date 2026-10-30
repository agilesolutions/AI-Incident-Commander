package com.agilesolutions.investigation.agent;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(name = "investigation.engine", havingValue = "fallback")
public class DeterministicFallbackEngine implements InvestigationEngine {
    @Override
    public InvestigationReport investigate(InvestigationRequest r) {
        return new InvestigationReport(
            "Er is een incident ontvangen, maar de deterministische fallback beschikt niet over ruwe telemetry.",
            "Onvoldoende bewijs om een root cause vast te stellen.",
            0.05,
            r.evidenceEventIds() == null ? List.of() : r.evidenceEventIds(),
            List.of("Ruwe logs, metrics en deploymentinformatie ontbreken in de onderzoeksopdracht."),
            List.of("Verzamel tijdgebonden logs en metrics.", "Controleer recente deployments en configuratiewijzigingen.",
                    "Laat een bevoegde engineer de bevindingen beoordelen."),
            r.priority() == null ? "ONBEKEND" : r.priority(),
            true
        );
    }
}
