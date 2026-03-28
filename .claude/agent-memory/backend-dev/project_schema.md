---
name: project_schema
description: Canonical PostgreSQL schema — tables, enums, RLS policies, PR trigger, ACWR stored function
type: project
---

**Why:** This is the single source of truth for all backend data models. All future feature additions must reference and extend this schema.

**How to apply:** When adding new tables or columns, check this file first to avoid breaking FK references or RLS policies.

## Tables
- profiles — extends auth.users(id), stores display_name, avatar_url
- movements — ExerciseDB seed data; category, primary_muscles[], equipment, biomechanical_class
- workouts — time_domain enum (AMRAP/EMOM/RFT/TABATA), scoring_metric enum
- workout_movements — junction: workout_id + movement_id + prescribed values + sort_order
- results — user workout results; score TEXT + score_numeric DECIMAL for comparisons; rpe 1-10
- personal_records — UNIQUE(user_id, movement_id, unit); auto-populated by trigger only
- health_snapshots — HRV, sleep, resting HR synced from Android Health Connect
- video_uploads — Supabase Storage path + movement_type + status enum
- coaching_reports — Gemini output: overall_assessment, rep_count, global_cues[], overlay_data JSONB
- movement_faults — child of coaching_reports; severity enum (MINOR/MODERATE/CRITICAL)

## PR Trigger
Function: check_and_update_pr() — fires AFTER INSERT ON results FOR EACH ROW.
Logic: upserts personal_records WHERE EXCLUDED.value > personal_records.value.
Unit inference: KG for Olympic Lifting/Powerlifting, REPS for Gymnastics, REPS otherwise.

## ACWR Stored Function
Function: calculate_readiness(p_user_id UUID) RETURNS JSONB — SECURITY DEFINER.
Called via Supabase RPC: POST /rpc/calculate_readiness.
Returns: acwr, zone (UNDERTRAINED/OPTIMAL/CAUTION/HIGH_RISK), acute_load, chronic_load, hrv, sleep_minutes, recommendation, calculated_at.

## RLS Summary
- profiles, results, personal_records, health_snapshots, video_uploads, coaching_reports: auth.uid() = user_id
- movement_faults: EXISTS subquery on coaching_reports.user_id = auth.uid()
- workouts, movements: SELECT only for authenticated users
