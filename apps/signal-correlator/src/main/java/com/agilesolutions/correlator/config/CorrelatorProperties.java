package com.agilesolutions.correlator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "correlator")
public class CorrelatorProperties {
    private String inputTopic = "incident.telemetry.v1";
    private String outputTopic = "incident.detected.v1";
    private String detectorId = "signal-correlator-v1";
    private int threshold = 3;
    private long windowSeconds = 120;
    private long deduplicationTtlSeconds = 900;
    private String environment = "";

    public String getInputTopic() { return inputTopic; }
    public void setInputTopic(String inputTopic) { this.inputTopic = inputTopic; }
    public String getOutputTopic() { return outputTopic; }
    public void setOutputTopic(String outputTopic) { this.outputTopic = outputTopic; }
    public String getDetectorId() { return detectorId; }
    public void setDetectorId(String detectorId) { this.detectorId = detectorId; }
    public int getThreshold() { return threshold; }
    public void setThreshold(int threshold) { this.threshold = threshold; }
    public long getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(long windowSeconds) { this.windowSeconds = windowSeconds; }
    public long getDeduplicationTtlSeconds() { return deduplicationTtlSeconds; }
    public void setDeduplicationTtlSeconds(long value) { this.deduplicationTtlSeconds = value; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
}
