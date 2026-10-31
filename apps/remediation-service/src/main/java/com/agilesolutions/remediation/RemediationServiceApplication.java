package com.agilesolutions.remediation;

import com.agilesolutions.remediation.config.RemediationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RemediationProperties.class)
public class RemediationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RemediationServiceApplication.class, args);
    }
}
