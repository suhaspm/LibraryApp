# Stakeholder message template

Use for any message to people outside the team: incident updates, requests to a partner,
questions to another department. **Don't send until every checklist box is ticked.**

## Checklist before sending
- [ ] I reproduced or checked the problem myself, and I can say what I *know* versus what I *suspect*.
- [ ] I checked logs, config and recent changes, and I searched the docs and runbooks.
- [ ] The message has **one** clear ask, and it's obvious who must do what by when.
- [ ] Any key, certificate or file I mention is named precisely: type, purpose, fingerprint or filename.
- [ ] Someone with no context could act on it without replying "what do you mean?".
- [ ] If unsure: a senior proofread it.

## Template
**Subject:** [Action needed | Info | Resolved] <system>: <one-line problem or request>

**Context:** one or two sentences: what system, what happened or what we're doing.

**Impact:** who or what is affected, and since when. "None yet" is a valid answer.

**What we know:** facts only, with evidence (log line, error message, timestamp).

**What we need from you:** ONE request. Be specific: *"Please add the SSH public key below
(ED25519, fingerprint SHA256:abc...) to user `svc-feed` on sftp.example.org"*.

**Next steps / ETA:** what we'll do next and when you'll hear from us.

## Example: unclear versus clear
> **Unclear:** "Hi, can you send us the public key for the FTP? Our transfer isn't working. Thanks"
>
> **Clear:** "Subject: [Action needed] Partner feed SFTP: please confirm your host key fingerprint
> Our nightly upload to sftp.partner.example has failed since 02:00 with *Host key verification
> failed*. The server's host key has changed since yesterday. Before we trust the new key,
> please confirm that the ED25519 host key fingerprint of sftp.partner.example:22 is
> SHA256:Xyz... (reply or call). No files have been sent to the new key."
