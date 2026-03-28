---
name: project_architecture
description: ApexAI Athletics tech stack, key architectural decisions, and CLAUDE.md constraints
type: project
---

**App:** ApexAI Athletics — native Android CrossFit coaching platform (Kotlin/Jetpack Compose).

**Why:** AI-powered biomechanical video analysis + physiological readiness scoring for CrossFit athletes.

**Tech Stack:**
- Language: Kotlin, native Android only (no Flutter/React Native)
- UI: Jetpack Compose + Material3
- Video: Android Media3 (not legacy ExoPlayer)
- Backend DB: Supabase (PostgreSQL + RLS)
- Health: Android Health Connect API
- CV: MediaPipe BlazePose LIVE_STREAM mode, on-device
- AI Coaching: Gemini 1.5/3.1 Pro (video analysis) + Gemini Flash (posture correction images)
- Backend service: Python FastAPI microservice
- CI/CD: GitHub Actions + Fastlane

**Architecture:** Single-activity, Hilt DI, MVVM + clean architecture layers (Presentation → ViewModel → Domain → Data).

**Hard constraints from CLAUDE.md:**
- No NoSQL/Firebase — PostgreSQL only
- PR detection via PostgreSQL trigger only (never Android client)
- MediaPipe Z-depth unreliable — use 2D profile-view angles
- Gemini Context Caching mandatory for cost viability
- ExoPlayer pooling mandatory (pool size = 2, never one-player-per-tile)

**Key Android dependencies (from libs.versions.toml):**
- AGP 8.6.1, Kotlin 2.0.21, Compose BOM 2025.01.00
- Supabase 3.0.2, Ktor 3.0.3, Media3 1.5.1
- CameraX 1.4.1, MediaPipe 0.10.21, Health Connect 1.1.0-rc01
- Hilt 2.51.1, Room 2.7.0, Coil 3.0.4
- JUnit 5.11.3 for unit tests
