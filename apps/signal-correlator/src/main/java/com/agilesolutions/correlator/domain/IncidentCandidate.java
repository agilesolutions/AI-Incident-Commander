package com.agilesolutions.correlator.domain;

import java.util.List;

public record IncidentCandidate(
        String incidentId, String title, String description, String severity,
        String environment, String primaryResourceId, List<String> evidenceEventIds,
        String detectorId, String correlationId, String causationId) { }
