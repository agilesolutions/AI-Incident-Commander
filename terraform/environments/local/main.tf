module "grafana-stack" {
  source       = "../../modules/grafana-stack"
  namespace        = "monitoring"
  create_namespace = true

  # Override default Grafana web portal administrative access controls
  grafana_admin_password = var.grafana_admin_password

  prometheus_service_url = "${var.prometheus_service_url}"

  # Ensure you ALSO remove the invalid top-level "prometheus" block if it's still here
  custom_values = {
    kube-prometheus-stack = {
      prometheus = {
        prometheusSpec = {
          retention = "15d"
        }
      }
    }
  }
}

module "postgresql" {
  source = "../../modules/postgresql"

  name          = "notebook-postgresql"
  namespace     = "database"
  chart_version = "18.12.1"

  database      = "notebook"
  username      = "notebook"
  password      = var.postgres_password
  storage_size  = "8Gi"
}

module "traefik" {
  source = "../../modules/traefik"
  namespace     = "traefik"
  replica_count = 1
  enable_metrics = false
}

module "grafana_webhook_receiver" {
  source = "../../modules/grafana-webhook-receiver"

  namespace = "monitoring"
  name      = "grafana-webhook"
}