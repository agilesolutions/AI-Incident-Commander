output "namespace" {
  value = kubernetes_namespace_v1.traefik.metadata[0].name
}

output "traefik_release_name" {
  value = helm_release.traefik.name
}

