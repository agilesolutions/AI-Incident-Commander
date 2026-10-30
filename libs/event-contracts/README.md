
# Event Contracts

## Overzicht

Deze module bevat de gedeelde Apache Avro-schema's voor de Kafka-events die worden uitgewisseld tussen de verschillende services van **AI Incident Commander**.

De event-contracten definiëren de structuur van berichten waarmee services informatie uitwisselen over telemetrie, incidenten, onderzoeken en herstelacties.

Het doel is om een consistente, versieerbare en controleerbare communicatie tussen de services te realiseren.

### Ontwerpprincipe

> **AI onderzoekt en adviseert. Gecontroleerde automatisering voert uit. Een mens houdt de regie.**

Dit principe is met name belangrijk voor herstelacties: een voorgestelde actie mag nooit automatisch worden beschouwd als toestemming om deze uit te voeren.

## Verantwoordelijkheden

Deze module is verantwoordelijk voor:

- Het definiëren van gedeelde Avro-schema's.
- Het vastleggen van de structuur en metadata van events.
- Het ondersteunen van versiebeheer en compatibiliteitscontroles.
- Het leveren van gemeenschappelijke contracten aan Kafka-producers en -consumers.

Deze module bevat nadrukkelijk geen:

- Bedrijfslogica.
- Database-entiteiten of persistentielogica.
- Kafka-producers of -consumers.
- Logica voor het uitvoeren van herstelacties.
- AI-agents of LLM-integraties.

De module vormt daarmee een gedeelde contractlaag voor de event-driven architectuur.

## Architectuur

De event-contracten worden gebruikt door de volgende applicaties:

- `chaos-simulator`
- `signal-correlator`
- `incident-service`
- `investigation-agent`
- `remediation-service`

De globale eventflow is:

```text
Chaos Simulator
      |
      | TelemetryEvent
      v
     Kafka
      |
      v
Signal Correlator
      |
      | IncidentDetected
      v
     Kafka
      |
      v
Incident Service
      |
      | InvestigationRequested
      v
Investigation Agent
      |
      | InvestigationCompleted
      | RemediationProposed
      v
     Kafka
      |
      v
Human Approval
      |
      | RemediationApproved
      v
Remediation Service
      |
      | RemediationExecuted
      | RemediationVerified
      v
     Kafka
```

De exacte verwerking en verantwoordelijkheden worden geïmplementeerd in de afzonderlijke applicatiemodules.

## Pakketnamespace

De Avro-records gebruiken de volgende namespace:

```text
com.agilesolutions.aiincidentcommander.contracts.v1
```

De namespace bevat de contracten voor versie 1 van de eventstructuren.

## Beschikbare Avro-schema's

De module bevat de volgende schema's:

| Bestand | Avro-record | Doel |
|---|---|---|
| `telemetry-event.avsc` | `TelemetryEvent` | Technische signalen en telemetrie vastleggen |
| `incident-detected.avsc` | `IncidentDetected` | Een nieuw gedetecteerd incident publiceren |
| `incident-updated.avsc` | `IncidentUpdated` | Een wijziging van een incident publiceren |
| `investigation-requested.avsc` | `InvestigationRequested` | Een onderzoek naar een incident aanvragen |
| `investigation-completed.avsc` | `InvestigationCompleted` | De resultaten van een onderzoek publiceren |
| `remediation-proposed.avsc` | `RemediationProposed` | Een voorgestelde herstelactie publiceren |
| `remediation-approval-requested.avsc` | `RemediationApprovalRequested` | Menselijke goedkeuring aanvragen |
| `remediation-approved.avsc` | `RemediationApproved` | Een herstelvoorstel goedgekeurd verklaren |
| `remediation-rejected.avsc` | `RemediationRejected` | Een herstelvoorstel afwijzen |
| `remediation-executed.avsc` | `RemediationExecuted` | De uitvoering van een herstelactie registreren |
| `remediation-failed.avsc` | `RemediationFailed` | Een mislukte herstelactie registreren |
| `remediation-verified.avsc` | `RemediationVerified` | Het resultaat van een herstelactie verifiëren |

De daadwerkelijke `.avsc`-bestanden zijn altijd leidend voor de precieze velddefinities, datatypes, standaardwaarden en eventuele optionele velden.

## Eventmetadata

Events kunnen gemeenschappelijke metadata bevatten om berichten te correleren, te traceren en te verwerken.

