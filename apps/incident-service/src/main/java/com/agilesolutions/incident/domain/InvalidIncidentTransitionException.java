package com.agilesolutions.incident.domain;

public class InvalidIncidentTransitionException extends RuntimeException {
    public InvalidIncidentTransitionException(IncidentStatus current, IncidentStatus requested) {
        super("Ongeldige incidentstatusovergang: " + current + " -> " + requested);
    }
}
