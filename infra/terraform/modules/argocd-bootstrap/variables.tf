variable "repo_url" {
  description = "HTTPS URL of the public GitHub repo Argo CD pulls from."
  type        = string
}

variable "target_revision" {
  description = "Branch, tag or commit Argo CD tracks."
  type        = string
  default     = "main"
}

variable "environment" {
  description = "Selects deploy/gitops/apps/<environment>."
  type        = string
}

variable "image_registry" {
  description = "Registry prefix for service images, e.g. ghcr.io/<owner>/mediahub (lowercase)."
  type        = string

  validation {
    condition     = lower(var.image_registry) == var.image_registry
    error_message = "Container registry paths must be lowercase."
  }
}

variable "argocd_chart_version" {
  type    = string
  default = "10.11.6"
}

variable "argocd_apps_chart_version" {
  type    = string
  default = "2.0.7"
}
