# Signal Correlator

## Doel

`signal-correlator` consumeert synthetische en operationele telemetrie-events van Kafka, correleert foutsignalen deterministisch en publiceert een `IncidentDetected`-event wanneer een configureerbare drempel wordt bereikt.

De service voert geen AI-analyse uit en neemt geen herstelmaatregelen. Detectie is deterministisch en uitlegbaar; onderzoek en remediation vinden plaats in de volgende componenten.

## Eventflow

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

## Detectieregels

- Alleen `ERROR`- en `CRITICAL`-telemetrie telt mee voor de huidige detector.
- Signalen worden gegroepeerd op `environment + resourceId`.
- De detector gebruikt een configureerbaar tijdvenster (`window-seconds`).
- Bij minimaal `threshold` signalen binnen het venster wordt een incident-event gepubliceerd.
- Het incident krijgt `CRITICAL` wanneer ten minste één bewijs-event `CRITICAL` is; anders `HIGH`.
- `eventId` wordt gebruikt voor in-memory deduplicatie.
- Na detectie voorkomt een deduplicatievenster herhaald publiceren voor dezelfde resource.
- `evidenceEventIds`, `correlationId` en `causationId` worden opgenomen in het incident-event.

Dit is een eerste deterministische baseline, geen geavanceerde anomaliedetectie. Drempels moeten per type workload worden gekalibreerd.

## Contracten

De service gebruikt de Avro-contracten uit `libs/event-contracts`:

- `TelemetryEvent` op `incident.telemetry.v1`
- `IncidentDetected` op `incident.detected.v1`

De ZIP bevat lokale kopieën van de twee `.avsc`-bestanden als runtime-resources, zodat deze module zelfstandig kan worden gebouwd. In de monorepo blijven de schema's in `libs/event-contracts/src/main/avro` de canonieke bron. Houd de kopieën synchroon of vervang deze runtime-resources bij integratie door een gedeelde schema-artifact.

Schema Registry wordt gebruikt via Confluent Avro Serializer/Deserializer. Auto-registration staat in deze baseline aan in de producerconfiguratie; voor productie verdient gecontroleerde schema-publicatie in CI de voorkeur.

## Configuratie

| Variabele | Standaard | Beschrijving |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka bootstrapservers |
| `SCHEMA_REGISTRY_URL` | `http://localhost:8081` | Schema Registry URL |
| `KAFKA_CONSUMER_GROUP` | `signal-correlator` | Consumer group |
| `CORRELATOR_INPUT_TOPIC` | `incident.telemetry.v1` | Inputtopic |
| `CORRELATOR_OUTPUT_TOPIC` | `incident.detected.v1` | Outputtopic |
| `CORRELATOR_DETECTOR_ID` | `signal-correlator-v1` | Detectoridentificatie |
| `CORRELATOR_THRESHOLD` | `3` | Aantal foutsignalen voor detectie |
| `CORRELATOR_WINDOW_SECONDS` | `120` | Correlatievenster in seconden |
| `CORRELATOR_DEDUP_TTL_SECONDS` | `900` | Tijd waarin herhaalde detectie wordt onderdrukt |
| `CORRELATOR_ENVIRONMENT` | leeg | Optioneel filter op environment |
| `SERVER_PORT` | `8080` | Actuator HTTP-poort |

## Bouwen en testen

Vereist: JDK 25 en Gradle 9.x of een compatibele Gradle-installatie.

```bash
gradle clean test
```

Start de service:

```bash
gradle bootRun
```

De applicatie verwacht dat Kafka en Schema Registry bereikbaar zijn. De health endpoints zijn beschikbaar via `/actuator/health`; Prometheus-metrics via `/actuator/prometheus`.

## Metrics

- `correlator.telemetry.received`
- `correlator.telemetry.duplicates`
- `correlator.telemetry.processing.errors`
- `correlator.incidents.detected`
- `correlator.incidents.published`
- `correlator.incidents.publish.errors`

## Kubernetes

Een Helm-chart staat onder `deploy/helm/signal-correlator`.

```bash
helm upgrade --install signal-correlator deploy/helm/signal-correlator \
  --namespace incident-platform --create-namespace
```

Pas de Kafka- en Schema Registry-adressen aan op de infrastructuur van de omgeving.

## Betrouwbaarheid en bekende beperkingen

**Belangrijk:** de correlatievensters, deduplicatie en actieve incident-sleutels worden momenteel alleen in geheugen bijgehouden. Bij een restart gaat deze state verloren. Meerdere replicas kunnen daardoor dezelfde incidenten onafhankelijk detecteren. Gebruik voor productie een gedeelde state store of Kafka Streams met state stores/changelog-topics, en maak de downstream `incident-service` idempotent.

De listener gebruikt handmatige record-acknowledgement via Spring Kafka's `RECORD`-ack-mode en werpt verwerkingsfouten opnieuw op. Een expliciete retry/DLQ-topologie, poison-messagebeleid en transactionele consume-transform-produce-flow moeten nog worden ingericht voor productie.

De huidige detector telt alle ERROR/CRITICAL signalen voor een resource samen, ongeacht `signalType`. Een volgende iteratie kan regels per signaaltype, gewogen ernst, recovery/reset-signalen en configureerbare detectorprofielen toevoegen.

## Veiligheidsgrens

Deze service detecteert en publiceert incidenten. Zij heeft geen Kubernetes-client, cluster write-permissions, remediation credentials of AI-agent. Een gedetecteerd incident is geen toestemming om een herstelactie uit te voeren.
