output "otlp_grpc_endpoint" {
  value = "alloy.${var.namespace}.svc.cluster.local:4317"
}

output "otlp_http_endpoint" {
  value = "http://alloy.${var.namespace}.svc.cluster.local:4318"
}