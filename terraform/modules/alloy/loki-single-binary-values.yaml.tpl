# -----------------------------------------------------------------------------
# Loki Helm Values
# Development configuration for Docker Desktop Kubernetes (Windows)
# Single Binary deployment
# -----------------------------------------------------------------------------

deploymentMode: SingleBinary

loki:
  auth_enabled: false

  commonConfig:
    replication_factor: 1

  storage:
    type: filesystem

  schemaConfig:
    configs:
      - from: "2024-01-01"
        store: tsdb
        object_store: filesystem
        schema: v13
        index:
          prefix: index_
          period: 24h

  limits_config:
    allow_structured_metadata: true
    volume_enabled: true

  compactor:
    working_directory: /var/loki/compactor

  storage_config:
    filesystem:
      directory: /var/loki/chunks

singleBinary:
  replicas: 1

  persistence:
    enabled: true
    storageClass: hostpath
    accessModes:
      - ReadWriteOnce
    size: 10Gi

gateway:
  enabled: false

backend:
  replicas: 0

read:
  replicas: 0

write:
  replicas: 0

ingester:
  replicas: 0

querier:
  replicas: 0

queryFrontend:
  replicas: 0

queryScheduler:
  replicas: 0

indexGateway:
  replicas: 0

distributor:
  replicas: 0

compactor:
  replicas: 0

ruler:
  enabled: false

chunksCache:
  enabled: false

resultsCache:
  enabled: false

minio:
  enabled: false

monitoring:
  dashboards:
    enabled: false

  serviceMonitor:
    enabled: false

  selfMonitoring:
    enabled: false

  lokiCanary:
    enabled: false

test:
  enabled: false