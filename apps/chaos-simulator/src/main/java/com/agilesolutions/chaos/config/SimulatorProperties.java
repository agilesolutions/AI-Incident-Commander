package com.agilesolutions.chaos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {
    private boolean enabled = false;
    private long intervalMs = 30000;
    private String source = "chaos-simulator";
    private String environment = "demo";
    private String topic = "incident.telemetry.v1";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getIntervalMs() { return intervalMs; }
    public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}
