variable "name" {
  description = "Cluster name; kubectl context becomes kind-<name>."
  type        = string
}

variable "node_image" {
  description = "kindest/node image (pins the Kubernetes version). Null uses the kind provider's default."
  type        = string
  default     = null
}

variable "worker_count" {
  description = "Extra worker nodes. 0 keeps everything on the control plane, which is enough for 16 GB laptops."
  type        = number
  default     = 0

  validation {
    condition     = var.worker_count >= 0 && var.worker_count <= 3
    error_message = "worker_count must be between 0 and 3."
  }
}

variable "http_port" {
  description = "Host port mapped to the cluster's HTTP NodePort."
  type        = number
  default     = 80
}

variable "https_port" {
  description = "Host port mapped to the cluster's HTTPS NodePort."
  type        = number
  default     = 443
}
