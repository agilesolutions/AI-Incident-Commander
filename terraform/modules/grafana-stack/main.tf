resource "kubernetes_namespace_v1" "monitoring" {
  metadata {
    name = var.namespace
  }
}


resource "helm_release" "cert_manager" {
  name       = "cert-manager"
  repository = "https://charts.jetstack.io"
  chart      = "cert-manager"
  version    = var.cert_manager_version

  namespace        = var.namespace
  create_namespace = false

  # Install Certificate, Issuer, ClusterIssuer, etc. CRDs
  set {
    name  = "crds.enabled"
    value = "true"
  }

  wait    = true
  timeout = 600

  depends_on = [
    kubernetes_namespace_v1.monitoring
  ]
}


resource "helm_release" "opentelemetry_operator" {
  name             = "opentelemetry-operator"
  repository       = "https://open-telemetry.github.io/opentelemetry-helm-charts"
  chart            = "opentelemetry-operator"
  version          = var.opentelemetry_operator_version
  namespace        = var.namespace
  create_namespace = false

  set {
    name  = "crds.create"
    value = "true"
  }
  set {
    name  = "manager.collectorImage.repository"
    value = "ghcr.io/open-telemetry/opentelemetry-collector-releases/opentelemetry-collector-k8s"
  }

  depends_on = [
    kubernetes_namespace_v1.monitoring, helm_release.cert_manager
  ]
}


resource "helm_release" "lgtm_stack" {
  name       = "lgtm"
  repository = "https://promptlylabs.github.io/lgtm-helm-chart"
  chart      = "lgtm"
  version    = var.chart_version
  namespace  = var.namespace

  create_namespace = var.create_namespace

  disable_openapi_validation = true


  values = [
    yamlencode({

      # The operator is managed separately above.
      opentelemetry-operator = {
        enabled = false
      }

      # CORRECT UMBRELLA SPECS: Keep this clean. The chart will build the
      # cluster collector pipelines automatically.
      collectors = {
        enabled = true
      }

      grafana = {
        adminPassword = var.grafana_admin_password

        persistence = {
          enabled = var.persistence_enabled
          size    = "2Gi"
        }

      }

      kube-prometheus-stack = {
        prometheus = {
          prometheusSpec = {
            storageSpec = var.persistence_enabled ? {
              volumeClaimTemplate = {
                spec = {
                  accessModes = ["ReadWriteOnce"]

                  resources = {
                    requests = {
                      storage = var.persistence_size
                    }
                  }
                }
              }
            } : null
          }
        }
      }

      loki = {
        persistence = {
          enabled = var.persistence_enabled
          size    = var.persistence_size
        }
      }

      # Tempo tracing sub-chart routing
      tempo = {
        persistence = {
          enabled = var.persistence_enabled
          size    = var.persistence_size
        }

        metricsGenerator = {
          enabled = true

          config = {
            storage = {
              path = "/var/tempo/generator/wal"

              remote_write = [
                {
                  url            = "${var.prometheus_service_url}/api/v1/write"
                  send_exemplars = true
                }
              ]
            }

            processor = {
              service_graphs = {
                wait       = "10s"
                max_items  = 10000
                workers    = 10
                dimensions = []
              }
            }
          }
        }

        overrides = {
          defaults = {
            metrics_generator = {
              processors = [
                "service-graphs"
              ]
            }
          }
        }
      }



    }),

    yamlencode(var.custom_values)
  ]

  # Forces Terraform to fully create the operator before starting this resource
  depends_on = [
    helm_release.opentelemetry_operator
  ]
}

resource "local_file" "lgtm_otel_metrics_patch" {
  filename = "${path.module}/.generated/lgtm-otel-metrics-patch.yaml"

  content = yamlencode({
    spec = {
      config = {
        service = {
          pipelines = {
            metrics = {
              receivers = [
                "otlp",
                "hostmetrics",
                "prometheus",
                "kubeletstats"
              ]
            }
          }
        }
      }
    }
  })
}

resource "null_resource" "patch_lgtm_otlp_metrics_pipeline" {
  depends_on = [
    helm_release.lgtm_stack
  ]

  triggers = {
    lgtm_revision = helm_release.lgtm_stack.metadata[0].revision
    patch_file    = filesha256("${path.module}/otel-metrics-patch.yaml")
  }

  provisioner "local-exec" {
    interpreter = [
      "PowerShell",
      "-Command"
    ]

    command = <<-EOT
      kubectl patch opentelemetrycollector otel-node-collector `
        -n ${var.namespace} `
        --type=merge `
        --patch-file="${path.module}/otel-metrics-patch.yaml"
    EOT
  }
}

resource "kubernetes_config_map_v1" "grafana_alerting" {
  metadata {
    name      = "grafana-alerting-provisioning"
    namespace = var.namespace
  }

  data = {
    "contact-points.yaml" = <<-YAML
      apiVersion: 1

      contactPoints:
        - orgId: 1
          name: local-webhook

          receivers:
            - uid: local-webhook
              type: webhook

              settings:
                url: http://grafana-webhook.monitoring.svc.cluster.local:8080/webhook
                httpMethod: POST
    YAML

    "policies.yaml" = <<-YAML
      apiVersion: 1

      policies:
        - orgId: 1
          receiver: local-webhook

          group_by:
            - alertname
            - namespace
    YAML
  }
}