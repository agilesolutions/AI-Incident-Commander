# Chaos Simulator

De `chaos-simulator` is een Spring Boot-service voor AI Incident Commander die **synthetische telemetrie-events** genereert en naar Kafka publiceert. De events zijn bedoeld om de event-driven incidentketen te demonstreren en te testen.

> Veiligheidsgrens: deze applicatie simuleert signalen. Ze stopt geen containers, verwijdert geen pods en voert geen echte infrastructuur- of deploymentwijzigingen uit.

## Technologie

- Java 25
- Spring Boot 4.0.0
- Gradle
- Apache Kafka / Redpanda
- Apache Avro en Confluent Schema Registry serializer
- Springdoc/OpenAPI-annotaties
- JUnit 5, Mockito en Spring MVC-testondersteuning
- Docker en Helm

## Scenario's

| Scenario | Gesimuleerd signaal | Voorbeeldwaarde |
|---|---|---:|
| `HIGH_ERROR_RATE` | `HTTP_ERROR_RATE` | 18.5 % |
| `HIGH_LATENCY` | `HTTP_LATENCY` | 2400 ms |
| `FAILED_DEPLOYMENT` | `DEPLOYMENT_FAILURE` | 1 failure |
| `RESOURCE_PRESSURE` | `CPU_UTILIZATION` | 96 % |
| `NORMAL_RECOVERY` | `HTTP_ERROR_RATE` | 0.2 % |

## API

`POST /api/v1/chaos/scenarios` accepteert een scenario en publiceert een Avro `TelemetryEvent` op topic `incident.telemetry.v1`.

Voorbeeld:

```bash
curl -X POST http://localhost:8081/api/v1/chaos/scenarios \
  -H 'Content-Type: application/json' \
  -d '{"scenario":"HIGH_ERROR_RATE","resourceId":"checkout-service","environment":"demo","serviceName":"checkout-service"}'
```

Mogelijke scenario's zijn de enumwaarden uit `ScenarioType`. `resourceId`, `environment` en `serviceName` hebben standaardwaarden wanneer ze ontbreken.

De API geeft HTTP 202 terug met een `runId`, scenario, resource en status `SUBMITTED`. Dit betekent dat de aanvraag is geaccepteerd en dat de publicatie is gestart; controleer de applicatielogs voor eventuele Kafka-publicatiefouten.

## Lokaal starten

Vereisten: JDK 25, Gradle en een bereikbare Kafka/Redpanda-broker met Schema Registry.

```bash
gradle test
gradle bootRun
```

Standaard verwacht de applicatie:

- Kafka: `localhost:9092`
- Schema Registry: `http://localhost:8081`
- API: `http://localhost:8080`

Voorbeeld omgevingsvariabelen:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export SCHEMA_REGISTRY_URL=http://localhost:8081
export SIMULATOR_ENABLED=false
gradle bootRun
```

`SIMULATOR_ENABLED` staat standaard op `false`. Zet deze alleen op `true` als je periodiek automatisch synthetische foutpercentage-events wilt publiceren. De interval wordt ingesteld met `SIMULATOR_INTERVAL_MS`.

## Avro en Schema Registry

Het schema `src/main/resources/avro/telemetry-event.avsc` is overgenomen uit de gedeelde event-contracten. De publisher bouwt een Avro `GenericRecord` en gebruikt `KafkaAvroSerializer`. De topicnaam is configureerbaar met `SIMULATOR_TOPIC` en standaard `incident.telemetry.v1`.

Voor de Schema Registry moet de value-subjectcompatibiliteit aansluiten op de contracten van de rest van het platform. In een ontwikkelomgeving kan `SCHEMA_REGISTRY_AUTO_REGISTER=true` worden gebruikt; in een gecontroleerde productieomgeving is het beter schema's vooraf te registreren en automatische registratie uit te schakelen.

## Configuratie

| Variabele | Standaard | Betekenis |
|---|---|---|
| `SERVER_PORT` | `8081` | HTTP-poort |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrap servers |
| `SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Schema Registry URL |
| `SCHEMA_REGISTRY_AUTO_REGISTER` | `true` | Automatische schemaregistratie |
| `SIMULATOR_ENABLED` | `false` | Periodieke scenario's inschakelen |
| `SIMULATOR_INTERVAL_MS` | `30000` | Interval voor periodieke publicatie |
| `SIMULATOR_SOURCE` | `chaos-simulator` | Eventbron |
| `SIMULATOR_ENVIRONMENT` | `demo` | Standaardomgeving |
| `SIMULATOR_TOPIC` | `incident.telemetry.v1` | Kafka-topic |

## Observability

Actuator-endpoints:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

## Docker en Helm

Bouw de applicatie en container:

```bash
gradle clean bootJar
docker build -t chaos-simulator:0.1.0 .
```

Deploy met Helm vanuit de modulemap:

```bash
helm upgrade --install chaos-simulator deploy/helm/chaos-simulator \
  --set image.repository=chaos-simulator \
  --set image.tag=0.1.0
```

Controleer de broker- en Schema Registry-adressen voor jouw Kubernetes-namespace voordat je deployt. Het standaard `schemaRegistryUrl` in `values.yaml` is een voorbeeld en moet worden aangepast aan de gekozen Redpanda/Kafka-installatie.

## Tests

```bash
gradle test
```

De tests dekken de scenario-aanvraag, standaardwaarden, de HTTP 202-response en validatie. Dit pakket is gegenereerd maar is nog niet lokaal gebouwd of uitgevoerd in deze omgeving; voer de tests uit met JDK 25 en valideer de gebruikte Spring Boot-, Kafka- en Confluent-dependencyversies in jouw buildomgeving.
