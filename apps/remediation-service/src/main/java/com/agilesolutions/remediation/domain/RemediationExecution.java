package com.agilesolutions.remediation.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "remediation_executions", indexes = @Index(name = "idx_remediation_incident", columnList = "incident_id"))
public class RemediationExecution {
    @Id
    @Column(name = "proposal_id", length = 100)
    private String proposalId;
    @Column(nullable = false, length = 36)
    private String incidentId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RemediationActionType actionType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RemediationStatus status;
    @Column(nullable = false, length = 120)
    private String resourceId;
    @Column(nullable = false, length = 64)
    private String actionDigest;
    @Column(nullable = false, length = 120)
    private String approvalId;
    @Column(nullable = false, length = 120)
    private String approvedBy;
    @Column(nullable = false)
    private Instant approvedAt, receivedAt;
    private Instant executedAt, verifiedAt;
    @Column(length = 2000)
    private String executionReference, resultMessage, errorMessage;
    @Version
    private long version;

    protected RemediationExecution() {
    }

    public RemediationExecution(String p, String i, RemediationActionType t, String r, String d, String a, String by, Instant at) {
        proposalId = p;
        incidentId = i;
        actionType = t;
        resourceId = r;
        actionDigest = d;
        approvalId = a;
        approvedBy = by;
        approvedAt = at;
        receivedAt = Instant.now();
        status = RemediationStatus.APPROVED;
    }

    public void validating() {
        status = RemediationStatus.VALIDATING;
    }

    public void executing() {
        status = RemediationStatus.EXECUTING;
    }

    public void executed(String ref, String msg) {
        status = RemediationStatus.EXECUTED;
        executedAt = Instant.now();
        executionReference = ref;
        resultMessage = msg;
    }

    public void verified(boolean ok, String msg) {
        status = ok ? RemediationStatus.VERIFIED : RemediationStatus.UNVERIFIED;
        verifiedAt = Instant.now();
        resultMessage = msg;
    }

    public void failed(String msg) {
        status = RemediationStatus.FAILED;
        errorMessage = msg;
    }

    public String getProposalId() {
        return proposalId;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public RemediationActionType getActionType() {
        return actionType;
    }

    public RemediationStatus getStatus() {
        return status;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public String getExecutionReference() {
        return executionReference;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
