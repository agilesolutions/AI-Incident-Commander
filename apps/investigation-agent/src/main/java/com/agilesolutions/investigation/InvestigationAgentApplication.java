package com.agilesolutions.investigation;

import com.agilesolutions.investigation.config.AgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AgentProperties.class)
public class InvestigationAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvestigationAgentApplication.class, args);
    }
}
