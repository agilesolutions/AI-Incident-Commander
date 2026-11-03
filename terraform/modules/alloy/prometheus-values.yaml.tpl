grafana:

  enabled: true

  adminPassword: admin

prometheus:

  prometheusSpec:

    retention: 30d

alertmanager:
  enabled: true