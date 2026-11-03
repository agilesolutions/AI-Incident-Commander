resource "kubernetes_namespace_v1" "database" {
  metadata {
    name = var.namespace
  }
}

resource "helm_release" "postgresql" {
  name             = var.name
  namespace        = var.namespace
  create_namespace = true

  repository = "oci://registry-1.docker.io/bitnamicharts"
  chart      = "postgresql"
  version    = var.chart_version

  values = [
    templatefile("${path.module}/values.yaml", {
      postgres_database = var.database
      postgres_username = var.username
      postgres_password = var.password
      storage_size      = var.storage_size
    })
  ]
}