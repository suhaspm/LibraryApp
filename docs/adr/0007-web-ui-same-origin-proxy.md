# 0007: Web UI served by nginx, which also proxies the API

**Status:** Accepted

## Context
The UI was pulled forward from week 12 so the app can be used in a browser. A browser app has to
reach backend services, and three questions follow:
- Where does it run?
- How does it reach the APIs without opening them to cross-origin calls?
- How will login work later?

## Decision
- `services/web-ui` is a React single-page app (Vite, TypeScript, TanStack Query for server state, React Router).
- It ships as a container: `nginx-unprivileged` serves the static build and **proxies `/api/` to the backend**. The browser only ever talks to one origin, so:
  - No CORS configuration in any service. CORS headers are easy to get wrong and too permissive.
  - Cookies just work, which matters for week 3.
- It follows the same pipeline as every other service: CI builds `dist/` and the Dockerfile only copies it. It uses the same Helm chart, GitOps values and image signing.
- The container meets the same runtime constraints: non-root UID 10001 and a read-only root filesystem. nginx writes its rendered config, pid file and temp files under `/tmp`.
- Security headers are set in nginx: a Content-Security-Policy with no inline scripts, `nosniff`, and Referrer-Policy. `add_header_inherit merge` stops a `location` block from silently dropping them.

**Rejected:** calling services directly from the browser with CORS. That exposes every
service's URL, needs per-service CORS config, and makes cookie-based sessions awkward.

## Consequences
- **Week 3:** `/api/` points at the API gateway instead of catalog-service. Login uses the
  backend-for-frontend (BFF) pattern: the OAuth tokens stay server-side and the browser only holds an HttpOnly session cookie.
- **Type drift:** API types in `src/api/types.ts` are written by hand. Week 3 generates them from the services' OpenAPI documents.
- **Week 11:** no server-side rendering means nothing to monitor beyond nginx access logs. Front-end error reporting is a candidate for observability work.
