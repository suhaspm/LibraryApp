# 0006: Supply-chain hardening from day one

**Status:** Accepted

## Context
Several widely used GitHub Actions and container images have been compromised by moving a tag to
malicious code. In 2025 the Bitnami free image catalog was withdrawn, which broke many Helm
installs overnight. What we run should be verifiable and under our control.

## Decision
- Third-party GitHub Actions are pinned to **commit SHAs**, and Dependabot proposes updates.
- Images are built in CI, scanned (Grype via `anchore/scan-action`), shipped with an SBOM and provenance attestation, and **signed with cosign keyless** (identity = the GitHub workflow).
- No Bitnami images or charts. Use official upstream images (`mongo`, `postgres`) and operators.
- Runtime: non-root UID 10001, read-only root filesystem, all capabilities dropped, seccomp `RuntimeDefault`.
- Week 10 adds Kyverno to *enforce* "signed images only" in the cluster.

## Consequences
- Slightly slower CI and more YAML.
- Pinned SHAs look opaque, so each pin has a `# vX.Y.Z` comment.
