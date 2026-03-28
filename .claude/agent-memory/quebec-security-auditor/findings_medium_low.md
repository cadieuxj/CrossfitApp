---
name: Medium and Low Findings — Initial Audit
description: MEDIUM and LOW severity findings from the 2026-03-28 audit of ApexAI Athletics
type: project
---

**Why:** Secondary priority findings that should be addressed before or shortly after launch.

**How to apply:** Address after Critical/High findings are resolved.

## MEDIUM Findings

| ID | Title | File | Status |
|---|---|---|---|
| M-01 | Deep links use custom scheme (apexai://) without HTTPS fallback — hijackable by malicious apps | AndroidManifest.xml | OPEN |
| M-02 | No explicit RECORD_AUDIO justification in manifest — potential Play Store rejection or user concern | AndroidManifest.xml line 15 | OPEN |
| M-03 | Signed URLs for corrections have 1-hour TTL — no revocation mechanism | main.py lines 376-380 | OPEN |
| M-04 | error_message from internal exceptions written to video_uploads.error_message column (up to 500 chars) — may leak internal details | main.py line 467 | OPEN |
| M-05 | calculate_readiness() SECURITY DEFINER function has no rate limit at RPC layer | 001_initial_schema.sql | OPEN |
| M-06 | python-jose 3.3.0 — check for CVEs (known algorithm confusion issues in older versions) | requirements.txt | OPEN |
| M-07 | No account lockout / brute-force protection on login beyond Supabase defaults | AuthRepositoryImpl.kt | OPEN |
| M-08 | No email verification enforced before access granted | AuthRepositoryImpl.kt | OPEN |
| M-09 | data_extraction_rules.xml does not exclude file domain for device-transfer | data_extraction_rules.xml | OPEN |

## LOW Findings

| ID | Title | File | Status |
|---|---|---|---|
| L-01 | Health check endpoint leaks Gemini cache state (internal system info) | main.py line 490 | OPEN |
| L-02 | No Content-Security-Policy or other security headers (backend serves JSON API — low risk but noted) | main.py | OPEN |
| L-03 | RECORD_AUDIO permission declared but no visible audio-specific UI disclosure | AndroidManifest.xml | OPEN |
| L-04 | No CASL-compliant unsubscribe mechanism for any future marketing emails | N/A | OPEN |
| L-05 | movements.instructions column may contain coaching IP but has no access restriction beyond authenticated role | 001_initial_schema.sql | OPEN |
