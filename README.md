# MediaHub

A library platform, in the spirit of the Hamburg public library app, built as microservices. It
is also a 12-week, hands-on curriculum in architecture, security, networking and operations
([docs/pdp.md](docs/pdp.md)).

Members browse the catalog, rent media in order of request, see due dates, pay what they owe and
buy media. Everything runs on free tiers.

## Architecture at a glance
```
 Web / mobile clients ──► Gateway (TLS, JWT) ──► catalog · inventory · rental · billing · purchase · account · partner-feed · notification
                                                   │  REST outside, gRPC inside, Kafka for events (ordered per media item)
 Keycloak (OIDC/SAML) ◄─ OpenLDAP        MongoDB (catalog) · PostgreSQL (transactions) · Valkey (cache)
 GitHub Actions ─► GHCR (signed, multi-arch) ─► git commit ─► Argo CD ─► Kubernetes (kind locally, k3s on Oracle Cloud)
 Terraform: clusters, cloud networking, GitHub repo settings · Ansible: VM configuration
```
Decisions and their trade-offs: [docs/adr](docs/adr/README.md).

## Status
| Week | Deliverable | State |
|---|---|---|
| 1 | Monorepo, catalog-service (MongoDB, Testcontainers), Dockerfile, compose, keys lab | Done |
| 2 | Helm chart, GitOps app-of-apps, CI pipeline, Terraform (kind, Argo CD, GitHub) | Done: needs your repo to run end to end |
| 3+ | See [docs/pdp.md](docs/pdp.md) | Planned |

## Repository layout
```
services/<name>-service/     Spring Boot services (auto-discovered by Gradle and CI)
build-logic/                 Gradle convention plugin shared by all services
templates/spring-service/    golden-path template, used by scripts/new-service.sh
deploy/helm/microservice/    one Helm chart for every service
deploy/gitops/               Argo CD app-of-apps and per-environment values (CI edits image tags here)
infra/terraform/             modules + envs/local (kind + Argo CD) and envs/github (repo governance)
labs/                        hands-on labs (keys, later: file transfer, VM servers, IIS)
docs/                        ADRs, labs, runbooks, incident write-ups, PDP, message template
```

## Quick start

### 1. Run the catalog locally (needs Java 21 and Docker)
```bash
./gradlew build                                       # unit + Testcontainers integration tests
./gradlew :services:catalog-service:bootJar
docker compose -f docker-compose.dev.yml up --build
curl "localhost:8080/api/v1/media?q=mann"             # Swagger UI: http://localhost:8080/swagger-ui.html
```

### 2. Put it on GitHub (once)
Create an **empty public** repo named `mediahub` on GitHub, then:
```bash
git remote add origin git@github.com:<you>/mediahub.git
git push -u origin main
```
Then let Terraform take over the repo's settings: branch ruleset, the deploy key CI uses to write
GitOps commits, and vulnerability alerts. Use a fine-grained token scoped to just this repo with
Administration, Secrets and Contents read/write:
```bash
cd infra/terraform/envs/github
cp terraform.tfvars.example terraform.tfvars          # set github_owner
export GITHUB_TOKEN=github_pat_...
terraform init && terraform apply
```
After the first green CI run on `main`, open the package page on GitHub
(`Packages → catalog-service → Package settings`) and set its visibility to **public**, so the
cluster can pull it without credentials.

### 3. Create the local cluster with Argo CD (needs Docker with ~10 GB RAM; no kind CLI needed, Terraform drives it)
```bash
cd infra/terraform/envs/local
cp terraform.tfvars.example terraform.tfvars          # set github_owner
terraform init && terraform apply
kubectl --context kind-mediahub get applications -n argocd
kubectl --context kind-mediahub -n mediahub port-forward svc/catalog-service 8080:80
```
Argo CD UI: `terraform output argocd_ui` prints how to open it.

### 4. Add a service
```bash
scripts/new-service.sh rental-service
```

## Conventions
- Every change goes through a PR. The `ci-result` check must be green.
- Images are tagged with the short git SHA, signed with cosign, and never rebuilt for another environment.
- Nothing organisation-specific or secret is committed. Lab keys are generated under `labs/**/out/`, which git ignores.
