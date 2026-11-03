variable "name" {
  description = "PostgreSQL Helm release name"
  type        = string
  default     = "postgresql"
}

variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
  default     = "notebook"
}

variable "chart_version" {
  description = "Bitnami PostgreSQL Helm chart version"
  type        = string
}

variable "database" {
  description = "PostgreSQL database name"
  type        = string
}

variable "username" {
  description = "PostgreSQL username"
  type        = string
}

variable "password" {
  description = "PostgreSQL password"
  type        = string
  sensitive   = true
}

variable "storage_size" {
  description = "Persistent volume size"
  type        = string
  default     = "8Gi"
}