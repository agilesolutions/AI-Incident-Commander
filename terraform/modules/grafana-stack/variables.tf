variable "namespace" {
  type        = string
  description = "The Kubernetes namespace where the LGTM stack will be deployed."
  default     = "observability"
}

variable "create_namespace" {
  type        = bool
  description = "Whether to create the Kubernetes namespace if it does not exist."
  default     = true
}

variable "chart_version" {
  type        = string
  description = "The specific version of the PromptlyLabs LGTM helm chart to deploy."
  default     = null # Installs the latest version if not specified
}

variable "grafana_admin_password" {
  type        = string
  description = "Custom admin password for the Grafana dashboard."
  default     = "admin"
  sensitive   = true
}

variable "persistence_enabled" {
  type        = bool
  description = "Enable persistent volume storage for the stateful components."
  default     = true
}

variable "persistence_size" {
  type        = string
  description = "The storage volume size allocated for telemetry data retention."
  default     = "20Gi"
}

variable "custom_values" {
  type        = any
  description = "Additional deep-merge values to override the PromptlyLabs Helm defaults."
  default     = {}
}

variable "opentelemetry_operator_version" {
  description = "OpenTelemetry Operator Helm chart version"
  type        = string
  default     = "0.122.1"
}

variable "cert_manager_version" {
  description = "cert-manager Helm chart version"
  type        = string
  default     = "v1.18.2"
}

variable "prometheus_service_url" {
  type        = string
  description = "Prometheus URL used by Tempo metrics-generator for service graph metrics."

  default = null
}
