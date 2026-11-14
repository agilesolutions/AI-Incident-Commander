# Signal Correlator

## Purpose

`signal-correlator` consumes synthetic and operational telemetry events from Kafka, correlates error signals deterministically, and publishes an `IncidentDetected` event when a configurable threshold is reached.

The service does not perform AI analysis and does not take remediation actions. Detection is deterministic and explainable; investigation and remediation take place in subsequent components.

## Event flow

```text
chaos-simulator
      |
      | TelemetryEvent (Avro)
      v
incident.telemetry.v1
      |
      v
signal-correlator
      |
      | IncidentDetected (Avro)
      v
incident.detected.v1
      |
      v
incident-service
```

## Detection rules

- Only `ERROR` and `CRITICAL` telemetry is counted by the current detector.
- Signals are grouped by `environment + resourceId`.
- The detector uses a configurable time window (`window-seconds`).
- An incident event is published when at least `threshold` signals occur within the window.
- The incident is assigned `CRITICAL` if at least one evidence event is `CRITICAL`; otherwise it is assigned `HIGH`.
- `eventId` is used for in-memory deduplication.
- After detection, a deduplication window prevents repeated publishing for the same resource.
- `evidenceEventIds`, `correlationId`, and `causationId` are included in the incident event.

This is an initial deterministic baseline, not advanced anomaly detection. Thresholds must be calibrated for each workload type.

## Contracts

The service uses the Avro contracts from `libs/event-contracts`:

- `TelemetryEvent` on `incident.telemetry.v1`
- `IncidentDetected` on `incident.detected.v1`

The ZIP contains local copies of the two `.avsc` files as runtime resources so this module can be built independently. In the monorepo, the schemas in `libs/event-contracts/src/main/avro` remain the canonical source. Keep the copies synchronised or replace these runtime resources during integration with a shared schema artifact.

Schema Registry is used through the Confluent Avro Serializer/Deserializer. Auto-registration is enabled in the producer configuration in this baseline; controlled schema publication in CI is preferable for production.

## Configuration

| Variable | Default | Description |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Schema Registry URL |
| `KAFKA_CONSUMER_GROUP` | `signal-correlator` | Consumer group |
| `CORRELATOR_INPUT_TOPIC` | `incident.telemetry.v1` | Input topic |
| `CORRELATOR_OUTPUT_TOPIC` | `incident.detected.v1` | Output topic |
| `CORRELATOR_DETECTOR_ID` | `signal-correlator-v1` | Detector identifier |
| `CORRELATOR_THRESHOLD` | `3` | Number of error signals required for detection |
| `CORRELATOR_WINDOW_SECONDS` | `120` | Correlation window in seconds |
| `CORRELATOR_DEDUP_TTL_SECONDS` | `900` | Period during which repeated detection is suppressed |
| `CORRELATOR_ENVIRONMENT` | empty | Optional environment filter |
| `SERVER_PORT` | `8080` | Actuator HTTP port |

## Build and test

Requires JDK 25 and Gradle 9.x or a compatible Gradle installation.

```bash
gradle clean test
```

Start the service:

```bash
gradle bootRun
```

The application expects Kafka and Schema Registry to be reachable. Health endpoints are available at `/actuator/health`; Prometheus metrics are available at `/actuator/prometheus`.

## Metrics

- `correlator.telemetry.received`
- `correlator.telemetry.duplicates`
- `correlator.telemetry.processing.errors`
- `correlator.incidents.detected`
- `correlator.incidents.published`
- `correlator.incidents.publish.errors`

## Kubernetes

A Helm chart is available under `deploy/helm/signal-correlator`.

```bash
helm upgrade --install signal-correlator deploy/helm/signal-correlator \
  --namespace incident-platform --create-namespace
```

Adjust the Kafka and Schema Registry addresses to match the infrastructure in your environment.

## Reliability and known limitations

**Important:** correlation windows, deduplication, and active incident keys are currently held in memory only. This state is lost on restart. Multiple replicas may therefore detect the same incidents independently. For production, use a shared state store or Kafka Streams with state stores/changelog topics, and make the downstream `incident-service` idempotent.

The listener uses manual record acknowledgement through Spring Kafka's `RECORD` ack mode and rethrows processing errors. An explicit retry/DLQ topology, poison-message policy, and transactional consume-transform-produce flow still need to be configured for production.

The current detector counts all ERROR/CRITICAL signals for a resource together, regardless of `signalType`. A future iteration could add rules per signal type, weighted severity, recovery/reset signals, and configurable detector profiles.

## Security boundary

This service detects and publishes incidents. It has no Kubernetes client, cluster write permissions, remediation credentials, or AI agent. A detected incident is not authorisation to execute a remediation action.
