variable "name" {
  type = string
}

variable "description" {
  type    = string
  default = "Library platform built as a microservices learning lab"
}

variable "required_approvals" {
  type    = number
  default = 0
}

variable "required_checks" {
  description = "Status check names that must pass before merging to main."
  type        = list(string)
  default     = ["ci-result"]
}
