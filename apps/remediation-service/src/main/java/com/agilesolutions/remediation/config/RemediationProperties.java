package com.agilesolutions.remediation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "remediation")
public class RemediationProperties {
    private String approvedTopic = "incident.remediation.approved.v1", rejectedTopic = "incident.remediation.rejected.v1", executedTopic = "incident.remediation.executed.v1", failedTopic = "incident.remediation.failed.v1", verifiedTopic = "incident.remediation.verified.v1", consumerGroup = "remediation-service", schemaRegistryUrl = "http://localhost:8081", executionMode = "simulation", verificationMode = "manual";
    private long outboxPollDelayMs = 1000;
    private Set<String> allowedNamespaces = Set.of("default", "demo");

    public String getApprovedTopic() {
        return approvedTopic;
    }

    public void setApprovedTopic(String v) {
        approvedTopic = v;
    }

    public String getRejectedTopic() {
        return rejectedTopic;
    }

    public void setRejectedTopic(String v) {
        rejectedTopic = v;
    }

    public String getExecutedTopic() {
        return executedTopic;
    }

    public void setExecutedTopic(String v) {
        executedTopic = v;
    }

    public String getFailedTopic() {
        return failedTopic;
    }

    public void setFailedTopic(String v) {
        failedTopic = v;
    }

    public String getVerifiedTopic() {
        return verifiedTopic;
    }

    public void setVerifiedTopic(String v) {
        verifiedTopic = v;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String v) {
        consumerGroup = v;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    public void setSchemaRegistryUrl(String v) {
        schemaRegistryUrl = v;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(String v) {
        executionMode = v;
    }

    public String getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(String v) {
        verificationMode = v;
    }

    public long getOutboxPollDelayMs() {
        return outboxPollDelayMs;
    }

    public void setOutboxPollDelayMs(long v) {
        outboxPollDelayMs = v;
    }

    public Set<String> getAllowedNamespaces() {
        return allowedNamespaces;
    }

    public void setAllowedNamespaces(Set<String> v) {
        allowedNamespaces = v;
    }
}
