package com.agilesolutions.investigation.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "investigation.engine", havingValue = "ollama", matchIfMissing = true)
public class OllamaInvestigationEngine implements InvestigationEngine {
    private final ChatClient chatClient;
    private final InvestigationPromptBuilder prompts;
    private final ObjectMapper mapper;

    public OllamaInvestigationEngine(ChatClient.Builder builder, InvestigationPromptBuilder prompts,
                                     ObjectMapper mapper) {
        this.chatClient = builder.build(); this.prompts = prompts; this.mapper = mapper;
    }

    @Override
    public InvestigationReport investigate(InvestigationRequest request) {
        String response = chatClient.prompt().user(prompts.build(request)).call().content();
        if (response == null || response.isBlank()) throw new IllegalStateException("LLM gaf een lege respons");
        String json = extractJson(response);
        try {
            InvestigationReport report = mapper.readValue(json, InvestigationReport.class);
            if (report.summary() == null || report.probableRootCause() == null)
                throw new IllegalArgumentException("LLM-rapport mist verplichte velden");
            return report;
        } catch (Exception e) {
            throw new IllegalStateException("LLM-respons is geen geldig InvestigationReport JSON", e);
        }
    }

    private String extractJson(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) throw new IllegalArgumentException("Geen JSON-object gevonden");
        return trimmed.substring(start, end + 1);
    }
}
