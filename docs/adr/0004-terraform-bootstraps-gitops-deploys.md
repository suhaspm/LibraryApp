# 0004: Terraform bootstraps, Argo CD deploys

**Status:** Accepted

## Context
Both Terraform and Argo CD can install things into Kubernetes. If both manage the same objects
they fight. CI also needs a way to deploy that doesn't require network access from GitHub into
a cluster running on a laptop.

## Decision
| Tool | Owns |
|---|---|
| Terraform | Things **outside** the cluster, or needed before GitOps exists: the cluster itself (kind, later OCI VMs + k3s), cloud networking, the GitHub repo settings, and installing Argo CD plus one root Application |
| Argo CD | Everything **inside** the cluster after that: platform components and services, defined in `deploy/gitops` |
| CI | Builds, tests, signs and publishes images, then commits the new tag to `deploy/gitops`. It never runs `kubectl` or `helm`. |

The flow is pull-based: Argo CD polls the public repo, so the cluster needs only outbound access.

## Consequences
- Rollback = `git revert`. Audit trail = `git log deploy/gitops`.
- `selfHeal` reverts manual `kubectl edit`s. That's good discipline, and a surprise the first time.
- Bootstrapping order matters: the Argo CD CRDs must exist before the root Application. This is handled by `depends_on` in the module.
- Two Terraform states (`envs/github`, `envs/local`) because repo governance outlives any cluster.
