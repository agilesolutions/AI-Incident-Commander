// ==========================================
// 1. INGESTION LAYER (OTLP RECEIVER)
// ==========================================
otelcol.receiver.otlp "default" {
  grpc {
    endpoint = "0.0.0.0:4317"
  }
  http {
    endpoint = "0.0.0.0:4318"
  }
  output {
    metrics = [otelcol.processor.batch.default.input]
    logs    = [otelcol.processor.batch.default.input]
    traces  = [otelcol.processor.batch.default.input]
  }
}

// ==========================================
// 2. PROCESSING LAYER (BATCHING)
// ==========================================
otelcol.processor.batch "default" {
  output {
    // Points directly to the correct Alloy translators and exporters
    metrics = [otelcol.exporter.prometheus.default.input]
    logs    = [otelcol.exporter.loki.default.input]
    traces  = [otelcol.exporter.otlp.tempo.input]
  }
}

// ==========================================
// 3. CONVERSION & EXPORT LAYER (METRICS)
// ==========================================
// FIX: Changed from otelcol.data.prometheus to otelcol.exporter.prometheus
otelcol.exporter.prometheus "default" {
  forward_to = [prometheus.remote_write.default.receiver]
}

prometheus.remote_write "default" {
  endpoint {
    url = "${prometheus_remote_write_url}"
  }
}

// ==========================================
// 4. CONVERSION & EXPORT LAYER (LOGS)
// ==========================================
// FIX: Changed from otelcol.to_loki to otelcol.exporter.loki
otelcol.exporter.loki "default" {
  forward_to = [loki.write.local_loki.receiver]
}

loki.write "local_loki" {
  endpoint {
    url = "${loki_url}"
  }
}

// ==========================================
// 5. EXPORT LAYER (TRACES / TEMPO)
// ==========================================
otelcol.exporter.otlp "tempo" {
  client {
    endpoint = "${tempo_endpoint}"
    tls {
      insecure = true
    }
  }
}
