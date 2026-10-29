# Lab 01: Public and private keys

**Goal:** after this lab you can answer, without asking anyone:
*"Someone asked for our public key. Which key do they mean, how do I produce it, and how do I
prove it's the right one?"*

Time: about 3 hours. Needs: Docker, `openssl`, `ssh`/`ssh-keygen` (built into Linux, macOS and
Windows 10+), `gpg`, and for section 5 `pip install pyjwt cryptography`. Work in `labs/keys/out/`, which git ignores, so no key you create is ever committed.

```bash
cd labs/keys && mkdir -p out && cd out
```

---

## 0. The one idea everything else builds on

A key pair is two halves that are mathematically linked:

| | Private key | Public key |
|---|---|---|
| Who has it | Only you. Never sent, never emailed, never committed. | Anyone. Safe to publish. |
| **Signing** (proving who you are) | Creates the signature | Verifies the signature |
| **Encryption** (keeping data secret) | Decrypts | Encrypts |

Two consequences you'll use all the time:
- **"Send me your public key"** is always safe to answer. If someone asks for a *private* key, the answer is no, and you escalate.
- Whoever holds the private key **is** that identity. Losing it means rotating it. You don't just "change the password".

**Several different kinds of "public key" show up in real work.** Asking for the wrong one is
the classic mix-up:

| Purpose | Key type | File looks like | Who uses whose key |
|---|---|---|---|
| Log in to an SFTP/SSH server | SSH user key | `ssh-ed25519 AAAA... comment` (one line, `.pub`) | You send **your** public key; the server admin adds it to `authorized_keys` |
| Know you're talking to the real server | SSH **host** key | Shown as a fingerprint `SHA256:...`, stored in your `known_hosts` | The server's key; you check its fingerprint the first time you connect |
| Encrypt files for a partner | PGP/GPG key | `-----BEGIN PGP PUBLIC KEY BLOCK-----` | You encrypt with **their** public key; they decrypt with their private key |
| HTTPS / TLS | X.509 **certificate** (public key + identity + CA signature) | `-----BEGIN CERTIFICATE-----` | The server presents it; the client trusts it because a CA signed it |
| Verify login tokens (OAuth/OIDC) | JWK in a JWKS endpoint | JSON `{"kty":"RSA","n":"...","e":"AQAB"}` | The identity provider signs tokens; APIs verify them with its published public key |

> **Before you send or request any key, write down:** what is it for (login, encryption, TLS,
> token verification), which side owns it (us or them), what format they expect, and the
> fingerprint they should see. One clear sentence with those four facts prevents most confused
> email threads.

---

## 1. SSH keys and SFTP login

### 1.1 Create a key pair for a service account
```bash
ssh-keygen -t ed25519 -f partner_ed25519 -C "partner-feed@mediahub" -N ""
ls -l partner_ed25519*        # private key (600) and .pub (public, safe to share)
cat partner_ed25519.pub        # one line: type, key, comment
ssh-keygen -lf partner_ed25519.pub   # the fingerprint: what you read out to the other side to confirm
```
`-N ""` means no passphrase. That's fine for an unattended service account whose file permissions
protect the key. Use a passphrase for keys that belong to a person.

### 1.2 Start an SFTP server that trusts only that public key
```bash
cd .. && docker compose up -d && cd out
```
The server gets **only `partner_ed25519.pub`**. The private key never leaves your machine.

### 1.3 Log in, and meet the host key
```bash
sftp -i partner_ed25519 -P 2222 partner@localhost
```
The first connection asks: `The authenticity of host '[localhost]:2222' can't be established. ED25519 key fingerprint is SHA256:...`.
That fingerprint is the **server's host key**, a different key from yours. In real work you
compare it with the fingerprint the server owner sent you through another channel. Type `yes`,
then `put partner_ed25519.pub` and `ls`, then `bye`.

### 1.4 Break it on purpose, and read the error
| Do this | Error you'll see | What it really means |
|---|---|---|
| `ssh-keygen -t ed25519 -f wrong_key -N ""` then `sftp -i wrong_key -P 2222 partner@localhost` | `Permission denied (publickey)` | The server doesn't have the public half of the key you offered. Check the **fingerprint** on both sides before you suspect accounts or permissions. |
| `chmod 644 partner_ed25519` then connect | `UNPROTECTED PRIVATE KEY FILE!` and the key is ignored | The client refuses to use a private key that other users can read. `chmod 600` fixes it. |
| `(cd .. && docker compose down && docker compose up -d)`, then connect | `REMOTE HOST IDENTIFICATION HAS CHANGED!` | The **server's** host key changed. Either the server was rebuilt, which is legitimate, or someone is intercepting the connection. Confirm with the server owner **before** you run `ssh-keygen -R "[localhost]:2222"`. |
| Connect with `-v` | Shows `Offering public key: ...` and `Authentications that can continue: publickey` | `-v` (or `-vvv`) is the first thing to try on any SSH or SFTP problem. |

### 1.5 Unattended transfer, the way scripts do it
```bash
printf 'put partner_ed25519.pub upload.pub\nls -l\nbye\n' > batch.txt
sftp -i partner_ed25519 -P 2222 -b batch.txt partner@localhost; echo "exit code: $?"
```
In batch mode, a failing command makes `sftp` exit non-zero. A script that doesn't check `$?`
(or `%ERRORLEVEL%` in a .bat file) will report success while nothing was transferred. When a
scheduled transfer "succeeds" but no file arrives, read the script before you blame the account.
Week 7 builds on this with .sh, .ps1 and .bat versions.

