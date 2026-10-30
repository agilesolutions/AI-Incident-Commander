package com.agilesolutions.investigation.agent;

import java.util.List;

public record InvestigationReport(
    String summary,
    String probableRootCause,
    double confidence,
    List<String> supportingEvidence,
    List<String> uncertainties,
    List<String> recommendedActions,
    String severityAssessment,
    boolean humanReviewRequired
) {
    public InvestigationReport {
        supportingEvidence = supportingEvidence == null ? List.of() : List.copyOf(supportingEvidence);
        uncertainties = uncertainties == null ? List.of() : List.copyOf(uncertainties);
        recommendedActions = recommendedActions == null ? List.of() : List.copyOf(recommendedActions);
        confidence = Math.max(0.0, Math.min(1.0, confidence));
        humanReviewRequired = true;
    }
}
