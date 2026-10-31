package com.agilesolutions.remediation.api;

import com.agilesolutions.remediation.domain.RemediationExecution;
import com.agilesolutions.remediation.persistence.RemediationExecutionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/remediations")
public class RemediationController {
    private final RemediationExecutionRepository repo;

    public RemediationController(RemediationExecutionRepository r) {
        repo = r;
    }

    @GetMapping
    public List<RemediationView> list() {
        return repo.findTop100ByOrderByReceivedAtDesc().stream().map(RemediationView::from).toList();
    }

    @GetMapping("/{proposalId}")
    public ResponseEntity<RemediationView> get(@PathVariable String proposalId) {
        return repo.findById(proposalId).map(RemediationView::from).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record RemediationView(String proposalId, String incidentId, String actionType, String status,
                                  String resourceId, String approvalId, String approvedBy, String executionReference,
                                  String resultMessage, String errorMessage, Instant executedAt, Instant verifiedAt) {
        static RemediationView from(RemediationExecution e) {
            return new RemediationView(e.getProposalId(), e.getIncidentId(), e.getActionType().name(), e.getStatus().name(), e.getResourceId(), e.getApprovalId(), e.getApprovedBy(), e.getExecutionReference(), e.getResultMessage(), e.getErrorMessage(), e.getExecutedAt(), e.getVerifiedAt());
        }
    }
}
