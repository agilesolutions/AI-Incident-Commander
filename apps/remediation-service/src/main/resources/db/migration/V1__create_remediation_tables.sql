CREATE TABLE remediation_executions (
 proposal_id VARCHAR(100) PRIMARY KEY, incident_id VARCHAR(36) NOT NULL, action_type VARCHAR(40) NOT NULL,
 status VARCHAR(20) NOT NULL, resource_id VARCHAR(120) NOT NULL, action_digest VARCHAR(64) NOT NULL,
 approval_id VARCHAR(120) NOT NULL, approved_by VARCHAR(120) NOT NULL, approved_at TIMESTAMPTZ NOT NULL,
 received_at TIMESTAMPTZ NOT NULL, executed_at TIMESTAMPTZ, verified_at TIMESTAMPTZ,
 execution_reference VARCHAR(2000), result_message VARCHAR(2000), error_message VARCHAR(2000), version BIGINT NOT NULL DEFAULT 0);
CREATE INDEX idx_remediation_incident ON remediation_executions(incident_id);
CREATE TABLE processed_remediation_messages(event_id VARCHAR(100) PRIMARY KEY, processed_at TIMESTAMPTZ NOT NULL);
CREATE TABLE remediation_outbox(id VARCHAR(36) PRIMARY KEY, topic VARCHAR(160) NOT NULL, message_key VARCHAR(160) NOT NULL,
 payload TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL, published_at TIMESTAMPTZ, last_error VARCHAR(2000), attempts INTEGER NOT NULL DEFAULT 0);
CREATE INDEX idx_remediation_outbox_pending ON remediation_outbox(published_at,created_at);
