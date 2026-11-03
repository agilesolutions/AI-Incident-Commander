variable "namespace" {
  description = "Kubernetes namespace for the webhook receiver"
  type        = string
  default     = "monitoring"
}

variable "name" {
  description = "Name of the webhook receiver"
  type        = string
  default     = "grafana-webhook"
}

variable "replicas" {
  description = "Number of receiver replicas"
  type        = number
  default     = 1
}

variable "image" {
  description = "Python container image"
  type        = string
  default     = "python:3.12-alpine"
}

variable "service_port" {
  description = "Webhook service port"
  type        = number
  default     = 8080
}