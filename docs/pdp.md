# Personal development plan: 12 weeks (3 Nov 2026 – 30 Jan 2027, with the week of 29 Dec off)

Objectives are written so they can be copied into a learning-objectives tool as-is: each has a
measurable outcome and evidence in this repo. Organisation-specific systems are mapped to these
generic concepts privately, not in this public repo.

## Objectives
| # | Objective | Evidence of done | Weeks |
|---|---|---|---|
| 1 | **Security fundamentals:** explain and operate public/private keys, certificates, authentication vs authorization | Lab 01 completed with write-up; own CA issuing certs; 401/403 demo; JWT verified via JWKS | 1, 3 |
| 2 | **Enterprise identity:** OAuth2/OIDC, SSO (web + mobile), SAML, LDAP, IAM admin APIs | Keycloak realm federated from OpenLDAP; SSO across two clients; account-service provisioning users via the admin API | 3–4 |
| 3 | **Protocols:** HTTP/1.1, HTTP/2, HTTPS, gRPC, SFTP, FTP/FTPS: when to use each and how to debug them | Protocol lab with packet captures; gRPC between two services; SFTP feed with .sh/.ps1/.bat clients | 6–7 |
| 4 | **Web and application servers:** IIS, Apache, Tomcat: install, configure virtual directories, find and read logs | Ansible-provisioned Tomcat + Apache; IIS on Windows Server; one cheat sheet per server | 8–9 |
| 5 | **Cloud and deployment choices:** EC2 vs containers vs Kubernetes, Windows vs Linux | Same app deployed three ways via Terraform; decision ADR with a cost and ops comparison | 2, 9–10 |
| 6 | **Infrastructure as code and networking:** Terraform, VCN/subnets/security lists, TLS, network policies | Oracle Cloud environment from `terraform apply`; default-deny NetworkPolicies | 2, 10 |
| 7 | **Independent run-mode support** | 6+ incident drills solved using only runbooks, without asking a colleague; runbook per alert | 11 |
| 8 | **Clear stakeholder communication** | Every lab and drill ends with a message that passes `docs/templates/stakeholder-update.md`; at least 3 proofread by a senior | all |

## Weekly rhythm
- **Build** (most of the week): the week's deliverable, deployed.
- **Explain** (Friday): an ADR or lab notes in your own words, plus a five-sentence "explain it to a stakeholder" note.
- **Self-check:** before asking a colleague anything, write down what you tried and what you found. Ask only after 30 minutes of honest research, and include that write-up in the question.

## Schedule
| Week | Starting | Focus |
|---|---|---|
| 1 | 3 Nov | Keys lab · catalog-service · Docker (AWS training part 2 also this week) |
| 2 | 10 Nov | Walking skeleton: CI → GHCR → Terraform kind + GitHub → Argo CD |
| 3 | 17 Nov | Keycloak, OAuth2/OIDC login, gateway, roles, 401 vs 403 — **built early (30 Oct); do Lab 03** |
| 4 | 24 Nov | LDAP federation, SAML, mobile SSO, IAM admin API |
| 5 | 1 Dec | Kafka, inventory, rentals in arrival order, due dates |
| 6 | 8 Dec | gRPC, HTTP/2, protocol lab |
| 7 | 15 Dec | SFTP/FTP partner feed, scripts, billing and payments due |
| 8 | 22 Dec | Tomcat and Apache on an Ansible-provisioned VM (holiday week: light) |
| 9 | 5 Jan | AWS: Linux EC2 vs container vs Windows Server + IIS |
| 10 | 12 Jan | Oracle Cloud networking, k3s, TLS, network policies |
| 11 | 19 Jan | Observability, alerts, incident drills |
| 12 | 26 Jan | Purchase saga, Go notification service, retrospective, update objectives |

## Monthly check-in with the manager (suggested)
Bring: objectives done or not done with links to evidence, one thing that was hard, and one thing
you now handle alone that you previously escalated.
