variable "namespace" {
  type = string
  default = "traefik"
}

variable "replica_count" {
  type = number
  default = 2
}

variable "enable_metrics" {
  type = bool
  default = true
}