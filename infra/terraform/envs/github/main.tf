# Authenticate with a fine-grained personal access token limited to this one repo:
#   export GITHUB_TOKEN=github_pat_...   (Administration, Secrets, Contents: read/write)
provider "github" {
  owner = var.github_owner
}

# You create the repo by hand once, push the code, then let Terraform take it over.
# `import` adopts an existing resource into state instead of creating a new one.
import {
  to = module.repo.github_repository.this
  id = var.github_repository
}

module "repo" {
  source = "../../modules/github-repo"

  name = var.github_repository
}
