package com.agilesolutions.remediation.application;

import com.agilesolutions.remediation.domain.RemediationActionType;

import java.util.Map;

public record ExecutionCommand(String proposalId, String incidentId, RemediationActionType actionType,
                               String resourceId, String namespace, Map<String, String> parameters) {
}
