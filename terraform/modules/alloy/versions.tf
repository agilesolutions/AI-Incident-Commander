terraform {

  required_version = ">= 1.9"

  required_providers {

    kubernetes = {
      source = "hashicorp/kubernetes"
      version = "~> 3.2"
    }

    helm = {
      source = "hashicorp/helm"
      version = "~> 2.14"
    }

    kubectl = {
      source  = "gavinbunney/kubectl"
      version = "~> 1.14.0"
    }

  }
}