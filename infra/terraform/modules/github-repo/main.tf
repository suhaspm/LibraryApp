terraform {
  required_providers {
    github = {
      source = "integrations/github"
    }
    tls = {
      source = "hashicorp/tls"
    }
  }
}

resource "github_repository" "this" {
  name        = var.name
  description = var.description
  visibility  = "public" # public = unlimited free Actions minutes and free GHCR storage

  has_issues             = true
  has_projects           = false
  has_wiki               = false
  allow_merge_commit     = false
  allow_rebase_merge     = false
  allow_squash_merge     = true # one commit per PR keeps main readable
  delete_branch_on_merge = true

  lifecycle {
    prevent_destroy = true # the repo holds the code; never let `terraform destroy` take it
  }
}

resource "github_repository_vulnerability_alerts" "this" {
  repository = github_repository.this.name
  enabled    = true
}

# CI writes image tags back into deploy/gitops on main. It authenticates with an SSH deploy key
# (public half registered on the repo, private half stored as an Actions secret), and that key is
# the only actor allowed to bypass the "PRs only" rule. Generating the pair here is the week 1 keys
# lab in practice. Trade-off: the private key also sits in Terraform state, so state stays local.
resource "tls_private_key" "gitops" {
  algorithm = "ED25519"
}

resource "github_repository_deploy_key" "gitops" {
  repository = github_repository.this.name
  title      = "ci-gitops-writer"
  key        = tls_private_key.gitops.public_key_openssh
  read_only  = false
}

resource "github_actions_secret" "gitops_key" {
  repository  = github_repository.this.name
  secret_name = "GITOPS_DEPLOY_KEY"
  # The provider encrypts this with the repository's public key (libsodium sealed box) before
  # sending it: GitHub can decrypt it, nobody reading the API traffic can.
  value = tls_private_key.gitops.private_key_openssh
}

resource "github_repository_ruleset" "main" {
  name        = "protect-main"
  repository  = github_repository.this.name
  target      = "branch"
  enforcement = "active"

  conditions {
    ref_name {
      include = ["~DEFAULT_BRANCH"]
      exclude = []
    }
  }

  bypass_actors {
    actor_type  = "DeployKey"
    bypass_mode = "always"
  }

  rules {
    deletion         = true
    non_fast_forward = true

    pull_request {
      # Solo project: 0 approvals, but every change still goes through a PR and green CI.
      required_approving_review_count   = var.required_approvals
      dismiss_stale_reviews_on_push     = true
      require_last_push_approval        = false
      required_review_thread_resolution = true
    }

    required_status_checks {
      strict_required_status_checks_policy = false

      dynamic "required_check" {
        for_each = var.required_checks
        content {
          context = required_check.value
        }
      }
    }
  }

  depends_on = [github_repository_deploy_key.gitops]
}
