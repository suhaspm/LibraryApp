terraform {
  required_version = ">= 1.10"

  required_providers {
    kind = {
      source  = "tehcyx/kind"
      version = "~> 0.13"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 3.3"
    }
  }

  # Local state on purpose for the laptop cluster: it is disposable and holds a cluster key.
  # The OCI environment (week 10) uses a remote backend with locking.
  backend "local" {}
}
