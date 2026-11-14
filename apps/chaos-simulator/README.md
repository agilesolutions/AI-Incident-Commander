# Chaos Simulator

`chaos-simulator` is a Spring Boot service for AI Incident Commander. It generates **synthetic telemetry events** and publishes them to Kafka. These events are used to demonstrate and test the event-driven incident-response flow.

> Safety boundary: this application simulates signals. It does not stop containers, delete pods, or make real infrastructure or deployment changes.

## Technology

- Java 25
- Spring Boot 4.0.0
- Gradle
- Apache Kafka / Redpanda
- Apache Avro and Confluent Schema Registry serializer
- Springdoc/OpenAPI annotations
- JUnit 5, Mockito, and Spring MVC test support
- Docker and Helm

## Scenarios

| Scenario | Simulated signal | Example value |
|---|---|---:|
| `HIGH_ERROR_RATE` | `HTTP_ERROR_RATE` | 18.5 % |
| `HIGH_LATENCY` | `HTTP_LATENCY` | 2400 ms |
| `FAILED_DEPLOYMENT` | `DEPLOYMENT_FAILURE` | 1 failure |
| `RESOURCE_PRESSURE` | `CPU_UTILIZATION` | 96 % |
| `NORMAL_RECOVERY` | `HTTP_ERROR_RATE` | 0.2 % |

## API

`POST /api/v1/chaos/scenarios` accepts a scenario and publishes an Avro `TelemetryEvent` to topic `incident.telemetry.v1`.

Example:

```bash
curl -X POST http://localhost:8081/api/v1/chaos/scenarios \
  -H 'Content-Type: application/json' \
  -d '{"scenario":"HIGH_ERROR_RATE","resourceId":"checkout-service","environment":"demo","serviceName":"checkout-service"}'
```

Available scenarios are the enum values defined in `ScenarioType`. Default values are used for `resourceId`, `environment`, and `serviceName` when they are omitted.

The API returns HTTP 202 with a `runId`, scenario, resource, and `SUBMITTED` status. This means the request has been accepted and publishing has started; check the application logs for any Kafka publishing errors.

## Run locally

Prerequisites: JDK 25, Gradle, and a reachable Kafka/Redpanda broker with Schema Registry.

```bash
gradle test
gradle bootRun
```

By default, the application expects:

- Kafka: `localhost:9092`
- Schema Registry: `http://localhost:8081`
- API: `http://localhost:8080`

Example environment variables:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SCHEMA_REGISTRY_URL=http://localhost:8081
export SIMULATOR_ENABLED=false
gradle bootRun
```

`SIMULATOR_ENABLED` defaults to `false`. Set it to `true` only when you want periodic synthetic error-rate events to be published automatically. The interval is configured through `SIMULATOR_INTERVAL_MS`.

## Avro and Schema Registry

The schema at `src/main/resources/avro/telemetry-event.avsc` is copied from the shared event contracts. The publisher builds an Avro `GenericRecord` and uses `KafkaAvroSerializer`. The topic name is configurable through `SIMULATOR_TOPIC` and defaults to `incident.telemetry.v1`.

Schema Registry value-subject compatibility must align with the contracts used by the rest of the platform. In a development environment, `SCHEMA_REGISTRY_AUTO_REGISTER=true` can be used. In a controlled production environment, it is preferable to register schemas in advance and disable automatic registration.

## Configuration

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8081` | HTTP port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Schema Registry URL |
| `SCHEMA_REGISTRY_AUTO_REGISTER` | `true` | Automatic schema registration |
| `SIMULATOR_ENABLED` | `false` | Enable periodic scenarios |
| `SIMULATOR_INTERVAL_MS` | `30000` | Periodic publishing interval |
| `SIMULATOR_SOURCE` | `chaos-simulator` | Event source |
| `SIMULATOR_ENVIRONMENT` | `demo` | Default environment |
| `SIMULATOR_TOPIC` | `incident.telemetry.v1` | Kafka topic |

## Observability

Actuator endpoints:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

## Docker and Helm

Build the application and container:

```bash
gradle clean bootJar
docker build -t chaos-simulator:0.1.0 .
```

Deploy with Helm from the module directory:

```bash
helm upgrade --install chaos-simulator deploy/helm/chaos-simulator \
  --set image.repository=chaos-simulator \
  --set image.tag=0.1.0
```

Check the broker and Schema Registry addresses for your Kubernetes namespace before deploying. The default `schemaRegistryUrl` in `values.yaml` is an example and must be adjusted to match the selected Redpanda/Kafka installation.

## Tests

```bash
gradle test
```

The tests cover scenario requests, defaults, the HTTP 202 response, and validation. This package has been generated but has not yet been built or executed locally in this environment; run the tests with JDK 25 and validate the Spring Boot, Kafka, and Confluent dependency versions in your build environment.
