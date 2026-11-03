output "name" {
  description = "Webhook receiver name"
  value       = var.name
}

output "service_name" {
  description = "Kubernetes Service name"
  value       = kubernetes_service_v1.receiver.metadata[0].name
}

output "service_port" {
  description = "Webhook receiver service port"
  value       = var.service_port
}

output "webhook_url" {
  description = "Internal Kubernetes URL for Grafana"
  value = format(
    "http://%s.%s.svc.cluster.local:%d/webhook",
    kubernetes_service_v1.receiver.metadata[0].name,
    var.namespace,
    var.service_port
  )
}

output "health_url" {
  description = "Internal Kubernetes health URL"
  value = format(
    "http://%s.%s.svc.cluster.local:%d/health",
    kubernetes_service_v1.receiver.metadata[0].name,
    var.namespace,
    var.service_port
  )
}