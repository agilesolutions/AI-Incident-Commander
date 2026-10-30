package com.agilesolutions.incident.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "incident")
public class IncidentProperties {
    private String detectedTopic = "incident.detected.v1";
    private String updatedTopic = "incident.updated.v1";
    private String investigationRequestedTopic = "incident.investigation.requested.v1";
    private String consumerGroup = "incident-service";
    private String schemaRegistryUrl = "http://schema-registry:8081";
    public String getDetectedTopic() { return detectedTopic; }
    public void setDetectedTopic(String v) { detectedTopic = v; }
    public String getUpdatedTopic() { return updatedTopic; }
    public void setUpdatedTopic(String v) { updatedTopic = v; }
    public String getInvestigationRequestedTopic() { return investigationRequestedTopic; }
    public void setInvestigationRequestedTopic(String v) { investigationRequestedTopic = v; }
    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String v) { consumerGroup = v; }
    public String getSchemaRegistryUrl() { return schemaRegistryUrl; }
    public void setSchemaRegistryUrl(String v) { schemaRegistryUrl = v; }
}
