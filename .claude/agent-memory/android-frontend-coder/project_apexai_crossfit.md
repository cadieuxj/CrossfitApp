---
name: ApexAI Athletics Android Project Context
description: Core project facts, architecture constraints, and patterns for the ApexAI CrossFit Android app
type: project
---

ApexAI Athletics is a native Android CrossFit intelligence platform (Kotlin + Jetpack Compose) with three domains: WOD/PR tracking via Supabase PostgreSQL, physiological readiness via Android Health Connect + ACWR algorithm, and AI biomechanical coaching via on-device MediaPipe BlazePose + cloud Gemini multimodal.

**Why:** Competitive CrossFit athletes need objective kinematic analysis, readiness assessments, and longitudinal performance tracking in one app. No cross-platform bridge — direct hardware access for CameraX/MediaPipe is non-negotiable.

**How to apply:** Every architectural decision must preserve these three domains' independence. Feature modules must not depend on each other.

## Package structure
- `com.apexai.crossfit` — app module
- `com.apexai.crossfit.core.*` — shared infra (ui, model, data, network, media, health)
- `com.apexai.crossfit.feature.*` — feature modules (auth, wod, pr, readiness, vision, coaching)

## Mandatory architectural rules (from CLAUDE.md)
1. ExoPlayer MUST use PlayerPoolManager (pool size 2) — never instantiate inline per-tile
2. MediaPipe MUST run in LIVE_STREAM mode with async resultListener (not SINGLE_IMAGE)
3. PR detection NEVER client-side — PostgreSQL trigger on results INSERT handles it
4. Health Connect ONLY via Android Health Connect API (not Oura/Garmin APIs directly)
5. All API keys via BuildConfig fields (SUPABASE_URL, SUPABASE_ANON_KEY, FASTAPI_BASE_URL)
6. CameraX PreviewView in PERFORMANCE mode (not COMPATIBLE)
7. Canvas kinematic overlay uses landmarks: shoulders 11/12, hips 23/24, ankles 27/28
8. MediaPipe Z-depth is unreliable — use 2D profile-view angles only for coaching thresholds
9. Gemini Context Caching is non-negotiable for cost viability

## SDK/library versions
- Compose BOM: 2025.01.00
- Kotlin: 2.0.21
- Hilt: 2.51.1
- Supabase Kotlin: 3.0.2
- Media3: 1.5.1
- CameraX: 1.4.1
- MediaPipe Tasks Vision: 0.10.21
- Health Connect: 1.1.0-rc01
- Navigation Compose: 2.8.5
- Coil3: 3.0.4
- minSdk: 26, targetSdk: 35, compileSdk: 35

## Backend contracts
- Supabase REST for workouts, results, personal_records, health_snapshots, coaching_reports
- FastAPI `/api/v1/coaching/analyze` for Gemini video analysis (multipart upload)
- Supabase RPC `calculate_readiness` for ACWR score
- Supabase Storage for video files (private bucket, signed URLs)

## Navigation routes
- splash, auth/login, auth/register, home
- wod, wod/{wodId}, wod/{wodId}/log, wod/{wodId}/timer, wod/history
- pr, pr/{movementId}
- readiness, readiness/setup
- vision/live, vision/review/{videoUri}
- coaching/report/{analysisId}, coaching/playback/{videoId}
- profile
- Deep links: apexai://wod/{wodId}, apexai://coaching/report/{analysisId}
