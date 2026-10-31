package com.agilesolutions.remediation.application;

import com.agilesolutions.remediation.config.RemediationProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RemediationPolicyTest {
    private ApprovedRemediation req(String type, String ns, Map<String, String> p) {
        return new ApprovedRemediation("evt", "p1", "i1", type, "web", ns, p, "digest", "a1", "operator", Instant.now(), Instant.now().plusSeconds(300), "c1", "c0");
    }

    @Test
    void onlyAllowlistedActionsAndNamespaces() {
        var p = new RemediationPolicy(new RemediationProperties());
        assertEquals("RESTART_WORKLOAD", p.validate(req("RESTART_WORKLOAD", "default", Map.of())).name());
        assertThrows(SecurityException.class, () -> p.validate(req("DELETE_NAMESPACE", "default", Map.of())));
        assertThrows(SecurityException.class, () -> p.validate(req("RESTART_WORKLOAD", "kube-system", Map.of())));
    }

    @Test
    void scaleIsBounded() {
        var p = new RemediationPolicy(new RemediationProperties());
        assertThrows(SecurityException.class, () -> p.validate(req("SCALE_WORKLOAD", "default", Map.of("replicas", "20"))));
        assertDoesNotThrow(() -> p.validate(req("SCALE_WORKLOAD", "default", Map.of("replicas", "3"))));
    }

    @Test
    void expiredApprovalIsRejected() {
        var p = new RemediationPolicy(new RemediationProperties());
        var a = new ApprovedRemediation("e", "p", "i", "RESTART_WORKLOAD", "web", "default", Map.of(), "d", "a", "op", Instant.now().minusSeconds(100), Instant.now().minusSeconds(1), "c", "x");
        assertThrows(SecurityException.class, () -> p.validate(a));
    }
}
