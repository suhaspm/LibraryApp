variable "github_owner" {
  description = "GitHub user or organisation that owns the repo."
  type        = string
}

variable "github_repository" {
  description = "Repository name."
  type        = string
  default     = "mediahub"
}

variable "target_revision" {
  description = "Branch Argo CD tracks. Point it at a feature branch to test GitOps changes before merging."
  type        = string
  default     = "main"
}

variable "cluster_name" {
  type    = string
  default = "mediahub"
}

variable "worker_count" {
  type    = number
  default = 0
}
