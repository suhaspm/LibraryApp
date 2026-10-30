terraform {
  required_providers {
    helm = {
      source = "hashicorp/helm"
    }
    random = {
      source = "hashicorp/random"
    }
  }
}

# Generated once and kept in Terraform state (local and git-ignored for this environment).
# Nothing secret is ever written to git; the realm file and manifests only reference these by name.
resource "random_password" "keycloak_admin" {
  length  = 24
  special = false
}

resource "random_password" "bff_client" {
  length  = 40
  special = false
}

resource "random_password" "services_client" {
  length  = 40
  special = false
}

resource "random_password" "valkey" {
  length  = 32
  special = false
}

# Test users must satisfy the realm's password policy (12+ characters).
resource "random_password" "member" {
  length  = 16
  special = false
}

resource "random_password" "librarian" {
  length  = 16
  special = false
}

# A tiny local chart instead of kubernetes_secret resources: Helm's create_namespace tolerates
# the namespace already existing (Argo CD may have created it), so this works on a fresh
# cluster and on one that is already running.
resource "helm_release" "secrets" {
  name             = "bootstrap-secrets"
  chart            = "${path.module}/chart"
  namespace        = var.namespace
  create_namespace = true

  set_sensitive = [
    { name = "keycloakAdminPassword", value = random_password.keycloak_admin.result },
    { name = "bffClientSecret", value = random_password.bff_client.result },
    { name = "servicesClientSecret", value = random_password.services_client.result },
    { name = "valkeyPassword", value = random_password.valkey.result },
    { name = "memberPassword", value = random_password.member.result },
    { name = "librarianPassword", value = random_password.librarian.result },
  ]
}
