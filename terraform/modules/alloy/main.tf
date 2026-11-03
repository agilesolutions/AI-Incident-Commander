resource "kubernetes_namespace_v1" "monitoring" {
  metadata {
    name = var.namespace
  }
}

##############################################
# Loki- for logs (log aggregation system)
##############################################

# ==========================================
# MINIMAL LOKI HELM RELEASE
# ==========================================
resource "helm_release" "loki" {
  name       = "loki"
  repository = "https://grafana.github.io/helm-charts"
  chart      = "loki"
  version    = var.loki_version
  namespace  = var.namespace
  values = [
    templatefile("${path.module}/loki-single-binary-values.yaml.tpl",{})
  ]
}

##############################################
# Tempo- for traces (distributed tracing backend)
##############################################
resource "helm_release" "tempo" {
  name = "tempo"
  namespace = var.namespace
  repository = "https://grafana.github.io/helm-charts"
  chart = "tempo"
  version = var.tempo_version
  values = [
    templatefile(
      "${path.module}/tempo-values.yaml.tpl",
      {
        namespace = var.namespace
      }
    )
  ]

  depends_on = [helm_release.loki]

}
##############################################
# Monitoring - for metrics (long-term storage for Prometheus metrics)
##############################################
resource "helm_release" "prometheus" {
  name = "kube-prometheus-stack"
  namespace = var.namespace
  repository = "https://prometheus-community.github.io/helm-charts"
  chart = "kube-prometheus-stack"
  version = var.prometheus_version
  values = [
    templatefile(
      "${path.module}/prometheus-values-minimal-footprint.yaml.tpl",
      {
        tempo_endpoint     = var.tempo_endpoint
        loki_url    = var.loki_url
      }
    )
  ]

  depends_on = [helm_release.tempo]
}
##############################################
# Grafana Alloy : Grafana’s official open-source OpenTelemetry Collector
##############################################

resource "kubernetes_config_map_v1" "alloy" {
  metadata {
    name      = "alloy-config"
    namespace = var.namespace
  }
  data = {
    "config.alloy" = templatefile(
      "${path.module}/alloy.river.tpl",
      {
        loki_url                    = var.loki_url
        tempo_endpoint              = var.tempo_endpoint
        prometheus_remote_write_url = var.prometheus_remote_write_url
      }
    )
  }

  depends_on = [helm_release.prometheus]

}

resource "helm_release" "alloy" {
  name       = "alloy"
  namespace  = var.namespace
  repository = "https://grafana.github.io/helm-charts"
  chart      = "alloy"
  version    = var.alloy_version
  values = [
    templatefile(
      "${path.module}/alloy-values.yaml.tpl",
      {
        namespace = var.namespace
      }
    )
  ]
  depends_on = [
    kubernetes_config_map_v1.alloy
  ]
}