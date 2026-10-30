module "cluster" {
  source = "../../modules/kind-cluster"

  name         = var.cluster_name
  worker_count = var.worker_count
}

provider "helm" {
  kubernetes = {
    host                   = module.cluster.endpoint
    client_certificate     = module.cluster.client_certificate
    client_key             = module.cluster.client_key
    cluster_ca_certificate = module.cluster.cluster_ca_certificate
  }
}

module "argocd" {
  source = "../../modules/argocd-bootstrap"

  repo_url        = "https://github.com/${var.github_owner}/${var.github_repository}.git"
  target_revision = var.target_revision
  environment     = "local"
  image_registry  = lower("ghcr.io/${var.github_owner}/${var.github_repository}")
}

module "secrets" {
  source = "../../modules/bootstrap-secrets"
}
