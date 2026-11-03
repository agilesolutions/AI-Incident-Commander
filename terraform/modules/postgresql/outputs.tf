output "service_name" {
  description = "PostgreSQL Kubernetes service"
  value       = "${var.name}-postgresql"
}

output "namespace" {
  description = "PostgreSQL namespace"
  value       = var.namespace
}

output "port" {
  description = "PostgreSQL port"
  value       = 5432
}

output "connection_host" {
  description = "Internal PostgreSQL DNS name."
  value       = "mongodb.${var.namespace}.svc.cluster.local"
}