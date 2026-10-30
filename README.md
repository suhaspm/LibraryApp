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
| 2+ | Web UI (React) for the catalog, pulled forward from week 12 | Done |
| 3 | Login: Keycloak (OIDC, MFA), backend-for-frontend gateway, Envoy Gateway on http://localhost, 401 vs 403 | Done: do [Lab 03](docs/labs/03-authn-authz.md) |
| 3+ | See [docs/pdp.md](docs/pdp.md) | Planned |

## Repository layout
```
services/<name>-service/     Spring Boot services (auto-discovered by Gradle and CI)
services/api-gateway/        backend-for-frontend: login with Keycloak, session in Valkey, relays tokens (docs/adr/0008)
services/web-ui/             React UI, served by nginx, which also proxies /api (docs/adr/0007)
libs/service-security/       shared library: every service validates tokens itself (401/403)
build-logic/                 Gradle convention plugin shared by all services
templates/spring-service/    golden-path template, used by scripts/new-service.sh
deploy/helm/microservice/    one Helm chart for every service
deploy/gitops/               Argo CD app-of-apps and per-environment values (CI edits image tags here)
infra/terraform/             modules + envs/local (kind + Argo CD) and envs/github (repo governance)
labs/                        hands-on labs (keys, later: file transfer, VM servers, IIS)
docs/                        ADRs, labs, runbooks, incident write-ups, PDP, message template
```

## Quick start

### 1. Run everything locally (needs Java 21, Node 22 and Docker)
```bash
./gradlew build                                       # unit + Testcontainers integration tests
scripts/dev-secrets.sh                                # once: random passwords into .env (git-ignored)
./gradlew :services:catalog-service:bootJar :services:api-gateway:bootJar
(cd services/web-ui && npm ci && npm run build)
docker compose -f docker-compose.dev.yml up --build
```
- App: http://localhost:3000. Sign in as `member1` or `librarian1`; `scripts/dev-secrets.sh` prints the passwords. Librarians must set up an authenticator app.
- Keycloak admin console: http://localhost:3000/auth/admin (user `admin`, password in `.env`)
- API docs (catalog-service directly): http://localhost:8080/swagger-ui.html

Working on the UI? Run `npm run dev` in `services/web-ui` for instant reloads on http://localhost:3000.
It forwards `/api` to `localhost:8080`, which can be the compose catalog-service or a `kubectl port-forward`.

### 2. Put it on GitHub (once)
The code lives at https://github.com/suhaspm/LibraryApp (the project itself is called MediaHub).
```bash
git clone https://github.com/suhaspm/LibraryApp.git && cd LibraryApp
```
Then let Terraform take over the repo's settings: branch ruleset, the deploy key CI uses to write
GitOps commits, and vulnerability alerts. Use a fine-grained token scoped to just this repo with
Administration, Secrets and Contents read/write:
```bash
cd infra/terraform/envs/github
cp terraform.tfvars.example terraform.tfvars
export GITHUB_TOKEN=github_pat_...
terraform init && terraform apply
```
Images are pushed to GHCR, where packages of a public repo are public. If a new service's pod
shows `ImagePullBackOff`, check `Packages → <service> → Package settings → visibility`.

### 3. Create the local cluster with Argo CD (needs Docker with ~10 GB RAM; no kind CLI needed, Terraform drives it)
```bash
cd infra/terraform/envs/local
cp terraform.tfvars.example terraform.tfvars
terraform init && terraform apply
kubectl --context kind-mediahub get applications -n argocd
```
Then open **http://localhost**. Envoy Gateway routes everything; no port-forwards needed. Sign in as:
```bash
terraform output -raw member_password      # member1
terraform output -raw librarian_password   # librarian1 (sets up an authenticator app on first login)
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
