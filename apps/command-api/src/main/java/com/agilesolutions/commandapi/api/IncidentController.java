
package com.agilesolutions.commandapi.api;

import com.agilesolutions.commandapi.dto.IncidentResponse;
import com.agilesolutions.commandapi.dto.InvestigationRequest;
import com.agilesolutions.commandapi.dto.RemediationDecisionRequest;
import com.agilesolutions.commandapi.service.CommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@Validated
@Tag(
        name = "Incidents",
        description = "API voor het opvragen van incidenten, het starten van onderzoeken " +
                "en het goedkeuren of afwijzen van herstelvoorstellen."
)
public class IncidentController {

    private final CommandService commandService;

    public IncidentController(CommandService commandService) {
        this.commandService = commandService;
    }

    @GetMapping
    @Operation(
            summary = "Lijst incidenten",
            description = "Haalt een gepagineerde lijst van incidenten op."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incidenten succesvol opgehaald"),
            @ApiResponse(responseCode = "400", description = "Ongeldige paginaparameters")
    })
    public List<IncidentResponse> listIncidents(
            @Parameter(description = "Paginanummer, vanaf 0", example = "0")
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @Parameter(description = "Aantal incidenten per pagina, tussen 1 en 100", example = "20")
            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size) {

        return commandService.listIncidents(page, size);
    }

    @GetMapping("/{incidentId}")
    @Operation(
            summary = "Haal incident op",
            description = "Haalt de details van een incident op aan de hand van de incident-ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Incident succesvol opgehaald"),
            @ApiResponse(responseCode = "404", description = "Incident niet gevonden")
    })
    public IncidentResponse getIncident(
            @Parameter(description = "Unieke identificatie van het incident", example = "inc-123")
            @PathVariable String incidentId) {

        return commandService.getIncident(incidentId);
    }

    @PostMapping("/{incidentId}/investigations")
    @Operation(
            summary = "Start incidentonderzoek",
            description = "Dient een verzoek in om een incident nader te laten onderzoeken."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onderzoeksverzoek verwerkt"),
            @ApiResponse(responseCode = "202", description = "Onderzoeksverzoek geaccepteerd"),
            @ApiResponse(responseCode = "400", description = "Ongeldig onderzoeksverzoek"),
            @ApiResponse(responseCode = "404", description = "Incident niet gevonden")
    })
    public CommandAcceptedResponse requestInvestigation(
            @Parameter(description = "Unieke identificatie van het incident", example = "inc-123")
            @PathVariable String incidentId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Gegevens voor het aangevraagde onderzoek",
                    required = true
            )
            @Valid @RequestBody InvestigationRequest request) {

        return commandService.requestInvestigation(incidentId, request);
    }

    @PostMapping("/{incidentId}/remediation-proposals/{proposalId}/approve")
    @Operation(
            summary = "Keur herstelvoorstel goed",
            description = "Dient een beslissing in om een specifiek herstelvoorstel goed te keuren. " +
                    "De CommandService bepaalt hoe deze beslissing wordt verwerkt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goedkeuringsverzoek verwerkt"),
            @ApiResponse(responseCode = "202", description = "Goedkeuringsverzoek geaccepteerd"),
            @ApiResponse(responseCode = "400", description = "Ongeldig beslissingsverzoek"),
            @ApiResponse(responseCode = "404", description = "Incident of herstelvoorstel niet gevonden"),
            @ApiResponse(responseCode = "409", description = "Beslissing niet toegestaan voor de huidige status")
    })
    public CommandAcceptedResponse approveRemediation(
            @Parameter(description = "Unieke identificatie van het incident", example = "inc-123")
            @PathVariable String incidentId,

            @Parameter(description = "Unieke identificatie van het herstelvoorstel", example = "proposal-456")
            @PathVariable String proposalId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Beslissingsgegevens voor de goedkeuring",
                    required = true
            )
            @Valid @RequestBody RemediationDecisionRequest request) {

        return commandService.approveRemediation(incidentId, proposalId, request);
    }

    @PostMapping("/{incidentId}/remediation-proposals/{proposalId}/reject")
    @Operation(
            summary = "Wijs herstelvoorstel af",
            description = "Dient een beslissing in om een specifiek herstelvoorstel af te wijzen."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Afwijzingsverzoek verwerkt"),
            @ApiResponse(responseCode = "202", description = "Afwijzingsverzoek geaccepteerd"),
            @ApiResponse(responseCode = "400", description = "Ongeldig beslissingsverzoek"),
            @ApiResponse(responseCode = "404", description = "Incident of herstelvoorstel niet gevonden"),
            @ApiResponse(responseCode = "409", description = "Beslissing niet toegestaan voor de huidige status")
    })
    public CommandAcceptedResponse rejectRemediation(
            @Parameter(description = "Unieke identificatie van het incident", example = "inc-123")
            @PathVariable String incidentId,

            @Parameter(description = "Unieke identificatie van het herstelvoorstel", example = "proposal-456")
            @PathVariable String proposalId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reden en overige gegevens voor de afwijzing",
                    required = true
            )
            @Valid @RequestBody RemediationDecisionRequest request) {

        return commandService.rejectRemediation(incidentId, proposalId, request);
    }
}
