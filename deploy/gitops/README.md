# GitOps layout

Argo CD is the only thing that deploys into the cluster. Git is the source of truth:
to change what runs, change a file here and merge.

```
root/                 "app of apps" Helm chart: renders one Argo CD Application per entry
apps/<env>/apps.yaml  which applications exist in an environment
apps/<env>/values/    per-service Helm values; CI only ever edits image.tag in these
platform/             plain manifests for shared infrastructure (MongoDB now; Kafka, Postgres, Keycloak later)
```

Terraform installs Argo CD and creates the single root Application (see
`infra/terraform/modules/argocd-bootstrap`). Everything after that is pulled from git.

**Adding a service:** add an entry to `apps/<env>/apps.yaml` and a
`apps/<env>/values/<service>.yaml`. No Terraform change needed.