| Veld | Betekenis |
|---|---|
| `eventId` | Unieke identificatie van het event |
| `schemaVersion` | Versie van het gebruikte contract, met standaardwaarde `1.0.0` waar gedefinieerd |
| `occurredAt` | Tijdstip waarop het event plaatsvond, in UTC en RFC 3339-formaat |
| `correlationId` | Identificatie om gerelateerde gebeurtenissen binnen één incident te correleren |
| `causationId` | Optionele identificatie van het event dat de huidige gebeurtenis heeft veroorzaakt |

Niet ieder schema hoeft al deze velden te bevatten. Raadpleeg daarom altijd het betreffende Avro-schema voordat je een producer of consumer implementeert.

## Kafka-topics

De volgende topicnamen worden als conventie voorgesteld voor de eventstromen:

| Topic | Event |
|---|---|
| `incident.telemetry.v1` | `TelemetryEvent` |
| `incident.detected.v1` | `IncidentDetected` |
| `incident.updated.v1` | `IncidentUpdated` |
| `incident.investigation.requested.v1` | `InvestigationRequested` |
| `incident.investigation.completed.v1` | `InvestigationCompleted` |
| `incident.remediation.proposed.v1` | `RemediationProposed` |
| `incident.remediation.approval-requested.v1` | `RemediationApprovalRequested` |
| `incident.remediation.approved.v1` | `RemediationApproved` |
| `incident.remediation.rejected.v1` | `RemediationRejected` |
| `incident.remediation.executed.v1` | `RemediationExecuted` |
| `incident.remediation.failed.v1` | `RemediationFailed` |
| `incident.remediation.verified.v1` | `RemediationVerified` |

**Let op:** deze topicnamen zijn architectuurconventies. Ze moeten nog worden afgestemd op en geconfigureerd in de daadwerkelijke Kafka-infrastructuur en de afzonderlijke services.

### Partitionering

Voor events binnen de levenscyclus van een incident is `incidentId` een geschikte Kafka-recordkey.

Hierdoor kunnen events voor hetzelfde incident in dezelfde partitie terechtkomen, zodat hun volgorde binnen die partitie behouden blijft.

Voor telemetrie kan bijvoorbeeld `resourceId` worden gebruikt als stabiele recordkey.

Kafka garandeert geen globale volgorde over verschillende partities heen. Consumers moeten daarom rekening houden met vertraagde, dubbele of buiten de verwachte volgorde ontvangen events.

## Schema Registry

De architectuur is bedoeld om met een Avro Schema Registry te kunnen werken.

Een mogelijke conventie is om voor ieder topic een value-subject te gebruiken met de naam:

```text
<topic-name>-value
```

Bijvoorbeeld:

```text
incident.detected.v1-value
```

Voor de eerste implementatie kan `BACKWARD` als uitgangspunt voor schema-compatibiliteit worden gebruikt. De definitieve compatibiliteitsstrategie moet echter worden afgestemd op de gekozen Schema Registry en de gewenste evolutie van de contracten.

Compatibiliteitscontroles horen bij voorkeur onderdeel te zijn van de CI-pipeline.

De daadwerkelijke Schema Registry-integratie en compatibiliteitscontroles moeten afzonderlijk worden geconfigureerd en getest.

## Veiligheidsprincipes voor herstelacties

De event-contracten ondersteunen een gecontroleerde herstelworkflow.

### 1. Een voorstel is geen toestemming

Een `RemediationProposed`-event beschrijft een voorgestelde herstelactie. Het geeft op zichzelf geen toestemming om die actie uit te voeren.

### 2. Menselijke goedkeuring is verplicht

Voor iedere herstelactie is expliciete goedkeuring nodig voordat uitvoering plaatsvindt.

De goedkeuring moet betrekking hebben op het exacte voorstel. De uitvoerende service moet onder meer controleren of de goedkeuring geldig is en of de voorgestelde actie niet is gewijzigd.

Waar van toepassing kan hiervoor een `approvedActionDigest` worden gebruikt.

### 3. Goedkeuring moet worden gevalideerd

De uitvoerende service mag niet uitsluitend vertrouwen op het feit dat een goedkeuringsbericht op Kafka is ontvangen.

De service moet de geldigheid van de goedkeuring, de bevoegde goedkeurder, de geldigheidsperiode en de relatie met het oorspronkelijke voorstel controleren.

### 4. Uitvoering betekent niet dat het incident is opgelost

Een `RemediationExecuted`-event geeft aan dat een herstelactie is uitgevoerd of dat de uitvoering is geregistreerd.

