package com.agilesolutions.incident.application;

public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(String id) { super("Incident niet gevonden: " + id); }
}
