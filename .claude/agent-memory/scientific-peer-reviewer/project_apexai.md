---
name: project_apexai
description: Architecture overview and scientific methodology of the ApexAI Athletics app
type: project
---

ApexAI Athletics is a pre-production CrossFit AI coaching app.

**Why:** Provide data-driven coaching and injury-risk monitoring to CrossFit athletes using AI.
**How to apply:** All reviews should account for the v1 pilot framing — consenting athletes, not a clinical or medical-grade product.

## Key Scientific Subsystems

### ACWR (Readiness)
- Implemented in: `supabase/migrations/001_initial_schema.sql` (calculate_readiness() PL/pgSQL function)
- Client side: `ReadinessRepositoryImpl.kt`, `ReadinessScore.kt`
- Formula: acute = SUM(score_numeric * RPE) over 7 days; chronic = SUM(score_numeric * RPE) over 28 days / 4
- Zones: <0.8 UNDERTRAINED, 0.8-1.3 OPTIMAL, 1.3-1.5 CAUTION, >1.5 HIGH_RISK
- HRV thresholds hardcoded: <40ms low, 40-69ms normal, >=70ms recovered
- Sleep thresholds: <360min insufficient, 360-480min adequate, >=480min optimal

### Gemini Video Pipeline
- Model: gemini-1.5-pro for analysis, gemini-2.0-flash for corrective image generation
- Knowledge base: inline text (CROSSFIT_KNOWLEDGE_BASE string) loaded as context cache
- Overlay data: Gemini is asked to HALLUCINATE pose landmarks and joint angles (not from MediaPipe)
- Movement types: 36 supported movements
- Fault taxonomy: MINOR / MODERATE / CRITICAL with single-cue coaching

### Health Data
- Source: Android Health Connect (READ_HEART_RATE_VARIABILITY, READ_SLEEP, READ_RESTING_HEART_RATE)
- Storage: health_snapshots table (Supabase), one row per user per day
- Used for: HRV and sleep notes appended to ACWR recommendation text

## Security Audit Status (as of 2026-03-28)
- Security audit completed in docs/SECURITY_AUDIT.md
- Several critical issues in the audit appear addressed in current main.py:
  - C-02 (rate limiter) — user-keyed rate limiting now implemented
  - C-03 (cache endpoint) — require_admin_user dependency now applied
  - C-04 (job store ownership) — user_id now stored and enforced
- Remaining open: Law 25 compliance gaps, CORS wildcard, python-jose CVE, mock video URI