Het bewijst niet automatisch dat het onderliggende incident is opgelost. Daarvoor is afzonderlijke verificatie nodig, bijvoorbeeld vastgelegd met `RemediationVerified`.

### 5. Beperk de uitvoeringsmogelijkheden

- Sta uitsluitend expliciet toegestane herstelacties toe.
- Geef de AI-agent standaard geen schrijfrechten op Kubernetes-resources.
- Scheid onderzoek, goedkeuring, uitvoering en verificatie.
- Leg goedkeuringen, uitvoeringsresultaten en fouten controleerbaar vast.

## Betrouwbare eventverwerking

Kafka-events kunnen opnieuw worden afgeleverd. Consumers moeten daarom rekening houden met dubbele berichten en tijdelijke fouten.

Aanbevolen uitgangspunten:

- Verwerk events idempotent.
- Gebruik `eventId` om dubbele verwerking te herkennen.
- Behoud `correlationId` en `causationId` waar deze beschikbaar zijn.
- Valideer de huidige bedrijfsstatus voordat een overgang wordt uitgevoerd.
- Gebruik passende time-outs, retries en dead-letter-afhandeling.
- Voorkom dat gevoelige gegevens of onnodige persoonsgegevens in events worden opgenomen.
- Registreer relevante fouten en statusovergangen voor auditing en observability.

De precieze implementatie hiervan hoort bij de afzonderlijke producers en consumers.

## Projectstructuur

De module bevindt zich in de volgende locatie:

```text
libs/
└── event-contracts/
    ├── build.gradle
    ├── README.md
    └── src/
        ├── main/
        │   └── avro/
        │       ├── telemetry-event.avsc
        │       ├── incident-detected.avsc
        │       ├── incident-updated.avsc
        │       ├── investigation-requested.avsc
        │       ├── investigation-completed.avsc
        │       ├── remediation-proposed.avsc
        │       ├── remediation-approval-requested.avsc
        │       ├── remediation-approved.avsc
        │       ├── remediation-rejected.avsc
        │       ├── remediation-executed.avsc
        │       ├── remediation-failed.avsc
        │       └── remediation-verified.avsc
        └── test/
            └── ...
```

De precieze teststructuur is afhankelijk van de bestaande implementatie.

## Bouwen en testen

Voer vanuit de hoofdmap van de repository de volgende opdracht uit:

```bash
./gradlew :libs:event-contracts:test
```

Hiermee worden de tests van de module uitgevoerd, voor zover deze in de Gradle-build zijn geconfigureerd.

Een schema-parsetest kan helpen om ongeldige Avro-schema's vroegtijdig te detecteren.

### Aanvullende validatie

Voor een productiegeschikte implementatie zijn daarnaast de volgende controles wenselijk:

- Genereren en compileren van Avro Java-klassen.
- Compatibiliteitscontroles tegen bestaande schema's in de Schema Registry.
- Contracttests voor Kafka-producers en -consumers.
- Integratietests voor eventverwerking en foutafhandeling.

Deze aanvullende controles zijn niet automatisch aanwezig alleen doordat de schema's bestaan. Ze moeten expliciet in de build en CI-pipeline worden ingericht.

## Een event-contract wijzigen

Volg bij wijzigingen aan een bestaand schema deze checklist:

1. Pas het relevante `.avsc`-bestand aan.
2. Controleer of het schema geldig is.
3. Beoordeel de achterwaartse en voorwaartse compatibiliteit.
4. Controleer welke producers en consumers door de wijziging worden geraakt.
5. Werk relevante contract- en integratietests bij.
6. Controleer idempotentie, correlatie en foutafhandeling.
7. Controleer of de wijziging de veiligheidsregels voor herstelacties intact laat.
8. Werk deze README bij wanneer de eventstructuur, topicconventies of architectuur verandert.

Verwijder of hernoem velden niet zonder eerst de gevolgen voor bestaande consumers en de Schema Registry-compatibiliteit te beoordelen.

## Ontwerpdoel

De module `event-contracts` biedt een expliciete en versieerbare communicatiegrens tussen de services van AI Incident Commander.

Door eventstructuren centraal te beheren, worden wijzigingen beter controleerbaar en kunnen services onafhankelijk worden ontwikkeld en getest.

De contracten ondersteunen een event-driven architectuur waarin AI-gestuurd onderzoek wordt gecombineerd met gecontroleerde automatisering, menselijke goedkeuring en aantoonbare verificatie van herstelacties.
