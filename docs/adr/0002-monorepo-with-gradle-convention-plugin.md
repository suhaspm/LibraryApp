# 0002: Monorepo with a Gradle convention plugin

**Status:** Accepted

## Context
We'll have 8+ services, shared event contracts, one Helm chart, Terraform and GitOps config.
One developer, free CI minutes, and a strong need to change cross-cutting things (Java version,
test setup, chart) in one place.

## Decision
- One repository. Each service lives in `services/<name>` and is auto-included by `settings.gradle.kts`.
- Shared build logic lives in `build-logic/` as the `mediahub.spring-service` convention plugin, not in copy-pasted build files.
- Versions are kept in `gradle/libs.versions.toml`.
- CI computes which services changed and builds only those.

**Rejected:** polyrepo (one repo per service). It's closer to how large organisations scale
team ownership, but for one person it multiplies CI, versioning and contract-sync work with no
benefit. We can split later because service boundaries are already enforced: no service
depends on another's code.

## Consequences
- Atomic changes across a service and its deployment config in one PR.
- Risk of accidental coupling through shared libraries. Rule: `libs/` holds contracts and plumbing only, never domain logic.
- Change detection in CI must be maintained (see `.github/workflows/ci.yml`).
