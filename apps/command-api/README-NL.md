# Command API

REST façade for the AI Incident Commander platform. It exposes incident queries and operator commands while leaving incident state and remediation execution to their owning services.

## Responsibilities

- Query incidents through `incident-service`.
- Request an investigation through `incident-service`.
- Approve or reject a specific remediation proposal through `remediation-service`.
- Validate incoming requests and return consistent HTTP problem details.
- Propagate a caller-supplied `X-Correlation-ID` or generate one when absent.
- Expose Spring Boot Actuator health/readiness endpoints.

The API does **not** own incident persistence, does not execute remediation, and does not treat an AI proposal as authorization.

## HTTP API

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/v1/incidents` | List incidents |
| `GET` | `/api/v1/incidents/{incidentId}` | Get one incident |
| `POST` | `/api/v1/incidents/{incidentId}/investigations` | Request an investigation |
| `POST` | `/api/v1/incidents/{incidentId}/remediation-proposals/{proposalId}/approve` | Approve a proposal |
| `POST` | `/api/v1/incidents/{incidentId}/remediation-proposals/{proposalId}/reject` | Reject a proposal |
| `GET` | `/actuator/health` | Health check |
| `GET` | `/actuator/health/readiness` | Readiness check |

## Downstream service contract

This module assumes these internal endpoints. Align the paths with the actual `incident-service` and `remediation-service` controllers when those modules are implemented.

- `incident-service`
  - `GET /api/v1/incidents`
  - `GET /api/v1/incidents/{incidentId}`
  - `POST /api/v1/incidents/{incidentId}/investigations`
- `remediation-service`
  - `POST /api/v1/incidents/{incidentId}/proposals/{proposalId}/approve`
  - `POST /api/v1/incidents/{incidentId}/proposals/{proposalId}/reject`

Downstream failures are surfaced as gateway errors; this API does not silently claim that a command succeeded.

## Configuration

Environment variables:

- `INCIDENT_SERVICE_BASE_URL` (default `http://localhost:8081`)
- `REMEDIATION_SERVICE_BASE_URL` (default `http://localhost:8082`)
- `SERVER_PORT` (default `8080`)

Run from the repository root after adding `include 'apps:command-api'` to `settings.gradle`:

```bash
./gradlew :apps:command-api:test
./gradlew :apps:command-api:bootRun
```

For local standalone development, use the included `build.gradle` with a Gradle settings file that includes this module, or run from the full multi-project build.

## Security note

Authentication/authorization is expected to be supplied by the platform ingress/OIDC layer in the initial scaffold. Before production use, add Spring Security OAuth2 resource-server support, enforce role-based authorization for approval/rejection endpoints, and bind approval identity to a verified JWT subject. Never trust `approvedBy` supplied by an unauthenticated caller.
