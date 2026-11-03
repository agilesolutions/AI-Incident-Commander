output "helm_release_status" {
  description = "The deployment status of the Helm chart release."
  value       = helm_release.lgtm_stack.status
}

output "grafana_service_url" {
  description = "The internal Kubernetes service URL targeting the Grafana frontend interface."
  value       = "http://lgtm-grafana.${var.namespace}.svc.cluster.local:80"
}
