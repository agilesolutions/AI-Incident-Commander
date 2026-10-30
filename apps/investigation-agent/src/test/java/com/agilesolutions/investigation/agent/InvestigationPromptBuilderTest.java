package com.agilesolutions.investigation.agent;

import com.agilesolutions.investigation.config.AgentProperties;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InvestigationPromptBuilderTest {
    @Test void promptWarnsThatEvidenceIdsAreReferencesNotEvidenceContent() {
        var props = new AgentProperties();
        var builder = new InvestigationPromptBuilder(props);
        var request = new InvestigationRequest("evt-1", "inc-1", "HIGH", "Find likely cause",
            List.of("telemetry-1"), "corr-1", null, "2026-10-30T10:00:00Z");
        String prompt = builder.build(request);
        assertTrue(prompt.contains("Bewijs-ID's zijn alleen verwijzingen"));
        assertTrue(prompt.contains("telemetry-1"));
        assertTrue(prompt.contains("menselijke goedkeuring"));
    }

    @Test void promptIsBounded() {
        var props = new AgentProperties();
        props.setMaxPromptChars(1000);
        var builder = new InvestigationPromptBuilder(props);
        var request = new InvestigationRequest("evt", "inc", "HIGH", "x".repeat(5000),
            List.of(), "corr", null, "now");
        assertEquals(1000, builder.build(request).length());
    }
}
