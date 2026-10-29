terraform {
  required_version = ">= 1.10"

  required_providers {
    github = {
      source  = "integrations/github"
      version = "~> 6.13"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.4"
    }
  }

  backend "local" {}
}
