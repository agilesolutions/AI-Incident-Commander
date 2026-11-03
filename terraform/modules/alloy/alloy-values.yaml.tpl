alloy:
  # Keep the built-in UI enabled on 12345
  enableHttpServerPort: true

  # =======================================================================
  # EXPLICITLY OPEN OTLP PORTS ON THE KUBERNETES SERVICE
  # =======================================================================
  extraPorts:
    - name: "otel-grpc"
      port: 4317
      targetPort: 4317
      protocol: "TCP"
    - name: "otel-http"
      port: 4318
      targetPort: 4318
      protocol: "TCP"

  configMap:
    create: false
    name: alloy-config
controller:
  type: deployment
service:
  enabled: true
  ports:
    - name: otlp-grpc
      port: 4317
    - name: otlp-http
      port: 4318
    - name: metrics
      port: 12345
resources:
  requests:
    cpu: 200m
    memory: 256Mi
  limits:
    cpu: 1000m
    memory: 1Gi