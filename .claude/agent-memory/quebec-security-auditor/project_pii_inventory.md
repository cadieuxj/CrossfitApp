---
name: PII Inventory
description: Inventory of personal information collected by ApexAI Athletics relevant to Quebec Law 25 scope
type: project
---

**Why:** Law 25 (Loi 25) requires knowing exactly what personal information is collected, where it is stored, and where it flows. This inventory was constructed during the 2026-03-28 audit.

**How to apply:** Use this inventory when assessing consent requirements, data minimization, retention policy gaps, and cross-border transfer obligations.

## Personal Information Categories

| Category | Sensitivity | Storage Location | Cross-Border Transfer |
|---|---|---|---|
| Email address | Medium | Supabase auth.users | Supabase (jurisdiction TBD) |
| Display name | Low | Supabase profiles | Supabase |
| Avatar URL | Low | Supabase profiles | Supabase |
| Workout scores, RPE, notes | Medium | Supabase results | Supabase |
| Personal records (lifts) | Medium | Supabase personal_records | Supabase |
| HRV (heart rate variability) | HIGH — health data | Supabase health_snapshots | Supabase |
| Sleep duration + stages | HIGH — health data | Supabase health_snapshots | Supabase |
| Resting heart rate | HIGH — health data | Supabase health_snapshots | Supabase |
| Video of athlete performing exercise | HIGH — biometric/visual | Supabase Storage (videos bucket) + Google Gemini File API (transient) | Google (US) |
| AI coaching analysis (body position data) | HIGH — biometric | Supabase coaching_reports (overlay_data JSONB) | Supabase |
| Corrective posture images | HIGH — biometric | Supabase Storage (corrections bucket) | Supabase |
| IP address (rate limiting) | Medium | FastAPI in-memory (slowapi) | N/A (ephemeral) |

## Special Categories Under Law 25
- Health data (HRV, sleep, heart rate) constitutes sensitive personal information requiring heightened protection
- Video data containing identifiable biometric characteristics (body/face) is sensitive personal information
- Biomechanical pose overlay data (landmark coordinates) may constitute biometric data

## No Data Collected
- No payment/financial data (no PCI scope)
- No government ID numbers
- No geolocation beyond what Android system may infer
