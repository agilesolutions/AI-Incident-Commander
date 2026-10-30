package com.agilesolutions.aiincidentcommander.incident.api;

import com.agilesolutions.incident.api.ApiExceptionHandler;
import com.agilesolutions.incident.api.IncidentController;
import com.agilesolutions.incident.application.IncidentApplicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(ApiExceptionHandler.class)
class IncidentControllerTest {
    @Autowired MockMvc mvc;
    @Mock
    IncidentApplicationService service;

    @Test void rejectsBlankInvestigationObjective() throws Exception {
        mvc.perform(post("/api/v1/incidents/inc-1/investigations")
            .contentType("application/json").content("{\"objective\":\"\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
