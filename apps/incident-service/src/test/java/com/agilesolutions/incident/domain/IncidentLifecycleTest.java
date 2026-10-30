package com.agilesolutions.incident.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IncidentLifecycleTest {
    private Incident incident() {
        return new Incident("i-1", "HTTP 5xx", "Foutpercentage verhoogd", IncidentSeverity.HIGH,
            "test", "orders-api", "signal-correlator", "corr-1", null, java.time.Instant.now());
    }

    @Test void permitsExpectedInvestigationLifecycle() {
        Incident incident = incident();
        incident.transitionTo(IncidentStatus.INVESTIGATION_REQUESTED);
        incident.transitionTo(IncidentStatus.INVESTIGATING);
        incident.transitionTo(IncidentStatus.INVESTIGATION_COMPLETED);
        incident.transitionTo(IncidentStatus.RESOLVED);
        incident.transitionTo(IncidentStatus.CLOSED);
        assertEquals(IncidentStatus.CLOSED, incident.getStatus());
    }

    @Test void rejectsSkippingInvestigation() {
        Incident incident = incident();
        assertThrows(InvalidIncidentTransitionException.class,
            () -> incident.transitionTo(IncidentStatus.CLOSED));
    }

    @Test void closedIncidentCannotBeReopened() {
        Incident incident = incident();
        incident.transitionTo(IncidentStatus.RESOLVED);
        incident.transitionTo(IncidentStatus.CLOSED);
        assertThrows(InvalidIncidentTransitionException.class,
            () -> incident.transitionTo(IncidentStatus.INVESTIGATION_REQUESTED));
    }
}
