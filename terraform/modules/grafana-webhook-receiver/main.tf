locals {
  labels = {
    app                         = var.name
    "app.kubernetes.io/name"    = var.name
    "app.kubernetes.io/part-of" = "grafana-alert-demo"
  }
}

resource "kubernetes_config_map_v1" "receiver" {
  metadata {
    name      = "${var.name}-code"
    namespace = var.namespace

    labels = local.labels
  }

  data = {
    "receiver.py" = <<-PYTHON
      from http.server import BaseHTTPRequestHandler, HTTPServer
      import json

      class GrafanaWebhookHandler(BaseHTTPRequestHandler):

          def do_POST(self):
              try:
                  content_length = int(
                      self.headers.get("Content-Length", 0)
                  )

                  body = self.rfile.read(content_length)

                  print("=== GRAFANA WEBHOOK ===", flush=True)
                  print(
                      f"Method: {self.command}",
                      flush=True
                  )
                  print(
                      f"Path: {self.path}",
                      flush=True
                  )

                  try:
                      payload = json.loads(body)
                      print(
                          json.dumps(payload, indent=2),
                          flush=True
                      )
                  except Exception:
                      print(
                          body.decode(
                              "utf-8",
                              errors="replace"
                          ),
                          flush=True
                      )

                  self.send_response(200)
                  self.send_header(
                      "Content-Type",
                      "application/json"
                  )
                  self.end_headers()

                  response = b'{"status":"received"}'
                  self.wfile.write(response)

              except Exception as exc:
                  print(
                      f"Webhook processing error: {exc}",
                      flush=True
                  )

                  self.send_response(500)
                  self.end_headers()

          def do_GET(self):
              if self.path == "/health":

                  self.send_response(200)
                  self.send_header(
                      "Content-Type",
                      "application/json"
                  )
                  self.end_headers()

                  self.wfile.write(
                      b'{"status":"UP"}'
                  )

              else:
                  self.send_response(404)
                  self.end_headers()

          def log_message(self, format, *args):
              print(
                  format % args,
                  flush=True
              )


      server = HTTPServer(
          ("0.0.0.0", ${var.service_port}),
          GrafanaWebhookHandler
      )

      print(
          "Grafana webhook receiver listening on "
          ":${var.service_port}",
          flush=True
      )

      server.serve_forever()
    PYTHON
  }
}

resource "kubernetes_deployment_v1" "receiver" {
  metadata {
    name      = var.name
    namespace = var.namespace

    labels = local.labels
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = {
        app = var.name
      }
    }

    template {
      metadata {
        labels = {
          app = var.name
        }
      }

      spec {
        container {
          name  = "receiver"
          image = var.image

          command = [
            "python",
            "/app/receiver.py"
          ]

          port {
            name           = "http"
            container_port = var.service_port
          }

          readiness_probe {
            http_get {
              path = "/health"
              port = var.service_port
            }

            initial_delay_seconds = 2
            period_seconds        = 5
          }

          liveness_probe {
            http_get {
              path = "/health"
              port = var.service_port
            }

            initial_delay_seconds = 5
            period_seconds        = 10
          }

          volume_mount {
            name       = "receiver-code"
            mount_path = "/app"
            read_only  = true
          }

          resources {
            requests = {
              cpu    = "10m"
              memory = "32Mi"
            }

            limits = {
              cpu    = "100m"
              memory = "128Mi"
            }
          }
        }

        volume {
          name = "receiver-code"

          config_map {
            name = kubernetes_config_map_v1.receiver.metadata[0].name
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "receiver" {
  metadata {
    name      = var.name
    namespace = var.namespace

    labels = local.labels
  }

  spec {
    selector = {
      app = var.name
    }

    port {
      name        = "http"
      port        = var.service_port
      target_port = var.service_port
      protocol    = "TCP"
    }

    type = "ClusterIP"
  }
}