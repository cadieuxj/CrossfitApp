# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ApexAI Athletics** — a native Android CrossFit intelligence platform that combines WOD/PR tracking, AI biomechanical video coaching, and physiological readiness scoring. The authoritative architectural blueprint is in `AI-Powered CrossFit App Development Plan.pdf`; the sprint plan is in `Eng plan.html`.

---

## Technology Stack

| Layer | Technology | Rationale |
|---|---|---|
| Language | Kotlin (native Android only) | Direct hardware access for CameraX/MediaPipe; no cross-platform bridges |
| UI | Jetpack Compose | Declarative state-driven UI; avoids XML hierarchy traversal overhead |
| Video Playback | Android Media3 (not legacy ExoPlayer) | Native Compose integration, kinematic overlay support |
| Backend | Supabase (PostgreSQL) | Relational schema for fitness ontologies; RLS for video security; vector embeddings for AI search |
| Health Data | Android Health Connect API | Single API for all wearables (Oura, Garmin, Apple Watch via Android); no per-device OAuth flows |
| Computer Vision | MediaPipe BlazePose (on-device, LIVE_STREAM mode) | 33 3D landmarks at real-time framerates; edge inference for privacy + zero latency |
| Barbell Tracking | OpenCV object tracker or YOLOv8 (lightweight) | Supplementary to MediaPipe for barbell path in VBT |
| AI Coaching | Gemini 1.5/3.1 Pro (video analysis) + Gemini Flash (visual correction) | Multimodal video reasoning; dual-model pipeline |
| Backend Microservice | Python FastAPI | Orchestrates Gemini multimodal pipeline; Android communicates via REST |
| CI/CD | GitHub Actions + Fastlane | Automated test → build (.aab) → sign → Play Store deploy on every push |

**Cross-platform frameworks (Flutter, React Native) are explicitly ruled out** due to MediaPipe C++ bridge instability and hardware decoder budget constraints.

---

## Architecture

### Android App — Layer Structure

```
Presentation (Jetpack Compose screens)
    ↓
ViewModel (state holders, coroutines)
    ↓
Domain (use cases, readiness algorithm, kinematic logic)
    ↓
Data (Supabase client, Health Connect, CameraX pipeline)
```

### Key Architectural Patterns

**Video rendering is layered:**
1. `PlayerView` (Media3) — video decode/playback
2. Transparent Jetpack Compose `Canvas` overlaid — real-time kinematic landmark painting (joint angles, barbell trajectory, lines of gravity)

**ExoPlayer/Media3 pooling is mandatory** — never instantiate a player per video tile in `LazyColumn`/`LazyRow`. Maintain a small fixed pool of pre-initialized instances; detach/attach surfaces on scroll.

**MediaPipe runs in `LIVE_STREAM` mode** — asynchronous `resultListener` receives normalized (X, Y) + depth (Z) per frame. Z-coordinates are experimental; coaching algorithms must prioritize 2D angular calculations from profile-view footage.

**Pose landmark indices for Olympic lifting:**
- Shoulders: 11, 12
- Hips: 23, 24
- Ankles: 27, 28

### Gemini AI Pipeline (FastAPI microservice)

```
Android upload video
    → FastAPI: Gemini 3.1/1.5 Pro analyzes full video
    → JSON response: rep count, fault timestamps, coaching cues, weight estimates
    → Extract fault frame timestamps
    → Gemini Flash: synthesize corrected posture image for each fault frame
    → Return structured coaching report to Android
```

**Context Caching is required for economic viability** — pre-load the CrossFit movement standard database, biomechanical rule sets, and athlete history as a cached context (≈75-90% token cost reduction). Each new video prompt references the cache by resource name.

### Backend Database Schema (Supabase/PostgreSQL)

Core tables:
- `Workouts` — time domain (AMRAP/EMOM/Tabata/RFT), scoring metric
- `Workout_Movements` — junction table linking workouts to movements (weight, reps, equipment)
- `Movements` — seeded from ExerciseDB (11,000+ exercises with muscle groups, biomechanical classifications)
- `Results` — athlete scores per workout
- `Personal_Records` — auto-updated by PostgreSQL trigger on `Results` insert (no client-side PR detection)

PR detection is handled entirely by a **PostgreSQL database function triggered on `Results` insert** — the Android client never computes PRs.

### Readiness Score Algorithm (ACWR)

Calculated server-side via Supabase Edge Functions:

```
ACWR = W_acute / W_chronic
W_acute  = sum of training load (volume × intensity) over past 7 days
W_chronic = rolling average of training load over past 28 days

Optimal zone: 0.8 – 1.3
High injury risk: > 1.5
```

Health Connect data types consumed:
- `HeartRateVariabilityRecord` (parasympathetic recovery)
- `SleepSessionRecord` (deep + REM cycles)
- `HeartRateRecord` during WODs (acute cardiovascular load)

Required AndroidManifest permissions:
- `android.permission.health.READ_SLEEP`
- `android.permission.health.READ_HEART_RATE_VARIABILITY`
- `android.permission.health.READ_RESTING_HEART_RATE`

---

## Build & Development Commands

Once the Android project is initialized (Gradle-based):

```bash
# Build debug APK
./gradlew assembleDebug

# Build release AAB (for Play Store)
./gradlew bundleRelease

# Run all unit tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.apexai.crossfit.YourTestClass"

# Run instrumented (on-device) tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Lint with auto-fix
./gradlew lintFix
```

Fastlane handles signing and deployment — keystore credentials live in GitHub Secrets, never in source.

---

## Feature Modules (Sprint Plan)

| Week | Deliverables |
|---|---|
| 1 | Supabase schema, auth, Compose boilerplate, WOD logging |
| 2 | Health Connect integration, ACWR readiness scoring, PR auto-tracking |
| 3 | CameraX + MediaPipe pipeline, kinematic overlay, Gemini FastAPI microservice |
| 4 | Beta testing, performance profiling, Play Store submission |

**Effort allocation:** AI video coaching 45% · Health/telemetry 25% · WOD/PR tracking 20% · Social/leaderboards 10%

---

## Critical Implementation Constraints

- **Never use NoSQL / Firebase** — relational integrity of the fitness ontology requires PostgreSQL
- **All PR detection happens in PostgreSQL** via database trigger, not Android client
- **MediaPipe Z-depth is unreliable on mobile** — use 2D profile-view angles for all coaching thresholds
- **Gemini Context Caching is non-negotiable for cost** — without it, per-video API costs make the platform non-viable at scale
- **ExoPlayer pooling is non-negotiable for stability** — one-player-per-tile will exhaust the hardware decoder budget and crash the app

---

## Required Binary Assets (not committed to git)

### MediaPipe Pose Landmarker Model

The file `app/src/main/assets/pose_landmarker_lite.task` (~5 MB) must be downloaded
manually and placed in that directory before building. It is excluded from git because
binary model files should not be version-controlled.

**Download steps:**

1. Visit the MediaPipe Models page:
   https://developers.google.com/mediapipe/solutions/vision/pose_landmarker#models
2. Download **Pose landmarker (lite)** — the file is named `pose_landmarker_lite.task`.
3. Copy it to `app/src/main/assets/pose_landmarker_lite.task`.

The `app/src/main/assets/` directory is kept in git via a `.gitkeep` file.
The model file itself is listed in `.gitignore` (or should be added if not already present).
