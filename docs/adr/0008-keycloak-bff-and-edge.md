# 0008: Keycloak, a backend-for-frontend gateway, and one origin for everything

**Status:** Accepted

## Context
Borrowing needs real accounts, and librarians need more rights than members. Login for a browser
app raises three questions:
- **Where do the tokens live?** Any token stored in browser storage can be stolen by an XSS bug.
- **Who checks permissions?** Checking only at the edge trusts anything that reaches a service directly.
- **How do the browser and the pods agree on Keycloak's address?** Mismatched issuer URLs are the most common OIDC failure.

## Decision
- **Keycloak** (OIDC, free) is the identity provider. Realm `mediahub` is defined in git (`deploy/gitops/platform/keycloak/realm-mediahub.json`):
  - roles: `MEMBER`, and `LIBRARIAN`, which includes MEMBER
  - self-registration with MEMBER as the default role
  - brute-force lockout, 5-minute access tokens
  - mandatory TOTP for librarians
- **Backend-for-frontend (BFF):** `api-gateway` (Spring Cloud Gateway Server MVC + Spring Security OAuth2 client) is a *confidential* client using Authorization Code + PKCE.
  - Tokens stay in a server-side session in **Valkey**.
  - The browser holds only `MEDIAHUB_SESSION` (HttpOnly, SameSite=Lax).
  - CSRF uses the XSRF-TOKEN cookie and the `X-XSRF-TOKEN` header.
  - The gateway relays the access token to services and strips the cookie.
- **Zero trust:** every service validates the JWT itself (signature, `iss`, `aud=mediahub-api`, expiry) with the shared `libs/service-security` library, and enforces its own roles. The gateway lets `/api/**` through and the services answer 401/403.
- **One origin:** the browser only ever sees `http://localhost`, with **Envoy Gateway** (Kubernetes Gateway API) routing by path: `/auth` → Keycloak, `/api|/oauth2|/login|/logout` → api-gateway, `/` → web-ui.
  - Tokens' `iss` is the public URL; pods fetch keys and tokens over Keycloak's internal URL (`KC_HOSTNAME_BACKCHANNEL_DYNAMIC`).
  - The gateway's OAuth client registration is written out by hand with public and internal endpoints, instead of using discovery.

**Rejected:**
- **Tokens in the SPA** (a public client with PKCE in the browser). It's simpler, but every XSS becomes account takeover, and refresh tokens end up in JavaScript.
- **Authorization only at the gateway.** One less check per hop, but any route around the gateway (port-forward, a compromised pod) bypasses all security.
- **Spring Cloud 2026.0** (the train matching Boot 4.1). It's only at RC1. We use 2025.1.4 (Gateway 5.0.4) with the compatibility check off; the gateway's tests prove it works on Boot 4.1. Revisit when 2026.0 is GA (Dependabot will propose it).

## Consequences
- Login, logout and token refresh never touch JavaScript. The UI asks `GET /api/me` who is signed in, and `POST /logout` returns Keycloak's end-session URL.
- Each new service adds one route to `RoutesConfig` and its own `SecurityConfig` rules. The library does the rest.
- Keycloak runs in dev mode with its realm re-imported on start, so self-registered users don't survive a restart. Week 5 moves it to PostgreSQL.
- Plain HTTP on localhost. In week 10, TLS from cert-manager turns on `Secure` cookies.
