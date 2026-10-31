package com.agilesolutions.remediation.application;

import java.time.Instant;
import java.util.Map;

public record ApprovedRemediation(String eventId, String proposalId, String incidentId, String actionType,
                                  String resourceId, String namespace,
                                  Map<String, String> parameters, String actionDigest, String approvalId,
                                  String approvedBy, Instant approvedAt, Instant validUntil, String correlationId,
                                  String causationId) {
}
