---
name: ApexAI Athletics Project Context
description: Core project identity, purpose, target users, and architecture decisions for the ApexAI Athletics Android app
type: project
---

ApexAI Athletics is a native Android CrossFit intelligence platform targeting competitive CrossFit athletes.

**Why:** Athletes need objective kinematic analysis of Olympic weightlifting movements, data-driven readiness assessments, and longitudinal performance tracking — capabilities absent from current consumer fitness apps.

**Package name:** com.apexai.crossfit
**App name:** ApexAI Athletics
**minSdk:** 26 (Android 8.0) — required by Health Connect
**targetSdk / compileSdk:** 35 (Android 15)

**Three core domains fused into one app:**
1. Relational workout/PR tracking backed by Supabase PostgreSQL
2. Physiological readiness scoring via Android Health Connect + ACWR algorithm
3. Real-time and async AI biomechanical coaching via on-device MediaPipe BlazePose + cloud Gemini LLMs

**Critical architectural constraints:**
- No Firebase / NoSQL — PostgreSQL relational integrity is non-negotiable for fitness ontology
- No cross-platform frameworks (Flutter, React Native) — MediaPipe C++ bridge is unstable
- All PR detection happens in PostgreSQL trigger, never Android client
- ExoPlayer/Media3 pooling is mandatory (max 3 players) — one-player-per-tile crashes the app
- MediaPipe Z-depth is unreliable — all coaching algorithms use 2D X/Y angular calculations only
- Gemini Context Caching is required — 75-90% token cost reduction, without it the platform is not viable at scale

**Delivery timeline:** 4-week sprint (aggressive)
- Week 1: Supabase schema, auth, Compose boilerplate, WOD logging
- Week 2: Health Connect integration, ACWR readiness scoring, PR auto-tracking
- Week 3: CameraX + MediaPipe pipeline, kinematic overlay, Gemini FastAPI microservice
- Week 4: Beta testing, performance profiling, Play Store submission

**How to apply:** All architectural decisions should be measured against these constraints before suggesting alternatives.
