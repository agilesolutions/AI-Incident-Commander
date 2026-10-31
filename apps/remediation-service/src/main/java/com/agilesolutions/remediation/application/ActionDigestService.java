package com.agilesolutions.remediation.application;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Component
public class ActionDigestService {
    public String digest(ApprovedRemediation a) {
        try {
            String params = a.parameters() == null ? "" : new TreeMap<>(a.parameters()).entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).reduce((x, y) -> x + ";" + y).orElse("");
            String canonical = String.join("|", safe(a.proposalId()), safe(a.incidentId()), safe(a.actionType()), safe(a.resourceId()), safe(a.namespace()), params);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Digest berekenen mislukt", e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public boolean matches(String expected, String actual) {
        return expected != null && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }
}
