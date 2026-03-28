---
name: project_overview
description: ApexAI Athletics — project goals, 4-week sprint plan, technology decisions, and critical constraints
type: project
---

ApexAI Athletics is a native Android CrossFit intelligence platform (as of 2026-03-28).

**Why:** Competitive CrossFit athletes need objective kinematic analysis, data-driven readiness, and longitudinal PR tracking — capabilities absent from existing consumer apps.

**How to apply:** All backend decisions must serve the Android client. Bandwidth efficiency (pagination, compact JSON), multipart video upload support, and JWT auth compatible with Supabase are required from day one.

## Sprint Plan
- Week 1 (ends ~2026-04-04): Supabase schema, auth, Compose boilerplate, WOD logging
- Week 2 (ends ~2026-04-11): Health Connect integration, ACWR readiness, PR auto-tracking
- Week 3 (ends ~2026-04-18): CameraX + MediaPipe pipeline, Gemini FastAPI microservice
- Week 4 (ends ~2026-04-25): Beta testing, performance profiling, Play Store submission

## Tech Stack (Backend)
- Supabase (PostgreSQL + PostgREST + Auth + Storage + Edge Functions)
- Python FastAPI 0.115+ microservice for Gemini orchestration
- Gemini 1.5 Pro (video analysis) + Gemini 2.0 Flash (corrective image generation)
- Gemini Context Caching — pre-loads CrossFit movement standards + athlete history
- Docker container deployment (Cloud Run / Railway / Fly.io — not yet decided as of 2026-03-28)

## Critical Constraints
- All PR detection via PostgreSQL trigger — never application-layer
- Gemini Context Caching is non-negotiable (cost viability)
- FastAPI validates Supabase JWTs directly — no separate auth system
- Video storage bucket must be private with signed URL generation
- Model names: gemini-1.5-pro for video, gemini-2.0-flash for image (spec PDF said "Gemini 3.1 Pro" which does not exist — assumption A-1 in ARCHITECTURE_PLAN.md)
