CREATE TABLE incidents (
    incident_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(240) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(16) NOT NULL,
    status VARCHAR(32) NOT NULL,
    environment VARCHAR(120) NOT NULL,
    primary_resource_id VARCHAR(240) NOT NULL,
    detector_id VARCHAR(120) NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    causation_id VARCHAR(100),
    detected_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_resource ON incidents(primary_resource_id);
CREATE INDEX idx_incidents_detected_at ON incidents(detected_at DESC);

CREATE TABLE incident_evidence (
    id BIGSERIAL PRIMARY KEY,
    incident_id VARCHAR(36) NOT NULL REFERENCES incidents(incident_id) ON DELETE CASCADE,
    source_event_id VARCHAR(100) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_evidence_incident_event UNIQUE (incident_id, source_event_id)
);

CREATE TABLE processed_events (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    topic VARCHAR(160) NOT NULL,
    message_key VARCHAR(160) NOT NULL,
    payload TEXT NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    last_error VARCHAR(1000),
    attempts INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_outbox_pending ON outbox_events(published_at, created_at);
