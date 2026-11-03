variable "github_owner" {
  description = "GitHub organization or user that owns the GitOps repository."
  type        = string
}

variable "github_repository" {
  description = "GitHub repository used for Flux GitOps."
  type        = string
}

variable "github_token" {
  description = "GitHub token used to manage the deploy key."
  type        = string
  sensitive   = true
}

variable "cluster_name" {
  description = "Logical name of the Kubernetes cluster."
  type        = string
}

variable "git_branch" {
  description = "Git branch Flux should reconcile."
  type        = string
  default     = "master"
}

variable "flux_path" {
  description = "Path in the Git repository containing the cluster configuration."
  type        = string
}

variable "flux_namespace" {
  description = "Namespace where Flux is installed."
  type        = string
  default     = "flux-system"
}

variable "flux_version" {
  description = "Flux version."
  type        = string
  default     = "v2.9.0"
}

variable "flux_interval" {
  description = "Flux reconciliation interval."
  type        = string
  default     = "1m0s"
}

variable "network_policy" {
  description = "Enable Flux network policies."
  type        = bool
  default     = true
}

variable "watch_all_namespaces" {
  description = "Allow Flux to watch resources in all namespaces."
  type        = bool
  default     = true
}

variable "create_repository" {
  description = "Create the GitHub repository with Terraform."
  type        = bool
  default     = false
}

variable "repository_visibility" {
  description = "GitHub repository visibility."
  type        = string
  default     = "private"

  validation {
    condition = contains(
      ["private", "public", "internal"],
      var.repository_visibility
    )

    error_message = "Repository visibility must be private, public, or internal."
  }
}

variable "repository_description" {
  description = "Description of the GitOps repository."
  type        = string
  default     = "GitOps repository managed by FluxCD."
}

variable "deploy_key_read_only" {
  description = "Whether the Flux GitHub deploy key should be read-only. Must be false for Image Automation."
  type        = bool
  default     = false
}

variable "flux_components_extra" {
  description = "Additional Flux controllers to install."
  type        = set(string)

  default = [
    "image-reflector-controller",
    "image-automation-controller"
  ]
}