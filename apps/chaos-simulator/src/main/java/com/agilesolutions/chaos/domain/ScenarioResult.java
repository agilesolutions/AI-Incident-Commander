package com.agilesolutions.chaos.domain;

import java.time.Instant;
import java.util.UUID;

public record ScenarioResult(UUID runId, ScenarioType scenario, String resourceId,
                             Instant startedAt, String status, String message) { }
