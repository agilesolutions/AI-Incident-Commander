package com.agilesolutions.commandapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DownstreamClientsConfiguration {

    @Bean
    RestClient incidentServiceClient(
            RestClient.Builder builder,
            @Value("${commander.services.incident.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    RestClient remediationServiceClient(
            RestClient.Builder builder,
            @Value("${commander.services.remediation.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
