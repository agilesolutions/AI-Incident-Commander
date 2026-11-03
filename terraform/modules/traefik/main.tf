# namespace
resource "kubernetes_namespace_v1" "traefik" {
  metadata {
    name = var.namespace
  }
}

# helm release
resource "helm_release" "traefik" {
  name = "traefik-nginx"
  namespace = var.namespace
  repository = "https://traefik.github.io/charts/"
  chart = "traefik"
  version = "38.0.2"
  values = [
    templatefile(
      "${path.module}/values.yaml.tpl",
      {
        replica_count     = var.replica_count
        enable_metrics    = var.enable_metrics
      }
    )
  ]
}