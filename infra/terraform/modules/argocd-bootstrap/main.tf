terraform {
  required_providers {
    helm = {
      source = "hashicorp/helm"
    }
  }
}

# Terraform's job ends here: install Argo CD and point it at the repo once.
# Everything inside the cluster after this is pulled from git (docs/adr/0004).
resource "helm_release" "argocd" {
  name             = "argocd"
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = var.argocd_chart_version
  namespace        = "argocd"
  create_namespace = true
  wait             = true
  timeout          = 600

  values = [yamlencode({
    configs = {
      params = {
        # TLS is terminated by the gateway in front of Argo CD later; plain HTTP inside the cluster for now.
        "server.insecure" = true
      }
    }
    # Not needed locally and each costs memory.
    dex           = { enabled = false }
    notifications = { enabled = false }
    controller = {
      resources = { requests = { cpu = "100m", memory = "256Mi" }, limits = { memory = "768Mi" } }
    }
    repoServer = {
      resources = { requests = { cpu = "50m", memory = "128Mi" }, limits = { memory = "512Mi" } }
    }
    server = {
      resources = { requests = { cpu = "50m", memory = "64Mi" }, limits = { memory = "256Mi" } }
    }
  })]
}

resource "helm_release" "root_app" {
  name       = "mediahub-root"
  repository = "https://argoproj.github.io/argo-helm"
  chart      = "argocd-apps"
  version    = var.argocd_apps_chart_version
  namespace  = "argocd"

  values = [yamlencode({
    applications = {
      "mediahub-root" = {
        namespace = "argocd"
        project   = "default"
        sources = [
          {
            repoURL        = var.repo_url
            targetRevision = var.target_revision
            path           = "deploy/gitops/root"
            helm = {
              valueFiles = ["$values/deploy/gitops/apps/${var.environment}/apps.yaml"]
              valuesObject = {
                repoURL        = var.repo_url
                targetRevision = var.target_revision
                environment    = var.environment
                imageRegistry  = var.image_registry
              }
            }
          },
          {
            repoURL        = var.repo_url
            targetRevision = var.target_revision
            ref            = "values"
          },
        ]
        destination = {
          server    = "https://kubernetes.default.svc"
          namespace = "argocd"
        }
        syncPolicy = {
          automated = { prune = true, selfHeal = true }
        }
      }
    }
  })]

  depends_on = [helm_release.argocd]
}
