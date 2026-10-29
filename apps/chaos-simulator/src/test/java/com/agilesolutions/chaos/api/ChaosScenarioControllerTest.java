package com.agilesolutions.chaos.api;

import com.agilesolutions.chaos.application.ChaosScenarioService;
import com.agilesolutions.chaos.domain.ScenarioRequest;
import com.agilesolutions.chaos.domain.ScenarioResult;
import com.agilesolutions.chaos.domain.ScenarioType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;


import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChaosScenarioController.class)
@Import(ApiExceptionHandler.class)
class ChaosScenarioControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ChaosScenarioService service;

    @Test
    void acceptsValidScenario() throws Exception {
        when(service.run(any(ScenarioRequest.class))).thenReturn(new ScenarioResult(
                UUID.randomUUID(), ScenarioType.HIGH_ERROR_RATE, "checkout-service",
                Instant.parse("2026-01-01T00:00:00Z"), "SUBMITTED", "queued"));

        mvc.perform(post("/api/v1/chaos/scenarios")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"scenario":"HIGH_ERROR_RATE","resourceId":"checkout-service","environment":"test"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.scenario").value("HIGH_ERROR_RATE"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void rejectsMissingScenario() throws Exception {
        mvc.perform(post("/api/v1/chaos/scenarios")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
