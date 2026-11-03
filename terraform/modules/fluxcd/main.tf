# This Terraform module bootstraps FluxCD in a Kubernetes cluster and configures it to sync with a GitHub repository.
# The overall GitOps workflow
# 1. Terraform bootstraps FluxCD and enables Image Automation.
# 2. Gradle JIB plugin publishes a new container image.
# 3. Flux detects the image, updates the HelmRelease in Git, commits the change.
# 4. Flux then reconciles the updated HelmRelease into the STACKIT SKE cluster.


# generating an SSH key via your terminal (like the ssh-keygen), this resource generates the key programmatically during a terraform apply.
resource "tls_private_key" "flux" {
  algorithm   = "ECDSA"
  ecdsa_curve = "P384"
}

# Create a GitHub repository for FluxCD to sync with, if specified
resource "github_repository" "this" {
  count = var.create_repository ? 1 : 0
  name        = var.github_repository
  description = var.repository_description
  visibility = var.repository_visibility
  has_issues   = false
  has_projects = false
  has_wiki     = false

  lifecycle {
    prevent_destroy = true
  }
}

# Create a deploy key in the GitHub repository for FluxCD to use
resource "github_repository_deploy_key" "flux" {
  title      = "flux-cd-deploy-key"
  repository = var.github_repository
  key       = tls_private_key.flux.public_key_openssh
  read_only = var.deploy_key_read_only

  depends_on = [
    github_repository.this
  ]
}

# Bootstrap FluxCD in the Kubernetes cluster and configure it to sync with the GitHub repository. This native FluxCD workflow monitors your container registry, detects new image tags, updates your Git repository code automatically, and triggers a new Helm deployment.
# Requirement is that you have setup CI/CD hardend pipelines that validate test the quality of software before building and pushing images to the image repository.
# For Image Automation approach Flux Terraform bootstrap must install the Flux image automation controllers and configure the GitHub deploy key as read/write.
# 1. ImageRepository: Monitors your container registry (e.g., Docker Hub, ECR, GHCR) for new image tags.
# 2. ImagePolicy: Filters and selects the latest valid tag based on rules (e.g., SemVer or regex).
# 3. ImageUpdateAutomation: Automatically updates the Git repository with the new image tag, triggering a deployment in your cluster.
# 4. GitRepository: Represents the Git repository where your Kubernetes manifests are stored. It is used by Flux to fetch the desired state of your cluster.
# installs the following controllers:
# - source-controller
# - kustomize-controller
# - helm-controller
# - notification-controller
# - image-reflector-controller
# - image-automation-controller
/*
resource "flux_bootstrap_git" "this" {
  path = "fluxcd/flux-system"
  namespace = "flux-system"
  version = "v2.9.0"
  interval = var.flux_interval
  network_policy = var.network_policy
  watch_all_namespaces = var.watch_all_namespaces
  components_extra = var.flux_components_extra
  log_level = "debug"

  depends_on = [
    github_repository_deploy_key.flux
  ]
}
*/