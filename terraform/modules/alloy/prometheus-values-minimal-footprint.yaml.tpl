# ==========================================
# GLOBAL & EXPORTER OPTIMIZATIONS
# ==========================================
global:
  rbac:
    create: true

# Alertmanager is heavy and usually unnecessary for simple local development
alertmanager:
  enabled: false

# Grafana optimization
grafana:
  enabled: true

  # =========================================================================
  # THIS BLOCK AUTOMATICALLY PROVISIONS LOKI & TEMPO AT STARTUP
  # =========================================================================
  additionalDataSources:
    - name: Loki
      type: loki
      uid: loki
      access: proxy
      url: http://loki:3100
      jsonData:
        maxLines: 1000
        derivedFields:
          - datasourceUid: tempo
            matcherRegex: "(?:trace_id|traceId|trace)=([a-zA-Z0-9]+)"
            name: TraceID
            url: "$${__value.raw}"

    - name: Tempo
      type: tempo
      uid: tempo
      access: proxy
      url: http://tempo:3200
      jsonData:
        httpMethod: GET
        traceToLogs:
          datasourceUid: loki
          tags: [ "job", "instance", "pod", "container" ]
          mappedTags:
            - key: service.name
              value: container
          filterByTraceID: true
          filterBySpanID: false
  replicaCount: 1
  resources:
    limits:
      memory: 200Mi
    requests:
      memory: 100Mi

# Disable heavy node agent if you only care about Kubernetes/Pod metrics
# Pattern 1 (Standard for newer charts)
prometheus-node-exporter:
  enabled: false

# Pattern 2 (Alternative global/override flag used by some versions)
nodeExporter:
  enabled: false

# kube-state-metrics optimization
kube-state-metrics:
  enabled: true
  resources:
    limits:
      memory: 150Mi
    requests:
      memory: 50Mi

# ==========================================
# PROMETHEUS CORE OPTIMIZATIONS
# ==========================================
prometheus:
  enabled: true
  prometheusSpec:
    # Enable remote write receiver for Tempo metrics (default: false)
    enableRemoteWriteReceiver: true
    # Enable exemplar storage for metrics-traces linking (default: [])
    enableFeatures:
      - exemplar-storage
      - native-histograms

    replicaCount: 1

    # Drastically reduce data retention to save memory/disk in Docker Desktop
    retention: 2h
    retentionSize: 1GiB

    # Scale down scrape intervals to process fewer data samples over time
    scrapeInterval: 30s
    evaluationInterval: 30s

    # Strict resource bounds to force Prometheus to stay lean
    resources:
      limits:
        memory: 500Mi
      requests:
        memory: 250Mi

    # Drop high-cardinality cAdvisor metrics that bloat local environments
    # This filters out redundant container metrics before they hit memory
    metricRelabelings:
      - action: drop
        sourceLabels: [__name__]
        regex: '(container_tasks_state|container_memory_failures_total|container_sockets|container_threads|container_cpu_cfs_periods_total)'

# ==========================================
# KUBERNETES SYSTEM COMPONENT SCRAPING
# ==========================================
# Docker Desktop doesn't expose control plane components directly; disable them to prevent error logs
kubeApiServer:
  enabled: false
kubeControllerManager:
  enabled: false
kubeScheduler:
  enabled: false
kubeProxy:
  enabled: false
