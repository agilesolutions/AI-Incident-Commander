package com.agilesolutions.investigation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "investigation")
public class AgentProperties {
    private String requestedTopic = "incident.investigation.requested.v1";
    private String completedTopic = "incident.investigation.completed.v1";
    private String consumerGroup = "investigation-agent";
    private String schemaRegistryUrl = "http://localhost:8081";
    private String model = "qwen2.5:7b";
    private int maxEvidenceItems = 100;
    private int maxPromptChars = 24000;
    private int maxAttempts = 3;
    private long retryDelayMs = 3000;
    private boolean publishEnabled = true;

    public String getRequestedTopic() { return requestedTopic; }
    public void setRequestedTopic(String v) { requestedTopic = v; }
    public String getCompletedTopic() { return completedTopic; }
    public void setCompletedTopic(String v) { completedTopic = v; }
    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String v) { consumerGroup = v; }
    public String getSchemaRegistryUrl() { return schemaRegistryUrl; }
    public void setSchemaRegistryUrl(String v) { schemaRegistryUrl = v; }
    public String getModel() { return model; }
    public void setModel(String v) { model = v; }
    public int getMaxEvidenceItems() { return maxEvidenceItems; }
    public void setMaxEvidenceItems(int v) { maxEvidenceItems = v; }
    public int getMaxPromptChars() { return maxPromptChars; }
    public void setMaxPromptChars(int v) { maxPromptChars = v; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int v) { maxAttempts = v; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long v) { retryDelayMs = v; }
    public boolean isPublishEnabled() { return publishEnabled; }
    public void setPublishEnabled(boolean v) { publishEnabled = v; }
}
