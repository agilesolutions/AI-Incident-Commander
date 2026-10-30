package com.agilesolutions.investigation.agent;

import com.agilesolutions.investigation.config.AgentProperties;
import org.springframework.stereotype.Component;

@Component
public class InvestigationPromptBuilder {
    private final AgentProperties properties;
    public InvestigationPromptBuilder(AgentProperties properties) { this.properties = properties; }

    public String build(InvestigationRequest request) {
        String evidence = request.evidenceEventIds() == null ? "[]" :
            request.evidenceEventIds().stream().limit(properties.getMaxEvidenceItems()).toList().toString();
        String prompt = """
            Je bent een voorzichtige SRE-incidentanalist. Analyseer uitsluitend de context die hieronder is gegeven.
            Geef geen verzonnen feiten, logs, metrics, deployments of oorzaken. Maak expliciet onderscheid tussen
            waarnemingen, hypotheses en onzekerheden. Bewijs-ID's zijn alleen verwijzingen; hun inhoud is hier
            niet beschikbaar. Claim dus niet dat je de onderliggende telemetrie hebt ingezien.

            Veiligheidsregels:
            - Je onderzoekt en adviseert; je voert geen acties uit.
            - Geef alleen aanbevelingen, geen uitvoerbare shell-, Kubernetes- of cloudcommando's.
            - Vermeld dat iedere wijziging menselijke goedkeuring vereist.
            - Als de context onvoldoende is, zeg dat expliciet en benoem welke gegevens nodig zijn.
            - Confidence is een getal tussen 0 en 1 en moet conservatief zijn.

            Retourneer uitsluitend valide JSON met exact deze velden:
            {
              "summary": "korte samenvatting",
              "probableRootCause": "waarschijnlijke oorzaak of onvoldoende bewijs",
              "confidence": 0.0,
              "supportingEvidence": ["alleen concrete beschikbare feiten of bewijs-ID's"],
              "uncertainties": ["onzekerheden en ontbrekende informatie"],
              "recommendedActions": ["veilige aanbevelingen voor menselijke beoordeling"],
              "severityAssessment": "LOW|MEDIUM|HIGH|CRITICAL of ONBEKEND",
              "humanReviewRequired": true
            }

            Incidentcontext:
            incidentId: %s
            priority: %s
            objective: %s
            occurredAt: %s
            correlationId: %s
            evidenceEventIds: %s
            """.formatted(safe(request.incidentId()), safe(request.priority()),
                safe(request.objective()), safe(request.occurredAt()), safe(request.correlationId()), evidence);
        return prompt.length() <= properties.getMaxPromptChars()
            ? prompt : prompt.substring(0, properties.getMaxPromptChars());
    }

    private String safe(String value) { return value == null ? "niet opgegeven" : value.replaceAll("[\\r\\n]", " "); }
}
