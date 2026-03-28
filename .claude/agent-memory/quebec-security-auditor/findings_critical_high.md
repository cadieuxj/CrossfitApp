---
name: Critical and High Findings — Initial Audit
description: CRITICAL and HIGH severity security and compliance findings from the 2026-03-28 audit of ApexAI Athletics
type: project
---

**Why:** Baseline record of the most serious issues found in the first audit pass. Update remediation status as fixes are applied.

**How to apply:** These are the highest priority items. Do not mark resolved without verifying the fix in code.

## CRITICAL Findings

| ID | Title | File | Status |
|---|---|---|---|
| C-01 | CORS wildcard (*) in production config | backend/main.py line 110 | OPEN |
| C-02 | Rate limiter keyed on IP, not user ID — authenticated endpoint bypass | backend/main.py line 214 | OPEN |
| C-03 | /v1/cache/refresh has no admin guard — any authenticated user can evict Gemini cache | backend/main.py lines 919-956 | OPEN |
| C-04 | In-memory job store (_analysis_jobs) lost on restart — no ownership check on job IDs | backend/main.py line 122 | OPEN |
| C-05 | Video URI passed through navigation as raw string — deeplink injection risk | AppNavigation.kt lines 302-305 | OPEN |
| C-06 | VisionViewModel.stopRecording() emits hardcoded mock URI "content://mock/recording.mp4" | VisionViewModel.kt line 170 | OPEN |

## HIGH Findings

| ID | Title | File | Status |
|---|---|---|---|
| H-01 | No privacy policy or Terms of Service in registration flow — Law 25 violation | RegisterScreen.kt, LoginScreen.kt | OPEN |
| H-02 | No explicit consent for health data collection (HRV, sleep, HR) — Law 25 Art. 12-14 | HealthConnectDataSource.kt | OPEN |
| H-03 | No explicit consent for video/AI analysis by Google Gemini — cross-border transfer | VisionViewModel.kt, gemini_service.py | OPEN |
| H-04 | No data retention or destruction policy — videos bucket may accumulate indefinitely | 001_initial_schema.sql | OPEN |
| H-05 | No right to erasure endpoint — Law 25 Art. 28 violation | backend/main.py (no DELETE endpoint) | OPEN |
| H-06 | Supabase service_role_key used for all writes — single key compromise = full DB access | backend/main.py line 138 | OPEN |
| H-07 | Gemini safety settings relaxed (BLOCK_ONLY_HIGH for dangerous content) — no audit log | gemini_service.py lines 77-82 | OPEN |
| H-08 | No Privacy Impact Assessment (PIA) documented for new project — Law 25 Art. 3.3 | N/A | OPEN |
| H-09 | Password minimum length only 6 characters — weak credential policy | LoginViewModel.kt line 100 | OPEN |
| H-10 | file_paths.xml exposes entire app files directory via FileProvider | file_paths.xml line 12 | OPEN |
| H-11 | No Sentry PII scrubbing configuration — health/biometric data may appear in error reports | requirements.txt (sentry-sdk) | OPEN |
