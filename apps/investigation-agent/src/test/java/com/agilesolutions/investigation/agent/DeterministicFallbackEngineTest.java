package com.agilesolutions.investigation.agent;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DeterministicFallbackEngineTest {
    @Test void fallbackDoesNotInventRootCauseAndRequiresHumanReview() {
        var request = new InvestigationRequest("e1", "i1", "HIGH", "investigate",
            List.of("signal-1"), "c1", null, "2026-10-30T10:00:00Z");
        var report = new DeterministicFallbackEngine().investigate(request);
        assertTrue(report.humanReviewRequired());
        assertTrue(report.confidence() < 0.1);
        assertTrue(report.probableRootCause().contains("Onvoldoende bewijs"));
        assertEquals(List.of("signal-1"), report.supportingEvidence());
    }
}
