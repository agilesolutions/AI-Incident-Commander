package com.agilesolutions.investigation.agent;

import java.util.List;

public record InvestigationRequest(
    String eventId, String incidentId, String priority, String objective,
    List<String> evidenceEventIds, String correlationId, String causationId, String occurredAt
) {}
