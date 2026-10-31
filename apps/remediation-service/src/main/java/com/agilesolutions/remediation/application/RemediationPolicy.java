package com.agilesolutions.remediation.application;

import com.agilesolutions.remediation.config.RemediationProperties;
import com.agilesolutions.remediation.domain.RemediationActionType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Component
public class RemediationPolicy {
    private final RemediationProperties properties;
    private static final Set<String> PARAMS = Set.of("replicas", "revision");

    public RemediationPolicy(RemediationProperties p) {
        properties = p;
    }

    public RemediationActionType validate(ApprovedRemediation a) {
        if (blank(a.proposalId()) || blank(a.incidentId()) || blank(a.resourceId()) || blank(a.namespace()) || blank(a.approvalId()) || blank(a.approvedBy()))
            throw new SecurityException("Goedkeuringsbericht mist verplichte velden");
        if (a.approvedAt() == null || a.validUntil() == null || !a.validUntil().isAfter(Instant.now()))
            throw new SecurityException("Goedkeuring ontbreekt of is verlopen");
        if (a.approvedAt().isAfter(Instant.now().plusSeconds(60)))
            throw new SecurityException("Goedkeuringstijdstip ligt in de toekomst");
        if (!properties.getAllowedNamespaces().contains(a.namespace()))
            throw new SecurityException("Namespace niet toegestaan");
        if (a.resourceId().length() > 120 || !a.resourceId().matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?"))
            throw new SecurityException("Ongeldige resource-naam");
        Map<String, String> p = a.parameters() == null ? Map.of() : a.parameters();
        if (!PARAMS.containsAll(p.keySet())) throw new SecurityException("Niet-toegestane actieparameters");
        RemediationActionType type;
        try {
            type = RemediationActionType.valueOf(a.actionType());
        } catch (Exception e) {
            throw new SecurityException("Actietype staat niet op allowlist");
        }
        switch (type) {
            case RESTART_WORKLOAD -> {
                if (!p.isEmpty()) throw new SecurityException("RESTART_WORKLOAD accepteert geen parameters");
            }
            case SCALE_WORKLOAD -> {
                int n = parse(p.get("replicas"), "replicas");
                if (n < 1 || n > 5) throw new SecurityException("replicas moet tussen 1 en 5 liggen");
            }
            case ROLLBACK_DEPLOYMENT -> {
                if (parse(p.get("revision"), "revision") < 1)
                    throw new SecurityException("revision moet positief zijn");
            }
        }
        return type;
    }

    private int parse(String s, String field) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            throw new SecurityException(field + " moet een integer zijn");
        }
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
