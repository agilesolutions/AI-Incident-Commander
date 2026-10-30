CREATE TABLE investigations (
    event_id VARCHAR(100) PRIMARY KEY,
    incident_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    correlation_id VARCHAR(120) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    report_json TEXT,
    error_message VARCHAR(2000),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_investigations_incident ON investigations(incident_id);

CREATE TABLE processed_investigation_messages (
    event_id VARCHAR(100) PRIMARY KEY,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE investigation_outbox (
    id VARCHAR(36) PRIMARY KEY,
    topic VARCHAR(160) NOT NULL,
    message_key VARCHAR(160) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    last_error VARCHAR(2000),
    attempts INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_investigation_outbox_pending ON investigation_outbox(published_at, created_at);
