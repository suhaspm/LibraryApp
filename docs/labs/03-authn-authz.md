# Lab 03: Authentication, authorization, OAuth 2.0 and OpenID Connect

**Goal:** explain, and demonstrate on a running system:
- the difference between *authentication* (who are you?) and *authorization* (what may you do?)
- what is inside a token and who checks it
- why the browser never sees a token in MediaHub

Time: about 3 hours. Uses the docker-compose stack. Keycloak is also exposed directly on :8180 for these exercises.

```bash
scripts/dev-secrets.sh                                     # prints the test-user passwords
./gradlew :services:catalog-service:bootJar :services:api-gateway:bootJar
(cd services/web-ui && npm ci && npm run build)
docker compose -f docker-compose.dev.yml up --build
set -a; source .env; set +a                                # secrets into this shell for the curl commands
```

A small helper to read a token's payload (JWTs are base64url-encoded JSON: signed, **not** encrypted):
```bash
jwt() { cut -d. -f2 <<<"$1" | tr '_-' '/+' | base64 -d 2>/dev/null | python3 -m json.tool; }
```

---

## 1. The identity provider describes itself
```bash
curl -s http://localhost:8180/auth/realms/mediahub/.well-known/openid-configuration | python3 -m json.tool | head -20
```
Find `issuer`, `authorization_endpoint`, `token_endpoint` and `jwks_uri`. **Question:** you asked on
port 8180, so why does `issuer` say `http://localhost:3000/...`? (Read `docs/adr/0008`: tokens carry
the address the *browser* uses, while services fetch keys over the internal address.)

## 2. The public keys that make tokens verifiable
```bash
curl -s http://localhost:8180/auth/realms/mediahub/protocol/openid-connect/certs | python3 -m json.tool
```
Each key has a `kid`. Every token's header names the `kid` that signed it, which is how Keycloak can
rotate keys without breaking anyone. Connect this to Lab 01, section 5.

## 3. A machine-to-machine token (client credentials grant)
No user involved: a *service* proves who it is with its own secret.
```bash
TOKEN=$(curl -s -u "mediahub-services:$SERVICES_CLIENT_SECRET" -d grant_type=client_credentials \
  http://localhost:8180/auth/realms/mediahub/protocol/openid-connect/token | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")
jwt "$TOKEN"
```
Explain each of these claims in your notes: `iss`, `sub`, `aud` (why does it contain `mediahub-api`?), `azp`, `exp - iat` (why 300?), `realm_access`.

## 4. 401 versus 403
```bash
curl -i -X POST localhost:8080/api/v1/media -H 'Content-Type: application/json' -d '{}'                              # A
curl -s -o /dev/null -w '%{http_code}\n' -H "Authorization: Bearer $TOKEN" localhost:8080/api/v1/media                  # B
curl -i -X POST localhost:8080/api/v1/media -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"type":"BOOK","title":"x","creators":["y"]}'                                                                      # C
curl -s -o /dev/null -w '%{http_code}\n' -X POST -H "Authorization: Bearer ${TOKEN%?????}AAAAA" localhost:8080/api/v1/media # D
```
| | Expect | Why |
|---|---|---|
| A | 401 + `WWW-Authenticate: Bearer` | No identity at all |
| B | 200 | Reading the catalog is public |
| C | **403** | The token is genuine, but the service account doesn't have the `LIBRARIAN` role |
| D | 401 | A changed signature means a forged token. Rejected before any rule runs. |

Then find the code that makes each decision: `libs/service-security` (authentication) and
`services/catalog-service/.../SecurityConfig.java` (authorization).

## 5. The BFF: log in through the browser, then look for tokens
1. Open http://localhost:3000 and sign in as `member1`.
2. DevTools → Application → Cookies. You'll find `MEDIAHUB_SESSION` (**HttpOnly**: JavaScript can't read it) and `XSRF-TOKEN`.
3. Look in Local storage and Session storage: **no tokens anywhere.** The access token lives in Valkey, attached to that session:
   ```bash
   docker compose -f docker-compose.dev.yml exec valkey valkey-cli -a "$VALKEY_PASSWORD" --no-auth-warning keys 'mediahub:session:*'
   ```
4. DevTools → Network → reload. `GET /api/me` is how the UI learns your name and roles.
5. In the console: `await fetch('/api/v1/media', {method: 'POST'})` gets **403** from the *gateway*, because the CSRF header is missing. Compare it with the 403 a member gets *from catalog-service* when the header is present (the UI's "Librarians only" path). Same status, different layer: read the response bodies.

## 6. The login handshake, step by step
DevTools → Network → "Preserve log", then sign out and sign in again. Find:
1. `GET /oauth2/authorization/keycloak` → 302 to Keycloak with `code_challenge` (**PKCE**) and `state`
2. Keycloak's login form, `POST` credentials
3. 302 back to `/login/oauth2/code/keycloak?code=...&state=...`
4. The gateway exchanges the code for tokens *server-to-server*. You will **not** see this in the browser, and that's the point.
5. 302 to `/`, with a new `MEDIAHUB_SESSION` cookie (new session id after login: protection against session fixation)

## 7. Token expiry and refresh
Access tokens live 5 minutes. Stay signed in, wait 6 minutes, then add or browse media: it still works.
The gateway used the refresh token to get a new access token. Check *Sessions* for `member1` in the admin console
(http://localhost:3000/auth/admin, user `admin`, password `KEYCLOAK_ADMIN_PASSWORD` in `.env`).
**Question:** why short access tokens plus refresh, rather than one token valid for a day?

## 8. Brute-force protection
Sign out, then enter a wrong password for `member1` 5 times. The account is temporarily locked: even
the right password fails for a minute. Find the lock in the admin console (Users → member1) and the
`bruteForceProtected` settings in the realm file.

## 9. Two-factor authentication
Sign in as `librarian1`. Keycloak requires an authenticator app first: use any TOTP app on your phone
(Google/Microsoft Authenticator, FreeOTP) and scan the QR code. Sign out and in again to see the
one-time code prompt. Connect it to Lab 01: the QR code contains a **shared secret** (symmetric), not a key pair.

## 10. Log out everywhere
Sign in as `member1` in two browsers (or one normal plus one private window). In one, open the
account console at http://localhost:3000/auth/realms/mediahub/account → Device activity → "Sign out
all devices". Reload the other: MediaHub still shows you signed in until its session needs Keycloak
again. Find out when that is, and how a shorter `ssoSessionIdleTimeout` changes it.

## 11. Write-up (required)
In `docs/labs/03-authn-authz-notes.md`:
1. Authentication vs authorization, in two sentences, using a MediaHub example of each.
2. Your annotated token from section 3.
3. The table from section 4 in your own words, plus where in the code each decision is made.
4. A **five-sentence stakeholder note** (use `docs/templates/stakeholder-update.md`) explaining to a
   non-technical product owner why the web app never stores login tokens in the browser.