---

## 2. Signing: prove who sent something and that it wasn't changed
```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out signer.key
openssl pkey -in signer.key -pubout -out signer.pub      # publish this
echo "Pay invoice 4711: 120.00 EUR" > message.txt
openssl dgst -sha256 -sign signer.key -out message.sig message.txt
openssl dgst -sha256 -verify signer.pub -signature message.sig message.txt   # Verified OK
echo "Pay invoice 4711: 9120.00 EUR" > message.txt
openssl dgst -sha256 -verify signer.pub -signature message.sig message.txt   # Verification failure
```
The signature is made with the *private* key and checked with the *public* key. Changing a single
character breaks it. Container image signing (cosign, used in our CI) and JWTs both rely on this.

---

## 3. Encryption: send a file only the partner can read (PGP)
Partners who exchange files over SFTP often also PGP-encrypt them. Here SFTP protects the
connection and PGP protects the file itself.
```bash
export GNUPGHOME=$PWD/gnupg && mkdir -m 700 -p "$GNUPGHOME"
gpg --batch --passphrase '' --quick-generate-key "Partner Library <partner@example.org>" ed25519 sign,cert 1y
FPR=$(gpg --list-keys --with-colons partner@example.org | awk -F: '/^fpr/{print $10; exit}')
gpg --batch --passphrase '' --quick-add-key "$FPR" cv25519 encr 1y
gpg --armor --export partner@example.org > partner-pgp-public.asc   # what the partner sends YOU
gpg --fingerprint partner@example.org                                # what you confirm by phone/chat

echo "overdue report" > report.csv
gpg --batch --yes --trust-model always --encrypt --recipient partner@example.org --output report.csv.gpg report.csv
gpg --batch --decrypt report.csv.gpg      # only works where the partner's PRIVATE key exists
```
Notice the direction: to send *them* a secret file, you need *their* public key. Your own key
is only used if you also sign the file.

---

## 4. TLS certificates: build your own certificate authority (CA)
A certificate is a public key plus an identity (a hostname), signed by a CA that the client
already trusts.
```bash
# 4.1 Root CA: its private key is the most sensitive file in the whole lab
openssl req -x509 -new -newkey ec -pkeyopt ec_paramgen_curve:P-256 -nodes \
  -keyout ca.key -out ca.crt -days 3650 -subj "/CN=MediaHub Lab Root CA"

# 4.2 Server: create a key and a certificate signing request (CSR) that includes the hostname
openssl req -new -newkey ec -pkeyopt ec_paramgen_curve:P-256 -nodes \
  -keyout server.key -out server.csr -subj "/CN=catalog.mediahub.local"
printf "subjectAltName=DNS:catalog.mediahub.local,DNS:localhost\nextendedKeyUsage=serverAuth\n" > server.ext

# 4.3 The CA signs the CSR. Only the CSR travels, never server.key
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out server.crt -days 30 -extfile server.ext
openssl x509 -in server.crt -noout -subject -issuer -dates -ext subjectAltName

# 4.4 Serve it and connect
openssl s_server -accept 8443 -cert server.crt -key server.key -www >/dev/null 2>&1 &
curl --cacert ca.crt https://localhost:8443/ -o /dev/null -w "%{http_code}\n"   # 200: chain trusted, name matches
```
Now break it and read each error:
```bash
curl https://localhost:8443/                         # "unable to get local issuer certificate": client doesn't trust our CA
curl --cacert ca.crt https://127.0.0.1:8443/          # "no alternative certificate subject name matches": wrong name
openssl s_client -connect localhost:8443 -CAfile ca.crt </dev/null 2>/dev/null | grep -E "s:|i:|Verify return"   # read the chain yourself
kill %1
```
`openssl s_client -connect host:443 -showcerts` is the tool for "is the certificate on this
server the one I expect, and is it about to expire?" It works on any HTTPS, LDAPS or FTPS endpoint.

---

## 5. JWT: the same idea inside login tokens
```bash
python3 - <<'PY'
import json, jwt
from cryptography.hazmat.primitives.asymmetric import rsa
key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
token = jwt.encode({"sub": "member-42", "roles": ["MEMBER"], "iss": "https://login.mediahub.local"},
                   key, algorithm="RS256", headers={"kid": "lab-key-1"})
print("token:", token[:60], "...")
print("header:", jwt.get_unverified_header(token))
print("verified claims:", jwt.decode(token, key.public_key(), algorithms=["RS256"],
                                      issuer="https://login.mediahub.local"))
other = rsa.generate_private_key(public_exponent=65537, key_size=2048)
try:
    jwt.decode(token, other.public_key(), algorithms=["RS256"])
except jwt.InvalidSignatureError as e:
    print("with the wrong public key:", e)
PY
```
In week 3, Keycloak holds the private key and publishes the public half at its JWKS URL. Every
service downloads it and checks tokens locally. The `kid` header tells a service which key to
use, and that's how keys get rotated without downtime.

---

## 6. Clean up
```bash
cd .. && docker compose down && rm -rf out
```

## 7. Write-up (required, it's the communication practice)
In `docs/labs/01-keys-notes.md`, in your own words:
1. The five "public key" types from the table in section 0, each with one sentence on when you'd use it.
2. For each error in sections 1.4 and 4: the error, its cause, and the first command you'd run.
3. A **five-sentence email** to an imaginary partner asking them to set up SFTP access for our
   service account. Use `docs/templates/stakeholder-update.md`. Include: purpose, which key and its
   format, the fingerprint, the host and username you expect, and one clear request.
