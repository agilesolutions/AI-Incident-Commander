package com.agilesolutions.commandapi.dto;

import java.time.Instant;
import java.util.List;

public record IncidentResponse(
        String incidentId,
        String title,
        String description,
        String severity,
        String status,
        String environment,
        String primaryResourceId,
        List<String> evidenceEventIds,
        Instant createdAt,
        Instant updatedAt) {
}
