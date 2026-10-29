# Runbooks

One file per alert or known failure mode. Written so that you, at 3 a.m., without help, can go
from symptom to fix. Template:

```
# <Alert name / symptom>
## What it means
## Check (commands, in order, with expected output)
## Fix
## Escalate when (and to whom, with what information)
## Stakeholder message (link to docs/templates/stakeholder-update.md, pre-filled)
```

Week 11 adds incident drills that inject faults; each drill must be solved using only these files.
