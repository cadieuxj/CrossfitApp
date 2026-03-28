---
name: ApexAI Athletics app overview
description: Architecture, feature inventory, and known gaps for the ApexAI Athletics CrossFit Android app
type: project
---

ApexAI Athletics is an Android app (Kotlin/Compose) built for CrossFit athletes.

**Why:** Positioned as an AI-powered coaching and training companion for competitive CrossFit athletes.
**How to apply:** Use this context to frame all evaluation feedback and prioritization relative to competitive CrossFit use cases.

## Architecture
- Language: Kotlin, Jetpack Compose
- DI: Hilt
- Backend: Supabase
- AI: Google Gemini (video movement analysis, cross-border data transfer with Quebec Law 25 consent flow)
- Health data: Android Health Connect (HRV, sleep, resting HR)
- Video: CameraX + Media3/ExoPlayer
- Pose detection: on-device ML (landmarks, joint angles, barbell tracking)

## Features Present (as of 2026-03-28 evaluation)
- WOD logging: score (string), RxD toggle, RPE, notes; auto PR detection
- Score parsing: ROUNDS_PLUS_REPS (R+R format), TIME (MM:SS), numeric
- TimeDomain enum: AMRAP, EMOM, RFT, TABATA
- ScoringMetric enum: REPS, TIME, LOAD, ROUNDS_PLUS_REPS
- Live camera with pose overlay, joint angle readouts, FPS display, barbell trajectory tracking
- Video review + Gemini AI analysis with consent dialog (Quebec Law 25 compliance)
- CoachingReport: movement faults with severity (MINOR/MODERATE/CRITICAL), cues, rep count, estimated weight
- Readiness dashboard: ACWR (acute 7d / chronic 28d), HRV RMSSD, sleep duration+stages, resting HR
- ReadinessZone: OPTIMAL, CAUTION, HIGH_RISK, UNDERTRAINED
- PR tracking: by movement, with history and time-range filtering (3M/6M/1Y/All)
- Health Connect integration for biometric data sync
- Auth: email/password via Supabase

## Known Gaps (identified in evaluation)
- No FOR_TIME TimeDomain (only AMRAP, EMOM, RFT, TABATA — missing the most common format)
- No scaling field beyond binary RxD/Scaled — no fractional weights, substitution logging
- No multi-movement score entry (e.g., separate split times per movement in a chipper)
- No coach portal or coach-view mode — remote programming/feedback not supported
- No competition feature set (Open/Quarterfinal/Semifinal tracking, leaderboard, qualifier calendar)
- No nutrition module whatsoever
- No community features (benchmark comparisons, athlete follows, challenges)
- No WHOOP/Garmin/Oura direct integration — relies solely on Health Connect as middleman
- No soreness/subjective readiness input (subjective wellness check-in)
- No training session tagging (AM/PM session, session type: strength vs metcon vs gymnastics)
- Pause recording uses a Camera icon (wrong icon — visually confusing)
- Discard recording uses ArrowBack icon (ambiguous — looks like navigation, not destructive action)
- Joint angle labels truncated at 8 characters during live camera ("LEFT_KNE" instead of "L.Knee")
- Readiness score displayed to 2 decimal places (%.2f) — unnecessary precision for athlete readability
- "Synced recently" is vague — no actual timestamp shown
- No benchmark WOD library (Girl WODs, Hero WODs, Open workouts)
- No WOTD subscription / coach programming feed
- Password minimum of 12 characters is unusually strict for an athlete app (minor friction)
- UserProfile has no athlete-specific fields: no weight class, competition division, affiliate, coach assignment
