package com.agilesolutions.remediation.application;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ActionDigestServiceTest {
    private ApprovedRemediation req(Map<String, String> p) {
        return new ApprovedRemediation("evt", "p1", "i1", "SCALE_WORKLOAD", "web", "default", p, "x", "a1", "operator", Instant.parse("2026-10-31T10:00:00Z"), Instant.parse("2026-10-31T11:00:00Z"), "c1", "c0");
    }

    @Test
    void digestIndependentOfMapInsertionOrder() {
        var d = new ActionDigestService();
        assertEquals(d.digest(req(Map.of("replicas", "2", "region", "x"))), d.digest(req(Map.of("region", "x", "replicas", "2"))));
    }

    @Test
    void digestChangesWhenParametersChange() {
        var d = new ActionDigestService();
        assertNotEquals(d.digest(req(Map.of("replicas", "2"))), d.digest(req(Map.of("replicas", "3"))));
    }
}
