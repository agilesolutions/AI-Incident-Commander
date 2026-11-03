variable "cluster_name" {
  type        = string
  description = "The name of the Kubernetes cluster being monitored."
  default     = "my-kubernetes-cluster"
}

variable "namespace" {
  type        = string
  description = "The target namespace where the monitoring stack will be deployed."
  default     = "monitoring"
}

variable "chart_version" {
  type        = string
  description = "The version of the promptlylabs.github.io/lgtm-helm-chart helm chart to install."
  default     = "0.28.0" # Adjust to the latest stable major release
}

variable "postgres_password" {
  type      = string
  sensitive = true
}

variable "grafana_admin_password" {
  type      = string
  sensitive = true
}

variable "grafana_url" {
  description = "Grafana URL"
  type        = string
  default     = "http://localhost:3000"
}

variable "grafana_token" {
  description = "Grafana service account token"
  type        = string
  sensitive   = true
}

variable "prometheus_service_url" {
  type        = string
  description = "Prometheus URL used by Tempo metrics-generator for service graph metrics."

  default = null
}