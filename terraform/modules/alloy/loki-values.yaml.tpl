# =========================================================================
# Loki Helm Chart 6.6.2 Optimized Configuration for Docker Desktop (Windows)
# Deployment Mode: Monolithic Single Binary with Local Filesystem Storage
# =========================================================================

deploymentMode: Monolithic

# 1. Global Component Toggles (Disable heavy cloud-scale microservices)
backend:
  replicas: 0
read:
  replicas: 0
write:
  replicas: 0

# 2. Configure Monolithic Architecture Pod
singleBinary:
  replicas: 1
  # Allocate conservative development resources suitable for local Windows limits
  resources:
    limits:
      cpu: "1"
      memory: 1Gi
    requests:
      cpu: "200m"
      memory: 256Mi

  # Configure persistent local volume storage inside Docker Desktop's VM
  persistence:
    enabled: true
    size: 5Gi
    # Docker Desktop provides 'hostpath' storage mapping via 'hostpath' or 'standard'
    storageClass: "hostpath"

# 3. Core Loki Configuration Schema (Required for Chart 6.x+)
loki:
  auth_enabled: false

  # Map tracking database formats to local storage paths rather than S3
  commonConfig:
    replication_factor: 1
    path_prefix: /var/loki

  storage:
    type: filesystem

  schemaConfig:
    configs:
      - from: "2024-04-01"
        store: tsdb
        object_store: filesystem
        schema: v13
        index:
          prefix: loki_index_
          period: 24h

  # Strict resource adjustments to stop querier timeouts on Windows hosts
  limits_config:
    allow_structured_metadata: true
    reject_old_samples: true
    reject_old_samples_max_age: 168h
    max_query_parallelism: 2

# 4. Gateway (NGINX routing engine)
# Provides a clean entry point wrapper for your local Grafana instance
gateway:
  enabled: true
  replicas: 1
  # CRITICAL FIX FOR MONOLITHIC MODE:
  # Overrides NGINX to route reads directly to the monolithic pod instead of query-frontend
  deploymentMode: Monolithic
  resources:
    limits:
      cpu: "200m"
      memory: 128Mi
    requests:
      cpu: "50m"
      memory: 64Mi
  service:
    type: ClusterIP
    port: 80

# 5. Disable default cloud-scale caching engines to prevent memory leaks
chunksCache:
  allocatedMemory: 0
  enabled: false
resultsCache:
  allocatedMemory: 0
  enabled: false

# 6. Disable internal telemetry generation components
loki-canary:
  enabled: false