package com.agilesolutions.incident.api;

import com.agilesolutions.incident.application.IncidentApplicationService;
import com.agilesolutions.incident.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {
    private final IncidentApplicationService service;
    public IncidentController(IncidentApplicationService service) { this.service = service; }

    @GetMapping
    public List<IncidentResponse> list(@RequestParam(required = false) IncidentStatus status) {
        return service.list(status).stream().map(IncidentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public IncidentResponse get(@PathVariable String id) { return IncidentResponse.from(service.getOrThrow(id)); }

    @GetMapping("/{id}/evidence")
    public List<EvidenceResponse> evidence(@PathVariable String id) {
        return service.evidence(id).stream().map(EvidenceResponse::from).toList();
    }

    @PostMapping("/{id}/investigations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IncidentResponse requestInvestigation(@PathVariable String id, @Valid @RequestBody InvestigationRequest request) {
        return IncidentResponse.from(service.requestInvestigation(id, request.objective()));
    }

    @PatchMapping("/{id}/status")
    public IncidentResponse transition(@PathVariable String id, @Valid @RequestBody StatusTransitionRequest request) {
        return IncidentResponse.from(service.transition(id, request.status(), request.summary()));
    }

    public record InvestigationRequest(@NotBlank String objective) {}
    public record StatusTransitionRequest(@NotNull IncidentStatus status, String summary) {}
    public record EvidenceResponse(Long id, String incidentId, String sourceEventId, Instant recordedAt) {
        static EvidenceResponse from(IncidentEvidence e) {
            return new EvidenceResponse(e.getId(), e.getIncidentId(), e.getSourceEventId(), e.getRecordedAt());
        }
    }
    public record IncidentResponse(String incidentId, String title, String description, IncidentSeverity severity,
        IncidentStatus status, String environment, String primaryResourceId, String detectorId,
        String correlationId, String causationId, Instant detectedAt, Instant createdAt, Instant updatedAt, long version) {
        static IncidentResponse from(Incident i) {
            return new IncidentResponse(i.getIncidentId(), i.getTitle(), i.getDescription(), i.getSeverity(),
                i.getStatus(), i.getEnvironment(), i.getPrimaryResourceId(), i.getDetectorId(),
                i.getCorrelationId(), i.getCausationId(), i.getDetectedAt(), i.getCreatedAt(),
                i.getUpdatedAt(), i.getVersion());
        }
    }
}
