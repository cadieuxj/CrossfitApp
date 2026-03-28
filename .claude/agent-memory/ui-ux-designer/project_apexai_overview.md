---
name: ApexAI Athletics — Project Overview
description: Core product facts, tech stack, and design constraints for the ApexAI Athletics CrossFit Android app
type: project
---

ApexAI Athletics is a native Android CrossFit intelligence platform (Kotlin + Jetpack Compose) targeting competitive CrossFit athletes. It combines WOD/PR tracking, AI biomechanical video coaching (MediaPipe + Gemini), and physiological readiness scoring (ACWR + Health Connect).

**Why:** One-month delivery timeline with no existing wireframes or mockups. All screens derived from feature specifications in ARCHITECTURE_PLAN.md.

**How to apply:** All UI designs must be Compose-native, dark athletic theme, implement Clean Architecture MVVM pattern, and respect the constraints below.

## Critical Constraints for Design
- Platform: Android only (minSdk 26, targetSdk 35). No iOS or web.
- UI: Jetpack Compose exclusively. No XML layouts.
- Video: Media3 ExoPlayer pool (never one-player-per-tile). Canvas overlay on top of PlayerView.
- Camera: CameraX LIVE_STREAM mode. Pose overlay via Compose Canvas (not SurfaceView).
- PR detection: Server-side PostgreSQL trigger ONLY. UI displays results, never computes PRs.
- No NoSQL/Firebase — PostgreSQL via Supabase only.
- MediaPipe Z-depth unreliable — all coaching angles use 2D profile-view calculations only.
- Bottom nav has 5 items; center item is a FAB-style camera button (not a standard nav item).

## Sprint Timeline (2026)
- Week 1 (by ~2026-04-04): Supabase schema + auth + Compose boilerplate + WOD logging
- Week 2 (by ~2026-04-11): Health Connect + ACWR readiness + PR auto-tracking
- Week 3 (by ~2026-04-18): CameraX + MediaPipe pipeline + Gemini FastAPI microservice
- Week 4 (by ~2026-04-25): Beta testing + performance profiling + Play Store submission

## Effort Allocation
AI video coaching 45% · Health/telemetry 25% · WOD/PR tracking 20% · Social/leaderboards 10%
