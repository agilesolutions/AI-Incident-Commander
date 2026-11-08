package com.agilesolutions.incident.api;

import com.agilesolutions.incident.application.IncidentApplicationService;
import com.agilesolutions.incident.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(
        name = "Incidents",
        description = "Incident lifecycle, evidence retrieval, investigation requests and status transitions."
)
public class IncidentController {

    private final IncidentApplicationService service;

    public IncidentController(IncidentApplicationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "List incidents",
            description = "Returns incidents, optionally filtered by their current lifecycle status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incidents retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid status filter", content = @Content)
    })
    public List<IncidentResponse> list(
            @Parameter(
                    description = "Optional incident lifecycle status filter",
                    example = "NEW"
            )
            @RequestParam(required = false) IncidentStatus status
    ) {
        return service.list(status).stream().map(IncidentResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get incident",
            description = "Returns the incident identified by its incident ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found", content = @Content)
    })
    public IncidentResponse get(
            @Parameter(description = "Unique incident identifier", required = true, example = "incident-123")
            @PathVariable String id
    ) {
        return IncidentResponse.from(service.getOrThrow(id));
    }

    @GetMapping("/{id}/evidence")
    @Operation(
            summary = "List incident evidence",
            description = "Returns the evidence references recorded for the specified incident."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Evidence retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EvidenceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Incident not found", content = @Content)
    })
    public List<EvidenceResponse> evidence(
            @Parameter(description = "Unique incident identifier", required = true, example = "incident-123")
            @PathVariable String id
    ) {
        return service.evidence(id).stream().map(EvidenceResponse::from).toList();
    }

    @PostMapping("/{id}/investigations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Request an incident investigation",
            description = "Publishes an investigation request for the incident and returns the updated incident representation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Investigation request accepted",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid investigation request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Incident not found", content = @Content)
    })
    public IncidentResponse requestInvestigation(
            @Parameter(description = "Unique incident identifier", required = true, example = "incident-123")
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objective that should guide the investigation",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InvestigationRequest.class))
            )
            @Valid @RequestBody InvestigationRequest request
    ) {
        return IncidentResponse.from(service.requestInvestigation(id, request.objective()));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Transition incident status",
            description = "Requests a lifecycle status transition. The domain model validates whether the transition is allowed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident status updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or disallowed status transition", content = @Content),
            @ApiResponse(responseCode = "404", description = "Incident not found", content = @Content)
    })
    public IncidentResponse transition(
            @Parameter(description = "Unique incident identifier", required = true, example = "incident-123")
            @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Target status and optional transition summary",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StatusTransitionRequest.class))
            )
            @Valid @RequestBody StatusTransitionRequest request
    ) {
        return IncidentResponse.from(service.transition(id, request.status(), request.summary()));
    }

    @Schema(description = "Request to start an investigation for an incident.")
    public record InvestigationRequest(
            @Schema(description = "Investigation objective", example = "Determine the cause of elevated HTTP 5xx errors.")
            @NotBlank String objective
    ) {}

    @Schema(description = "Requested incident lifecycle status transition.")
    public record StatusTransitionRequest(
            @Schema(description = "Target lifecycle status", example = "INVESTIGATING", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull IncidentStatus status,

            @Schema(description = "Optional explanation for the transition", example = "Investigation started by the on-call operator.")
            String summary
    ) {}

    @Schema(description = "Evidence reference associated with an incident.")
    public record EvidenceResponse(
            @Schema(description = "Database identifier", example = "42")
            Long id,

            @Schema(description = "Related incident identifier", example = "incident-123")
            String incidentId,

            @Schema(description = "Identifier of the source event", example = "event-456")
            String sourceEventId,

            @Schema(description = "Timestamp when the evidence was recorded", type = "string", format = "date-time")
            Instant recordedAt
    ) {
        static EvidenceResponse from(IncidentEvidence e) {
            return new EvidenceResponse(e.getId(), e.getIncidentId(), e.getSourceEventId(), e.getRecordedAt());
        }
    }

    @Schema(description = "Incident representation returned by the Incident Service.")
    public record IncidentResponse(
            @Schema(description = "Unique incident identifier", example = "incident-123")
            String incidentId,

            @Schema(description = "Short incident title", example = "Elevated HTTP 5xx errors")
            String title,

            @Schema(description = "Detailed incident description")
            String description,

            @Schema(description = "Incident severity", example = "HIGH")
            IncidentSeverity severity,

            @Schema(description = "Current lifecycle status", example = "NEW")
            IncidentStatus status,

            @Schema(description = "Environment in which the incident was detected", example = "production")
            String environment,

            @Schema(description = "Primary affected resource", example = "payments-api")
            String primaryResourceId,

            @Schema(description = "Identifier of the detector", example = "prometheus-alertmanager")
            String detectorId,

            @Schema(description = "Correlation identifier")
            String correlationId,

            @Schema(description = "Causation identifier")
            String causationId,

            @Schema(description = "Time at which the incident was detected", type = "string", format = "date-time")
            Instant detectedAt,

            @Schema(description = "Time at which the incident was created", type = "string", format = "date-time")
            Instant createdAt,

            @Schema(description = "Time at which the incident was last updated", type = "string", format = "date-time")
            Instant updatedAt,

            @Schema(description = "Optimistic-locking version", example = "1")
            long version
    ) {
        static IncidentResponse from(Incident i) {
            return new IncidentResponse(i.getIncidentId(), i.getTitle(), i.getDescription(), i.getSeverity(),
                    i.getStatus(), i.getEnvironment(), i.getPrimaryResourceId(), i.getDetectorId(),
                    i.getCorrelationId(), i.getCausationId(), i.getDetectedAt(), i.getCreatedAt(),
                    i.getUpdatedAt(), i.getVersion());
        }
    }
}
