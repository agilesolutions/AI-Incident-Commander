# Technische Architectuur: Soevereine Kubernetes voor de Nederlandse Overheid

Dit document biedt een overzicht van de technische architectuur en inrichting van soevereine Kubernetes-omgevingen binnen de Nederlandse overheid. Het combineert de principes van het Rijkscloudbeleid, digitale soevereiniteit (hét lokale alternatief voor hyperscalers) en de platform-onafhankelijke **Haven-standaard** (Common Ground / VNG).

---

## 1. Kernprincipes van de Soevereine Overheidscloud

De architectuur is *Sovereign by Design* ingericht om cloud-native flexibiliteit te combineren met strikte fysieke, logische en juridische datarisolatie.

*   **100% Nederlandse Bodem:** Alle data en applicaties draaien fysiek in streng beveiligde Nederlandse datacenters (ISO 27001 en ISAE3000 SOC2 gecertificeerd).
*   **Exclusief Nederlands Recht:** De infrastructuur en het beheer liggen volledig in Nederlandse handen. Dit elimineert de risico's van buitenlandse wetgeving zoals de Amerikaanse *CLOUD Act* of *FISA 702*.
*   **Compliance & Richtlijnen:** De inrichting sluit naadloos aan op de **NIS2-richtlijn**, het *Basisbeveiligingsniveau Rijk* (BVR) en de aankomende EUCS-certificeringen.

---

## 2. Het Architectuurmodel & Control Planes

Binnen overheidsarchitecturen worden hoofdzakelijk twee technische modellen voor soevereine Kubernetes-omgevingen toegepast:

1.  **De CNCF/Bare-Metal Route:** Een architectuur opgebouwd uit open-source componenten van de *Cloud Native Computing Foundation* (CNCF), direct draaiend op fysieke servers om *vendor lock-in* te voorkomen conform de Aanbestedingswet.
2.  **De Enterprise Sovereign Stack:** Omgevingen die gebruikmaken van bewezen enterprise-software (zoals VMware Tanzu of OpenShift) die volledig ontkoppeld (**air-gapped**) gedraaid kunnen worden.

### Air-Gapped & "Disconnected" Control Planes
Voor kritieke overheidsapplicaties mag de Kubernetes *Control Plane* geen operationele of telemetrische lijnen hebben lopen naar partijen buiten Europa.
*   **Geïsoleerde Runtime:** Updates, securitypatches en container-images worden exclusief binnengehaald via lokale, streng beveiligde Nederlandse repositories (zoals een private Harbor-registry), zonder live verbinding met buitenlandse public-cloud back-ends.
*   **Volledig Nederlands Beheer:** Het operationele beheer van worker-nodes en control planes wordt uitgevoerd door gescreend personeel op Nederlandse bodem.

---

## 3. De Haven-standaard Implementatie

De **Haven-standaard** zorgt ervoor dat overheidsapplicaties platform-agnostisch zijn en op elk compliant Kubernetes-cluster kunnen draaien zonder aanpassingen.

### Haven+ Componenten (Sovereign Cloud-Native Extensies)
Waar de basis Haven-standaard public-cloud API's toestaat, vervangt **Haven+** deze door een soevereine open-source suite binnen het cluster:
*   **Identiteit & Toegang:** Authenticatie via OpenID Connect (OIDC), veelal geïmplementeerd met een lokale **Keycloak**-instantie (in plaats van Azure AD / Entra ID).
*   **Observability:** Standaardisatie op een cloud-native stack van **Prometheus** (metrics) en **Grafana** (dashboards).
*   **Secret Management:** Platform-agnostische afhandeling via in-cluster tools zoals **HashiCorp Vault** of gecodeerde GitOps-oplossingen.
*   **Netwerk & Certificaten:** Automatisch certificaatbeheer via in-cluster **cert-manager** operators en generieke Ingress-controllers (bijv. NGINX of Traefik).

---

## 4. Code Voorbeelden & Manifesten

