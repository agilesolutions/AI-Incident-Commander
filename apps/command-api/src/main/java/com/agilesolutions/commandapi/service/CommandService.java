package com.agilesolutions.commandapi.service;

import com.agilesolutions.commandapi.api.CommandAcceptedResponse;
import com.agilesolutions.commandapi.dto.IncidentResponse;
import com.agilesolutions.commandapi.dto.InvestigationRequest;
import com.agilesolutions.commandapi.dto.RemediationDecisionRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CommandService {

    private final RestClient incidentService;
    private final RestClient remediationService;

    public CommandService(
            @Qualifier("incidentServiceClient") RestClient incidentService,
            @Qualifier("remediationServiceClient") RestClient remediationService) {
        this.incidentService = incidentService;
        this.remediationService = remediationService;
    }

    public List<IncidentResponse> listIncidents(int page, int size) {
        List<IncidentResponse> response = incidentService.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/incidents")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return response == null ? List.of() : response;
    }

    public IncidentResponse getIncident(String incidentId) {
        IncidentResponse response = incidentService.get()
                .uri("/api/v1/incidents/{incidentId}", incidentId)
                .retrieve()
                .body(IncidentResponse.class);
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Incident service returned an empty response");
        }
        return response;
    }

    public CommandAcceptedResponse requestInvestigation(String incidentId, InvestigationRequest request) {
        CommandAcceptedResponse response = incidentService.post()
                .uri("/api/v1/incidents/{incidentId}/investigations", incidentId)
                .body(request)
                .retrieve()
                .body(CommandAcceptedResponse.class);
        return acceptedOrGatewayError(response, "Investigation request accepted");
    }

    public CommandAcceptedResponse approveRemediation(
            String incidentId, String proposalId, RemediationDecisionRequest request) {
        return remediationDecision(incidentId, proposalId, "approve", request);
    }

    public CommandAcceptedResponse rejectRemediation(
            String incidentId, String proposalId, RemediationDecisionRequest request) {
        return remediationDecision(incidentId, proposalId, "reject", request);
    }

    private CommandAcceptedResponse remediationDecision(
            String incidentId, String proposalId, String decision, RemediationDecisionRequest request) {
        CommandAcceptedResponse response = remediationService.post()
                .uri("/api/v1/incidents/{incidentId}/proposals/{proposalId}/{decision}",
                        incidentId, proposalId, decision)
                .body(request)
                .retrieve()
                .body(CommandAcceptedResponse.class);
        return acceptedOrGatewayError(response, "Remediation " + decision + " command accepted");
    }

    private CommandAcceptedResponse acceptedOrGatewayError(CommandAcceptedResponse response, String message) {
        if (response != null) {
            return response;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Downstream service returned an empty command response: " + message);
    }
}
