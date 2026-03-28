---
name: Quebec Law 25 Compliance Tracker
description: Gap analysis of ApexAI Athletics against Quebec Law 25 (Loi 25 / Bill 64) requirements as of 2026-03-28 audit
type: project
---

**Why:** App targets Quebec residents and collects sensitive health and biometric data. Law 25 non-compliance can result in penalties up to 4% of worldwide turnover or $25M CAD.

**How to apply:** Review this tracker at each audit session and update remediation status.

## Compliance Gaps (as of 2026-03-28)

| Requirement | Status | Finding ID |
|---|---|---|
| Privacy Policy published | NOT FOUND | LAW25-01 |
| Explicit informed consent at registration | MISSING | LAW25-02 |
| Separate consent for health data | MISSING | LAW25-03 |
| Separate consent for video/AI analysis | MISSING | LAW25-04 |
| Privacy Impact Assessment (PIA) documented | NOT FOUND | LAW25-05 |
| Privacy Officer appointed | NOT FOUND | LAW25-06 |
| Cross-border transfer safeguards (Gemini/Supabase) | MISSING | LAW25-07 |
| Data retention and destruction policy | MISSING | LAW25-08 |
| Right to erasure / right to be forgotten mechanism | MISSING | LAW25-09 |
| Right to data portability mechanism | MISSING | LAW25-10 |
| Breach notification readiness (72h to CAI) | UNKNOWN | LAW25-11 |
| Data minimization (video not stored server-side after analysis) | PARTIAL — videos bucket exists but lifecycle unclear | LAW25-12 |
| Purpose limitation (Gemini receives video — purpose clearly AI coaching) | PARTIAL — not disclosed to user | LAW25-04 |
