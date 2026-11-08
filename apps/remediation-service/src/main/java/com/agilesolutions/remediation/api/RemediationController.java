package com.agilesolutions.remediation.api;

import com.agilesolutions.remediation.domain.RemediationExecution;
import com.agilesolutions.remediation.persistence.RemediationExecutionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/remediations")
@Tag(
        name = "Remediations",
        description = "Endpoints for querying remediation execution records."
)
public class RemediationController {

    private final RemediationExecutionRepository repo;

    @GetMapping
    @Operation(
            summary = "List recent remediation executions",
            description = "Returns up to 100 remediation executions, ordered by received timestamp descending."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Remediation executions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RemediationView.class)
                    )
            )
    })
    public List<RemediationView> list() {
        return repo.findTop100ByOrderByReceivedAtDesc()
                .stream()
                .map(RemediationView::from)
                .toList();
    }
    @GetMapping("/{proposalId}")
    @Operation(
            summary = "Get remediation execution",
            description = "Returns the remediation execution associated with the supplied proposal ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Remediation execution found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RemediationView.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No remediation execution exists for the supplied proposal ID",
                    content = @Content
            )
    })
    public ResponseEntity<RemediationView> get(
            @Parameter(
                    description = "Unique remediation proposal identifier",
                    required = true,
                    example = "proposal-123"
            )
            @PathVariable String proposalId
    ) {
        return repo.findById(proposalId)
                .map(RemediationView::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Schema(description = "Read model representing a remediation execution.")
    public record RemediationView(
            @Schema(description = "Unique remediation proposal identifier", example = "proposal-123")
            String proposalId,

            @Schema(description = "Identifier of the related incident", example = "incident-456")
            String incidentId,

            @Schema(description = "Type of remediation action", example = "RESTART_POD")
            String actionType,

            @Schema(description = "Current execution status", example = "SUCCEEDED")
            String status,

            @Schema(description = "Target resource identifier", example = "payments-api-7d9f")
            String resourceId,

            @Schema(description = "Identifier of the approval authorizing the action")
            String approvalId,

            @Schema(description = "Identity of the person who approved the action")
            String approvedBy,

            @Schema(description = "Reference returned by the remediation executor")
            String executionReference,

            @Schema(description = "Human-readable execution result")
            String resultMessage,

            @Schema(description = "Error details when execution failed")
            String errorMessage,

            @Schema(description = "Timestamp at which execution took place", type = "string", format = "date-time")
            Instant executedAt,

            @Schema(description = "Timestamp at which the remediation was verified", type = "string", format = "date-time")
            Instant verifiedAt
    ) {
        static RemediationView from(RemediationExecution execution) {
            return new RemediationView(
                    execution.getProposalId(),
                    execution.getIncidentId(),
                    execution.getActionType().name(),
                    execution.getStatus().name(),
                    execution.getResourceId(),
                    execution.getApprovalId(),
                    execution.getApprovedBy(),
                    execution.getExecutionReference(),
                    execution.getResultMessage(),
                    execution.getErrorMessage(),
                    execution.getExecutedAt(),
                    execution.getVerifiedAt()
            );
        }
    }
}