### 4.1 Haven-compliant Kubernetes Manifest (`deployment.yaml`)
Dit verplichte basis-manifest toont de specifieke criteria waaraan een deployment moet voldoen om door de Haven Compliancy Checker te komen (stateless, high-availability en voorzien van health probes).

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: overheid-zaak-api
  namespace: haven-apps
  labels:
    app.kubernetes.io/name: overheid-zaak-api
    app.kubernetes.io/instance: release-v1
spec:
  replicas: 3 # Haven vereist high-availability (stateless & schaalbaar)
  selector:
    matchLabels:
      app.kubernetes.io/name: overheid-zaak-api
  template:
    metadata:
      labels:
        app.kubernetes.io/name: overheid-zaak-api
    spec:
      containers:
        - name: api-container
          # Afkomstig uit een soevereine, goedgekeurde container registry (bijv. Harbor)
          image: "registry.soevereinecloud.nl/overheid/zaak-api:1.4.2"
          imagePullPolicy: IfNotPresent
          
          # 1. Haven Criterium: Verplichte Health Probes voor self-healing
          livenessProbe:
            httpGet:
              path: /healthz/live
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 20
          readinessProbe:
            httpGet:
              path: /healthz/ready
              port: 8080
            initialDelaySeconds: 5
            periodSeconds: 10

          # 2. Haven Criterium: Voorspelbaar resourcebeheer (HPA-ready)
          resources:
            limits:
              cpu: "500m"
              memory: "512Mi"
            requests:
              cpu: "250m"
              memory: "256Mi"

          # Ports openstellen conform cloud-native standaarden
          ports:
            - containerPort: 8080
              name: http

          # 3. Haven Criterium: Omgevingsvariabelen via ConfigMaps (geen hardcoded data)
          env:
            - name: DATABASE_HOST
              valueFrom:
                configMapKeyRef:
                  name: zaak-api-config
                  key: db_host
            - name: LOG_LEVEL
              valueFrom:
                configMapKeyRef:
                  name: zaak-api-config
                  key: log_level
                  
          # 4. Haven Criterium: Geheimen veilig geïnjecteerd (bijv. via HashiCorp Vault)
          envFrom:
            - secretRef:
                name: zaak-api-secrets
```

### 4.2 Haven-compliant Helm `values.yaml` Snippet
Bij distributie binnen Common Ground-projecten worden harde waarden vervangen door dynamische variabelen. Er worden **geen** annotaties gebruikt naar specifieke public cloud loadbalancers of opslag-arrays.

```yaml
# Algemene metadata
replicaCount: 3

image:
  repository: registry.soevereinecloud.nl/overheid/zaak-api
  pullPolicy: IfNotPresent
  tag: "1.4.2"

# Generieke Ingress: Geen annotaties naar AWS/Azure controllers!
ingress:
  enabled: true
  className: "nginx" # Maakt gebruik van de in-cluster Haven NGINX ingress controller
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-production" # Lokale cert-manager
  hosts:
    - host: zaak-api.gemeente.nl
      paths:
        - path: /
          pathType: ImplementationSpecific

# Platform-onafhankelijke Storage (Haven+ componenten)
volumeMounts:
  - name: shared-data
    mountPath: /data

volumes:
  - name: shared-data
    persistentVolumeClaim:
      claimName: zaak-api-pvc # Gekoppeld aan een soevereine CSI (bijv. Ceph of vSphere)
```

---

## 5. Automatische Handhaving: Haven Compliancy Checker

De technische garantie dat het Kubernetes-cluster compliant blijft, wordt gecontroleerd via de **Haven Compliancy Checker CLI**. Deze tool valideert automatisch tegen de live Kubernetes API of:
1. De pod volledig **stateless** draait (data-opslag correct ontkoppeld via abstracte PVC's).
2. Er sprake is van **vendor lock-in** (blokkeert specifieke hyperscaler cloud-annotaties).
3. De applicatie **zelfredzaam** is middels de geconfigureerde `liveness` en `readiness` probes.