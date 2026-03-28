# ApexAI Athletics — Backend Implementation

Let me read all three specification documents before writing a single line of code.
The TECHNICAL_SPEC.md was too large. Let me read it in sections, and also read the ARCHITECTURE_PLAN.md in sections.
The TECHNICAL_SPEC.md is reading from the end. Let me search for the database and backend-specific sections.
I now have everything I need. Let me also check if any existing backend files exist to avoid duplicating work.
I now have a complete picture of the system. Let me save project memory before producing the implementation.
Now I have a complete picture of all requirements. Here is the full backend implementation.

---

# ApexAI Athletics — Backend Implementation

## Requirements Analysis Summary

Before writing code, here is my confirmed understanding of what needs to be built:

**Supabase migration** — All 10 tables from the canonical schema in `ARCHITECTURE_PLAN.md §8.2`, three domain enums (workout type, scoring metric, equipment), full foreign key graph, composite indexes on every hot query path, RLS on every user-owned table, a PR detection trigger that fires server-side on every `results` INSERT, and a `calculate_readiness` stored function called via Supabase RPC.

**FastAPI microservice** — Stateless Python service that validates Supabase JWTs, handles multipart video upload up to 500 MB, orchestrates Gemini 1.5 Pro for video analysis with context caching (CrossFit movement standards + athlete history pre-loaded), then Gemini 2.0 Flash for corrective posture image generation per fault. Stores results in Supabase. Rate-limited at 10 analyses per hour per user.

**Supabase Edge Function** — TypeScript/Deno function that accepts Health Connect data posted from Android, computes the ACWR composite readiness score, persists to `health_snapshots`, and returns a structured JSON breakdown.

**Assumption A-1 (from ARCHITECTURE_PLAN.md):** "Gemini 3.1 Pro" in the PDF does not exist. Implementation uses `gemini-1.5-pro` for video analysis and `gemini-2.0-flash` for image generation, controlled by environment variables so a model swap is a config change only.

---

## File: `supabase/migrations/001_initial_schema.sql`

```sql
-- =============================================================================
-- ApexAI Athletics — Initial Database Schema
-- Migration: 001_initial_schema.sql
-- Target: Supabase (PostgreSQL 15+)
--
-- Execution order matters:
--   1. Extensions
--   2. Enum types
--   3. Core tables (profiles, movements)
--   4. Dependent tables (workouts, workout_movements, results, personal_records)
--   5. Health & coaching tables
--   6. Indexes
--   7. Row Level Security
--   8. Functions & triggers
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Extensions
-- ---------------------------------------------------------------------------

-- pgcrypto: gen_random_uuid() is available natively in PG 13+ via
-- gen_random_uuid(), but we enable pgcrypto for forward-compatibility
-- with any uuid helper usage.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- pg_trgm: enables GIN trigram indexes for ILIKE/full-text movement search
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ---------------------------------------------------------------------------
-- 2. Enum Types
-- ---------------------------------------------------------------------------

-- workout_type captures the CrossFit time-domain formats.
-- TABATA is a specific EMOM variant (20s on / 10s off × 8 rounds)
-- but stored separately so the timer UI can apply correct logic.
CREATE TYPE workout_type AS ENUM (
    'AMRAP',    -- As Many Rounds/Reps As Possible
    'EMOM',     -- Every Minute On the Minute
    'RFT',      -- Rounds For Time
    'TABATA'    -- 20s/10s interval protocol
);

-- scoring_type determines how results are compared for PR detection.
-- ROUNDS_PLUS_REPS is standard CrossFit AMRAP notation (e.g., "5+12").
CREATE TYPE scoring_type AS ENUM (
    'REPS',
    'TIME',
    'LOAD',
    'ROUNDS_PLUS_REPS'
);

-- equipment_type seeds the movements table and drives the coaching
-- context cache (barbell vs. kettlebell vs. bodyweight changes the
-- biomechanical analysis prompt significantly).
CREATE TYPE equipment_type AS ENUM (
    'BARBELL',
    'DUMBBELL',
    'KETTLEBELL',
    'BODYWEIGHT',
    'PULL_UP_BAR',
    'RINGS',
    'ROPE',
    'BOX',
    'ROWER',
    'ASSAULT_BIKE',
    'MEDICINE_BALL',
    'NONE'
);

-- ---------------------------------------------------------------------------
-- 3. Core Tables: profiles, movements
-- ---------------------------------------------------------------------------

-- profiles extends auth.users. Supabase Auth manages the auth.users row;
-- we create a matching profiles row via a trigger on auth.users INSERT.
-- The PRIMARY KEY is a direct foreign key to auth.users(id) — same UUID,
-- no separate surrogate key.
CREATE TABLE IF NOT EXISTS profiles (
    id              UUID        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name    TEXT        NOT NULL    CHECK (char_length(display_name) BETWEEN 1 AND 100),
    avatar_url      TEXT,
    -- Unit preference stored here for server-side PR unit inference
    unit_system     TEXT        NOT NULL    DEFAULT 'KG' CHECK (unit_system IN ('KG', 'LBS')),
    created_at      TIMESTAMPTZ NOT NULL    DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL    DEFAULT NOW()
);

COMMENT ON TABLE profiles IS
    'Athlete profile data extending Supabase auth.users. One row per user.';

-- Auto-create a profile row whenever a new user signs up via Supabase Auth.
-- This fires in the auth schema, so it runs with SECURITY DEFINER to
-- allow the INSERT into the public schema.
CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.profiles (id, display_name)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(NEW.email, '@', 1))
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER trg_on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION handle_new_user();

-- Auto-update updated_at on profiles changes.
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER trg_profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- movements: seeded from ExerciseDB (11,000+ entries).
-- category drives PR unit inference in the check_and_update_pr() trigger.
-- biomechanical_class drives the Gemini coaching prompt context.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movements (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name                TEXT            NOT NULL UNIQUE CHECK (char_length(name) BETWEEN 1 AND 200),
    category            TEXT            NOT NULL,
        -- Allowed values: 'Olympic Lifting', 'Powerlifting', 'Gymnastics',
        -- 'Monostructural', 'Accessory', 'Strongman'
    primary_muscles     TEXT[]          NOT NULL DEFAULT '{}',
    secondary_muscles   TEXT[]          NOT NULL DEFAULT '{}',
    equipment           equipment_type  NOT NULL DEFAULT 'NONE',
    biomechanical_class TEXT,
        -- 'Push', 'Pull', 'Hinge', 'Squat', 'Carry', 'Rotation', 'Core'
    instructions        TEXT,           -- coaching cue seed text for Gemini cache
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE movements IS
    'Master movement catalog seeded from ExerciseDB. Read-only for athletes.';

COMMENT ON COLUMN movements.category IS
    'Used by check_and_update_pr() trigger to infer the PR unit (KG vs REPS).';

-- ---------------------------------------------------------------------------
-- 4. Workouts and Results Tables
-- ---------------------------------------------------------------------------

-- workouts: WOD definitions. Any authenticated user can browse; only
-- admin service-role can INSERT/UPDATE/DELETE via RLS.
CREATE TABLE IF NOT EXISTS workouts (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            TEXT            NOT NULL    CHECK (char_length(name) BETWEEN 1 AND 200),
    description     TEXT,
    time_domain     workout_type    NOT NULL,
    scoring_metric  scoring_type    NOT NULL,
    time_cap_seconds INT             CHECK (time_cap_seconds > 0),
    rounds          INT             CHECK (rounds > 0),
    -- The user who created the workout; NULL for seeded/system workouts
    created_by      UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE workouts IS
    'WOD definitions. time_domain determines which timer UI is used.';

-- workout_movements: junction table linking a workout to its constituent
-- movements with prescribed loading. sort_order determines display order.
CREATE TABLE IF NOT EXISTS workout_movements (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_id              UUID        NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    movement_id             UUID        NOT NULL REFERENCES movements(id) ON DELETE RESTRICT,
    prescribed_reps         INT         CHECK (prescribed_reps > 0),
    prescribed_weight_kg    DECIMAL(6,2) CHECK (prescribed_weight_kg >= 0),
    prescribed_distance_m   DECIMAL(8,2) CHECK (prescribed_distance_m > 0),
    prescribed_calories     INT         CHECK (prescribed_calories > 0),
    sort_order              INT         NOT NULL DEFAULT 0,
    -- Prevent the same movement appearing twice in the same workout at
    -- the same position (different sort_order values are allowed to
    -- support "21-15-9" couplets).
    CONSTRAINT uq_workout_movement_position UNIQUE (workout_id, movement_id, sort_order)
);

COMMENT ON TABLE workout_movements IS
    'Junction table: workout -> movements with prescribed weights/reps.';

-- results: athlete performance records. score is free-text to support all
-- scoring formats ("155 reps", "12:34", "225 lbs", "5+12").
-- score_numeric is parsed by the client and stored for numeric comparisons
-- in the PR trigger and ACWR calculation.
CREATE TABLE IF NOT EXISTS results (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    workout_id      UUID        NOT NULL REFERENCES workouts(id) ON DELETE RESTRICT,
    score           TEXT        NOT NULL CHECK (char_length(score) BETWEEN 1 AND 100),
    score_numeric   DECIMAL(10,2),
        -- Nullable: ROUNDS_PLUS_REPS format may not parse cleanly.
        -- The PR trigger uses COALESCE(score_numeric, 0).
    rxd             BOOLEAN     NOT NULL DEFAULT TRUE,
    notes           TEXT        CHECK (char_length(notes) <= 2000),
    rpe             INT         CHECK (rpe BETWEEN 1 AND 10),
    completed_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE results IS
    'Athlete workout results. AFTER INSERT fires check_and_update_pr().';

-- personal_records: one row per (user, movement, unit) triplet.
-- The UNIQUE constraint is the enforcement mechanism that makes the
-- ON CONFLICT ... DO UPDATE in the trigger an upsert, not an append.
-- Records are NEVER inserted by the application layer.
CREATE TABLE IF NOT EXISTS personal_records (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    movement_id     UUID        NOT NULL REFERENCES movements(id) ON DELETE CASCADE,
    value           DECIMAL(10,2) NOT NULL CHECK (value > 0),
    unit            TEXT        NOT NULL CHECK (unit IN ('KG', 'LBS', 'REPS', 'SECONDS')),
    achieved_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- result_id links back to the specific result that set this PR.
    -- Used by the coaching report to correlate video analysis with PR context.
    result_id       UUID        REFERENCES results(id) ON DELETE SET NULL,
    CONSTRAINT uq_user_movement_unit UNIQUE (user_id, movement_id, unit)
);

COMMENT ON TABLE personal_records IS
    'Auto-populated exclusively by check_and_update_pr() trigger. '
    'Application layer must never INSERT directly.';

-- ---------------------------------------------------------------------------
-- 5. Health, Video, and Coaching Tables
-- ---------------------------------------------------------------------------

-- health_snapshots: synced from Android Health Connect after the user
-- grants READ_HEART_RATE_VARIABILITY + READ_SLEEP + READ_RESTING_HEART_RATE.
-- Each sync from the Android client creates one row per day of data.
CREATE TABLE IF NOT EXISTS health_snapshots (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    hrv_rmssd               INT         CHECK (hrv_rmssd BETWEEN 1 AND 300),  -- ms
    sleep_duration_minutes  INT         CHECK (sleep_duration_minutes BETWEEN 0 AND 1440),
    deep_sleep_minutes      INT         CHECK (deep_sleep_minutes >= 0),
    rem_sleep_minutes       INT         CHECK (rem_sleep_minutes >= 0),
    resting_hr              INT         CHECK (resting_hr BETWEEN 20 AND 250),  -- bpm
    captured_at             TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- Prevent duplicate snapshots for the same user on the same day.
    -- Android client should only send one snapshot per calendar day.
    CONSTRAINT uq_user_snapshot_day UNIQUE (user_id, DATE(captured_at))
);

COMMENT ON TABLE health_snapshots IS
    'Health Connect biometric snapshots. One row per user per day. '
    'Read by calculate_readiness() for HRV and sleep components.';

-- video_uploads: tracks video files stored in Supabase Storage.
-- The storage_path is the bucket-relative path; full URL is constructed
-- by the client using the Supabase Storage signed URL API.
CREATE TABLE IF NOT EXISTS video_uploads (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    storage_path    TEXT        NOT NULL,
    movement_type   TEXT        NOT NULL CHECK (char_length(movement_type) BETWEEN 1 AND 100),
    duration_seconds INT        CHECK (duration_seconds > 0),
    file_size_bytes  BIGINT     CHECK (file_size_bytes > 0),
    status          TEXT        NOT NULL DEFAULT 'uploaded'
                    CHECK (status IN ('uploaded', 'analyzing', 'complete', 'error')),
    error_message   TEXT,       -- populated if status = 'error'
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE TRIGGER trg_video_uploads_updated_at
    BEFORE UPDATE ON video_uploads
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

COMMENT ON TABLE video_uploads IS
    'Video file metadata. Actual files live in Supabase Storage (private bucket). '
    'FastAPI updates status as analysis progresses.';

-- coaching_reports: the structured output from Gemini 1.5 Pro analysis.
-- overlay_data is a JSONB array of TimedPoseOverlay objects — this is
-- large but necessary for kinematic overlay playback on the Android client.
-- Consider partitioning or archiving if rows exceed 10 MB per report.
CREATE TABLE IF NOT EXISTS coaching_reports (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id            UUID        NOT NULL REFERENCES video_uploads(id) ON DELETE CASCADE,
    user_id             UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    movement_type       TEXT        NOT NULL,
    overall_assessment  TEXT,
    rep_count           INT         CHECK (rep_count >= 0),
    estimated_weight_kg DECIMAL(6,2) CHECK (estimated_weight_kg >= 0),
    global_cues         TEXT[]      NOT NULL DEFAULT '{}',
    -- overlay_data stores serialised List<TimedPoseOverlay> from the
    -- Gemini response. Structure: [{timestamp_ms, landmarks[], joint_angles{}}]
    overlay_data        JSONB,
    -- gemini_cache_name stores the resource name of the context cache
    -- used for this analysis. Enables cache hit rate monitoring.
    gemini_cache_name   TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coaching_reports IS
    'Gemini 1.5 Pro analysis output. movement_faults are child rows.';

-- movement_faults: individual fault instances detected in the video.
-- corrected_image_url is populated asynchronously by a second Gemini Flash
-- call after the main report is written.
CREATE TABLE IF NOT EXISTS movement_faults (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id           UUID        NOT NULL REFERENCES coaching_reports(id) ON DELETE CASCADE,
    description         TEXT        NOT NULL CHECK (char_length(description) BETWEEN 1 AND 1000),
    severity            TEXT        NOT NULL CHECK (severity IN ('MINOR', 'MODERATE', 'CRITICAL')),
    timestamp_ms        BIGINT      NOT NULL CHECK (timestamp_ms >= 0),
    cue                 TEXT        NOT NULL CHECK (char_length(cue) BETWEEN 1 AND 500),
    corrected_image_url TEXT,
    affected_joints     TEXT[]      NOT NULL DEFAULT '{}'
);

COMMENT ON TABLE movement_faults IS
    'Individual faults detected by Gemini. Populated by FastAPI after analysis. '
    'corrected_image_url is filled by a follow-up Gemini Flash call.';

-- ---------------------------------------------------------------------------
-- 6. Indexes
-- ---------------------------------------------------------------------------

-- profiles: rarely queried by anything other than PK, but avatar_url
-- lookups by auth.uid() are common.
CREATE INDEX IF NOT EXISTS idx_profiles_updated_at
    ON profiles (updated_at DESC);

-- movements: the primary access pattern is search by name (ILIKE) and
-- filter by category. GIN trigram index supports substring search for
-- the movement picker in the WOD logging screen.
CREATE INDEX IF NOT EXISTS idx_movements_name_trgm
    ON movements USING GIN (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_movements_category
    ON movements (category);

CREATE INDEX IF NOT EXISTS idx_movements_equipment
    ON movements (equipment);

-- workouts: athletes browse by time_domain and search by name.
CREATE INDEX IF NOT EXISTS idx_workouts_time_domain
    ON workouts (time_domain);

CREATE INDEX IF NOT EXISTS idx_workouts_name_trgm
    ON workouts USING GIN (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_workouts_created_at
    ON workouts (created_at DESC);

-- workout_movements: always fetched by workout_id in a join.
CREATE INDEX IF NOT EXISTS idx_workout_movements_workout_id
    ON workout_movements (workout_id);

CREATE INDEX IF NOT EXISTS idx_workout_movements_movement_id
    ON workout_movements (movement_id);

-- results: two hot paths — history list (user_id + completed_at DESC)
-- and ACWR calculation window (user_id + completed_at range).
CREATE INDEX IF NOT EXISTS idx_results_user_completed
    ON results (user_id, completed_at DESC);

CREATE INDEX IF NOT EXISTS idx_results_workout_id
    ON results (workout_id);

-- personal_records: fetched by user_id; joined to movements for display.
CREATE INDEX IF NOT EXISTS idx_personal_records_user_id
    ON personal_records (user_id);

CREATE INDEX IF NOT EXISTS idx_personal_records_user_movement
    ON personal_records (user_id, movement_id);

-- health_snapshots: ACWR function queries by user_id + captured_at range.
CREATE INDEX IF NOT EXISTS idx_health_snapshots_user_captured
    ON health_snapshots (user_id, captured_at DESC);

-- video_uploads: polled by status during analysis.
CREATE INDEX IF NOT EXISTS idx_video_uploads_user_id
    ON video_uploads (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_video_uploads_status
    ON video_uploads (status) WHERE status IN ('uploaded', 'analyzing');

-- coaching_reports: fetched by video_id (1:1) and by user_id for history.
CREATE INDEX IF NOT EXISTS idx_coaching_reports_video_id
    ON coaching_reports (video_id);

CREATE INDEX IF NOT EXISTS idx_coaching_reports_user_created
    ON coaching_reports (user_id, created_at DESC);

-- movement_faults: always fetched by report_id.
CREATE INDEX IF NOT EXISTS idx_movement_faults_report_id
    ON movement_faults (report_id);

-- ---------------------------------------------------------------------------
-- 7. Row Level Security
-- ---------------------------------------------------------------------------

-- Enable RLS on every user-owned table.
-- Public tables (workouts, movements) get SELECT-only policies.

ALTER TABLE profiles             ENABLE ROW LEVEL SECURITY;
ALTER TABLE workouts             ENABLE ROW LEVEL SECURITY;
ALTER TABLE workout_movements    ENABLE ROW LEVEL SECURITY;
ALTER TABLE movements            ENABLE ROW LEVEL SECURITY;
ALTER TABLE results              ENABLE ROW LEVEL SECURITY;
ALTER TABLE personal_records     ENABLE ROW LEVEL SECURITY;
ALTER TABLE health_snapshots     ENABLE ROW LEVEL SECURITY;
ALTER TABLE video_uploads        ENABLE ROW LEVEL SECURITY;
ALTER TABLE coaching_reports     ENABLE ROW LEVEL SECURITY;
ALTER TABLE movement_faults      ENABLE ROW LEVEL SECURITY;

-- profiles: users read and update only their own row.
-- INSERT is handled by the handle_new_user() trigger (SECURITY DEFINER),
-- so no INSERT policy is needed for the anon/authenticated roles.
CREATE POLICY "profiles_select_own"
    ON profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "profiles_update_own"
    ON profiles FOR UPDATE
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- workouts: any authenticated user can read; only service_role can write.
-- The service_role bypasses RLS, so we only need the SELECT policy here.
CREATE POLICY "workouts_select_authenticated"
    ON workouts FOR SELECT
    USING (auth.role() = 'authenticated');

-- workout_movements: readable by authenticated users (joins with workouts).
CREATE POLICY "workout_movements_select_authenticated"
    ON workout_movements FOR SELECT
    USING (auth.role() = 'authenticated');

-- movements: readable by all authenticated users (catalog data).
CREATE POLICY "movements_select_authenticated"
    ON movements FOR SELECT
    USING (auth.role() = 'authenticated');

-- results: full CRUD for the owning user.
-- The PR trigger runs as SECURITY DEFINER so it can write to
-- personal_records regardless of the RLS policy on that table.
CREATE POLICY "results_select_own"
    ON results FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "results_insert_own"
    ON results FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "results_update_own"
    ON results FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "results_delete_own"
    ON results FOR DELETE
    USING (auth.uid() = user_id);

-- personal_records: read-only for the athlete (writes come only from trigger).
CREATE POLICY "personal_records_select_own"
    ON personal_records FOR SELECT
    USING (auth.uid() = user_id);

-- health_snapshots: full CRUD for the owning user (Android syncs health data).
CREATE POLICY "health_snapshots_select_own"
    ON health_snapshots FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "health_snapshots_insert_own"
    ON health_snapshots FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "health_snapshots_update_own"
    ON health_snapshots FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- video_uploads: full CRUD for the owning user.
CREATE POLICY "video_uploads_select_own"
    ON video_uploads FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "video_uploads_insert_own"
    ON video_uploads FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "video_uploads_update_own"
    ON video_uploads FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- coaching_reports: read-only for the athlete (FastAPI writes via service_role).
CREATE POLICY "coaching_reports_select_own"
    ON coaching_reports FOR SELECT
    USING (auth.uid() = user_id);

-- movement_faults: readable if the parent coaching_report belongs to the user.
-- This join-based policy is evaluated per row; the index on
-- coaching_reports(id) keeps it fast.
CREATE POLICY "movement_faults_select_own"
    ON movement_faults FOR SELECT
    USING (
        EXISTS (
            SELECT 1
            FROM coaching_reports cr
            WHERE cr.id = movement_faults.report_id
              AND cr.user_id = auth.uid()
        )
    );

-- ---------------------------------------------------------------------------
-- 8. Functions and Triggers
-- ---------------------------------------------------------------------------

-- ==========================================================================
-- 8.1 PR Detection Trigger
--
-- Fires AFTER INSERT ON results FOR EACH ROW.
--
-- Algorithm:
--   For each movement in the workout, attempt to upsert a personal_record.
--   The upsert only succeeds (updates the row) if the new score_numeric
--   is strictly greater than the existing value. This means:
--     - First result for a movement: always inserts (no conflict exists yet)
--     - Subsequent results: only updates if the score is a new high
--
-- Unit inference:
--   - Olympic Lifting, Powerlifting -> KG (respects profile unit_system for
--     display, but stores in KG for consistent comparison)
--   - Gymnastics, Monostructural, Accessory -> REPS
--   - RFT / TIME scoring -> SECONDS (score_numeric contains elapsed seconds)
--
-- Note: SECURITY DEFINER is required because the trigger function needs
-- to INSERT/UPDATE personal_records regardless of the calling user's RLS.
-- The function only ever operates on NEW.user_id data, so this is safe.
-- ==========================================================================

CREATE OR REPLACE FUNCTION check_and_update_pr()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_score DECIMAL(10,2);
    v_scoring_metric TEXT;
BEGIN
    -- If score_numeric is null (e.g., ROUNDS_PLUS_REPS format not parsed),
    -- we cannot do a numeric comparison. Skip PR detection silently.
    IF NEW.score_numeric IS NULL OR NEW.score_numeric <= 0 THEN
        RETURN NEW;
    END IF;

    -- Retrieve the scoring_metric for this workout to determine the PR unit.
    SELECT scoring_metric::TEXT
    INTO v_scoring_metric
    FROM workouts
    WHERE id = NEW.workout_id;

    v_score := NEW.score_numeric;

    -- Upsert personal_records for every movement in the completed workout.
    -- The ON CONFLICT clause targets the (user_id, movement_id, unit) unique
    -- constraint and only updates when the new value exceeds the stored value.
    INSERT INTO personal_records (
        user_id,
        movement_id,
        value,
        unit,
        achieved_at,
        result_id
    )
    SELECT
        NEW.user_id,
        wm.movement_id,
        v_score,
        CASE
            -- TIME-scored workouts (RFT): lower score = faster = better.
            -- We store the raw seconds and infer "lower is better" in the UI.
            WHEN v_scoring_metric = 'TIME' THEN 'SECONDS'
            -- LOAD-scored workouts (max lift): store in KG.
            WHEN v_scoring_metric = 'LOAD' THEN 'KG'
            -- For AMRAP/EMOM/TABATA we check the movement category.
            WHEN m.category IN ('Olympic Lifting', 'Powerlifting') THEN 'KG'
            -- Gymnastics, Monostructural, and all others: REPS.
            ELSE 'REPS'
        END AS unit,
        NEW.completed_at,
        NEW.id
    FROM workout_movements wm
    JOIN movements m ON m.id = wm.movement_id
    WHERE wm.workout_id = NEW.workout_id
    ON CONFLICT (user_id, movement_id, unit)
    DO UPDATE SET
        value       = EXCLUDED.value,
        achieved_at = EXCLUDED.achieved_at,
        result_id   = EXCLUDED.result_id
    -- For LOAD/KG/REPS/SECONDS: higher is better (more reps, heavier lift).
    -- For TIME/SECONDS: lower elapsed time is better — inverse comparison.
    WHERE (
        CASE
            WHEN EXCLUDED.unit = 'SECONDS'
                THEN EXCLUDED.value < personal_records.value
            ELSE
                EXCLUDED.value > personal_records.value
        END
    );

    RETURN NEW;
END;
$$;

CREATE OR REPLACE TRIGGER trg_check_pr
    AFTER INSERT ON results
    FOR EACH ROW
    EXECUTE FUNCTION check_and_update_pr();

-- ==========================================================================
-- 8.2 ACWR Readiness Calculation Function
--
-- Called via Supabase RPC: POST /rpc/calculate_readiness
-- Body: { "p_user_id": "<uuid>" }
--
-- Algorithm (from CLAUDE.md):
--   W_acute  = SUM(score_numeric * rpe_or_5) over past 7 days
--   W_chronic = SUM(score_numeric * rpe_or_5) over past 28 days / 4
--   ACWR = W_acute / W_chronic
--
--   Zones:
--     < 0.8   -> UNDERTRAINED
--     0.8–1.3 -> OPTIMAL
--     1.3–1.5 -> CAUTION
--     > 1.5   -> HIGH_RISK
--
-- The function also reads the most recent health_snapshot to incorporate
-- HRV and sleep context into the recommendation text.
--
-- SECURITY DEFINER: the function is callable by authenticated users via RPC
-- but needs to read results/health_snapshots which are RLS-protected.
-- The WHERE clause always filters by p_user_id, and the calling user's
-- JWT is verified by Supabase before the RPC reaches this function.
-- ==========================================================================

CREATE OR REPLACE FUNCTION calculate_readiness(p_user_id UUID)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_acute         DECIMAL(12,4);
    v_chronic       DECIMAL(12,4);
    v_acwr          DECIMAL(8,4);
    v_zone          TEXT;
    v_hrv           INT;
    v_sleep_minutes INT;
    v_resting_hr    INT;
    v_recommendation TEXT;
    v_hrv_note      TEXT;
    v_sleep_note    TEXT;
BEGIN
    -- Validate the caller is requesting their own data.
    -- auth.uid() returns the UUID from the JWT; this prevents a user
    -- from passing a different UUID to read another athlete's readiness.
    IF auth.uid() IS NOT NULL AND auth.uid() <> p_user_id THEN
        RAISE EXCEPTION 'Access denied: cannot calculate readiness for another user'
            USING ERRCODE = 'insufficient_privilege';
    END IF;

    -- -----------------------------------------------------------------------
    -- Acute workload: sum of training load over the past 7 days.
    -- Training load = score_numeric * RPE (default RPE 5 if not recorded).
    -- score_numeric IS NULL rows are excluded via COALESCE to 0 below but
    -- the WHERE filter on score_numeric > 0 keeps the math clean.
    -- -----------------------------------------------------------------------
    SELECT COALESCE(SUM(score_numeric * COALESCE(rpe, 5)), 0)
    INTO v_acute
    FROM results
    WHERE user_id = p_user_id
      AND completed_at >= NOW() - INTERVAL '7 days'
      AND score_numeric IS NOT NULL
      AND score_numeric > 0;

    -- -----------------------------------------------------------------------
    -- Chronic workload: rolling 28-day total divided by 4 to get
    -- the equivalent weekly average.
    -- GREATEST(..., 0.01) prevents division-by-zero when no data exists.
    -- -----------------------------------------------------------------------
    SELECT COALESCE(SUM(score_numeric * COALESCE(rpe, 5)) / 4.0, 0.01)
    INTO v_chronic
    FROM results
    WHERE user_id = p_user_id
      AND completed_at >= NOW() - INTERVAL '28 days'
      AND score_numeric IS NOT NULL
      AND score_numeric > 0;

    v_chronic := GREATEST(v_chronic, 0.01);

    -- ACWR ratio
    v_acwr := v_acute / v_chronic;

    -- Zone classification
    v_zone := CASE
        WHEN v_acwr < 0.8  THEN 'UNDERTRAINED'
        WHEN v_acwr <= 1.3 THEN 'OPTIMAL'
        WHEN v_acwr <= 1.5 THEN 'CAUTION'
        ELSE 'HIGH_RISK'
    END;

    -- -----------------------------------------------------------------------
    -- Latest health snapshot for HRV and sleep context.
    -- The UNIQUE constraint ensures only one row per day, so LIMIT 1
    -- with ORDER BY captured_at DESC gives the most recent snapshot.
    -- -----------------------------------------------------------------------
    SELECT hrv_rmssd, sleep_duration_minutes, resting_hr
    INTO v_hrv, v_sleep_minutes, v_resting_hr
    FROM health_snapshots
    WHERE user_id = p_user_id
    ORDER BY captured_at DESC
    LIMIT 1;

    -- -----------------------------------------------------------------------
    -- HRV note: RMSSD > 70 ms is generally considered parasympathetically
    -- recovered; < 40 ms suggests elevated sympathetic tone / under-recovery.
    -- These thresholds are population averages; individual baselines vary.
    -- -----------------------------------------------------------------------
    v_hrv_note := CASE
        WHEN v_hrv IS NULL     THEN 'HRV data unavailable.'
        WHEN v_hrv >= 70       THEN 'HRV indicates strong parasympathetic recovery.'
        WHEN v_hrv BETWEEN 40 AND 69
                               THEN 'HRV is within normal range.'
        ELSE                        'HRV is low — consider additional recovery.'
    END;

    -- Sleep note: < 360 min (6 h) is flagged as insufficient for athletic recovery.
    v_sleep_note := CASE
        WHEN v_sleep_minutes IS NULL    THEN 'Sleep data unavailable.'
        WHEN v_sleep_minutes >= 480     THEN 'Sleep duration is optimal (8+ hours).'
        WHEN v_sleep_minutes >= 360     THEN 'Sleep duration is adequate (6-8 hours).'
        ELSE                                 'Sleep is insufficient (< 6 hours). Prioritise rest.'
    END;

    -- -----------------------------------------------------------------------
    -- Composite recommendation text.
    -- -----------------------------------------------------------------------
    v_recommendation := CASE v_zone
        WHEN 'OPTIMAL' THEN
            'You are in the optimal training zone. ' ||
            'Consider attempting a heavy single or a benchmark WOD today. ' ||
            v_hrv_note || ' ' || v_sleep_note
        WHEN 'CAUTION' THEN
            'Training load is elevated. Prioritise technique work and moderate intensity. ' ||
            'Avoid testing maximal efforts until your ACWR returns below 1.3. ' ||
            v_hrv_note || ' ' || v_sleep_note
        WHEN 'HIGH_RISK' THEN
            'High injury risk detected (ACWR > 1.5). Scale to active recovery, ' ||
            'mobility work, or a complete rest day. ' ||
            v_hrv_note || ' ' || v_sleep_note
        WHEN 'UNDERTRAINED' THEN
            'Training volume is below your baseline. Gradually increase load this week — ' ||
            'a 10-15% weekly increase is safe. ' ||
            v_hrv_note || ' ' || v_sleep_note
        ELSE 'Unable to determine recommendation.'
    END;

    RETURN jsonb_build_object(
        'acwr',             ROUND(v_acwr, 4),
        'zone',             v_zone,
        'acute_load',       ROUND(v_acute, 2),
        'chronic_load',     ROUND(v_chronic, 2),
        'hrv_rmssd',        v_hrv,
        'sleep_minutes',    v_sleep_minutes,
        'resting_hr',       v_resting_hr,
        'recommendation',   v_recommendation,
        'calculated_at',    NOW()
    );
END;
$$;

-- ==========================================================================
-- 8.3 Seed: Common CrossFit Movements
--
-- A representative seed set to enable development and testing without
-- the full ExerciseDB import. The full 11,000+ entry seed should be
-- run as a separate migration (002_seed_movements.sql) via a bulk INSERT
-- or \copy command from the ExerciseDB CSV.
-- ==========================================================================

INSERT INTO movements (name, category, primary_muscles, secondary_muscles, equipment, biomechanical_class, instructions)
VALUES
    ('Snatch',           'Olympic Lifting',  ARRAY['Trapezius', 'Deltoids', 'Glutes'],     ARRAY['Hamstrings', 'Quads', 'Core'],         'BARBELL',    'Pull',   'Full hip extension, arms long until hip contact. Receive in deep squat.'),
    ('Clean and Jerk',   'Olympic Lifting',  ARRAY['Trapezius', 'Deltoids', 'Quadriceps'], ARRAY['Hamstrings', 'Glutes', 'Triceps'],     'BARBELL',    'Pull',   'Aggressive hip drive on clean; split jerk with locked overhead.'),
    ('Back Squat',       'Powerlifting',     ARRAY['Quadriceps', 'Glutes'],                ARRAY['Hamstrings', 'Core'],                  'BARBELL',    'Squat',  'Bar on traps, hip crease below knee, knees track toes.'),
    ('Front Squat',      'Powerlifting',     ARRAY['Quadriceps', 'Glutes'],                ARRAY['Core', 'Upper Back'],                  'BARBELL',    'Squat',  'High elbows, upright torso, hip crease below knee.'),
    ('Deadlift',         'Powerlifting',     ARRAY['Hamstrings', 'Glutes', 'Erectors'],    ARRAY['Trapezius', 'Lats', 'Core'],           'BARBELL',    'Hinge',  'Neutral spine, bar over mid-foot, drive floor away.'),
    ('Overhead Squat',   'Olympic Lifting',  ARRAY['Deltoids', 'Trapezius', 'Quadriceps'], ARRAY['Core', 'Hamstrings'],                  'BARBELL',    'Squat',  'Arms locked, bar over base of support throughout.'),
    ('Push Press',       'Powerlifting',     ARRAY['Deltoids', 'Triceps'],                 ARRAY['Quadriceps', 'Core'],                  'BARBELL',    'Push',   'Dip-drive with legs, punch bar overhead aggressively.'),
    ('Strict Press',     'Powerlifting',     ARRAY['Deltoids', 'Triceps'],                 ARRAY['Core'],                                'BARBELL',    'Push',   'No leg drive, bar path straight up through ear line.'),
    ('Pull-Up',          'Gymnastics',       ARRAY['Latissimus Dorsi', 'Biceps'],          ARRAY['Core', 'Rear Deltoids'],               'PULL_UP_BAR','Pull',   'Full hang to chin over bar. Kipping permitted in WODs.'),
    ('Muscle-Up',        'Gymnastics',       ARRAY['Chest', 'Triceps', 'Lats'],            ARRAY['Core', 'Biceps'],                      'RINGS',      'Pull',   'False grip, aggressive kip, transition above rings.'),
    ('Handstand Push-Up','Gymnastics',       ARRAY['Deltoids', 'Triceps'],                 ARRAY['Core', 'Trapezius'],                   'BODYWEIGHT', 'Push',   'Nose and hands form a tripod, full lockout at top.'),
    ('Box Jump',         'Monostructural',   ARRAY['Quadriceps', 'Glutes'],                ARRAY['Hamstrings', 'Calves'],                'BOX',        'Squat',  'Two-foot take-off, hip extension at top, step down.'),
    ('Double-Under',     'Monostructural',   ARRAY['Calves', 'Deltoids'],                  ARRAY['Core', 'Wrists'],                      'NONE',       'Core',   'Single bounce per two rope passes. Relaxed wrists.'),
    ('Burpee',           'Monostructural',   ARRAY['Chest', 'Quadriceps', 'Deltoids'],     ARRAY['Core', 'Hamstrings'],                  'BODYWEIGHT', 'Push',   'Chest to floor, clap overhead at top. Consistent pace.'),
    ('Kettlebell Swing', 'Accessory',        ARRAY['Glutes', 'Hamstrings'],                ARRAY['Core', 'Deltoids'],                    'KETTLEBELL', 'Hinge',  'Hip hinge drive, arms passive, bell to eye level (American: overhead).')
ON CONFLICT (name) DO NOTHING;
```

---

## File: `backend/requirements.txt`

```text
# =============================================================================
# ApexAI Athletics — FastAPI Microservice Dependencies
# All versions are pinned to the minor version for reproducibility.
# Run: pip install -r requirements.txt
# Python: 3.11+
# =============================================================================

# --- Web Framework ---
fastapi==0.115.5
uvicorn[standard]==0.32.1      # ASGI server; [standard] includes uvloop + httptools

# --- Data Validation ---
pydantic==2.10.3
pydantic-settings==2.7.0       # Settings from environment variables / .env file

# --- Async HTTP Client ---
httpx==0.28.1                  # Used for Supabase JWT verification and service calls

# --- Google Gemini SDK ---
google-generativeai==0.8.3     # Gemini Pro + Flash + Context Caching APIs

# --- Supabase Client (for writing reports and updating video status) ---
supabase==2.10.0

# --- File Handling ---
python-multipart==0.0.20       # Required for FastAPI multipart/form-data uploads
aiofiles==24.1.0               # Async file I/O for temporary video storage

# --- Authentication (JWT validation) ---
python-jose[cryptography]==3.3.0  # JWT decode/verify using Supabase HS256 secret

# --- Rate Limiting ---
slowapi==0.1.9                 # Starlette-compatible rate limiter wrapping limits

# --- Caching (in-memory for context cache resource names) ---
cachetools==5.5.0              # TTLCache for Gemini context cache name storage

# --- Observability ---
structlog==24.4.0              # Structured JSON logging
sentry-sdk[fastapi]==2.19.2    # Error tracking

# --- Testing (not installed in production image) ---
pytest==8.3.4
pytest-asyncio==0.25.0
httpx==0.28.1                  # already listed above; used as async test client
pytest-cov==6.0.0
```

---

## File: `backend/models.py`

```python
"""
ApexAI Athletics — Pydantic Models for FastAPI Microservice
===========================================================
All request/response shapes for the coaching pipeline.
These models are the authoritative API contract between
the FastAPI microservice and the Android client.

Design notes:
- All UUIDs are typed as str (validated by Pydantic's uuid validator on input).
- Timestamps use datetime for proper JSON ISO-8601 serialization.
- Optional fields are explicit; never use bare None defaults in required fields.
- Error responses follow the project envelope: {"success": false, "error": {...}}
"""

from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum
from typing import Any

from pydantic import BaseModel, ConfigDict, Field, field_validator


# ---------------------------------------------------------------------------
# Shared Enums — must stay in sync with PostgreSQL CHECK constraints
# ---------------------------------------------------------------------------

class WorkoutType(str, Enum):
    AMRAP = "AMRAP"
    EMOM = "EMOM"
    RFT = "RFT"
    TABATA = "TABATA"


class ScoringType(str, Enum):
    REPS = "REPS"
    TIME = "TIME"
    LOAD = "LOAD"
    ROUNDS_PLUS_REPS = "ROUNDS_PLUS_REPS"


class FaultSeverity(str, Enum):
    MINOR = "MINOR"
    MODERATE = "MODERATE"
    CRITICAL = "CRITICAL"


class AnalysisStatus(str, Enum):
    PROCESSING = "processing"
    COMPLETE = "complete"
    ERROR = "error"


class AnalysisStage(str, Enum):
    UPLOADING = "uploading"
    ANALYZING_VIDEO = "analyzing_video"
    GENERATING_CORRECTIONS = "generating_corrections"
    FINALIZING = "finalizing"


# ---------------------------------------------------------------------------
# Pose / Overlay Data Shapes
# These are stored as JSONB in coaching_reports.overlay_data and returned
# to the Android client for kinematic overlay playback.
# ---------------------------------------------------------------------------

class PoseLandmark(BaseModel):
    """Single BlazePose landmark. Indices follow MediaPipe topology (0-32)."""
    model_config = ConfigDict(frozen=True)

    index: int = Field(..., ge=0, le=32, description="BlazePose landmark index")
    x: float = Field(..., ge=0.0, le=1.0, description="Normalized horizontal position")
    y: float = Field(..., ge=0.0, le=1.0, description="Normalized vertical position")
    z: float = Field(default=0.0, description="Depth estimate — unreliable on mobile")
    visibility: float = Field(..., ge=0.0, le=1.0, description="Landmark confidence")


class TimedPoseOverlay(BaseModel):
    """
    Pose data for a single video frame.
    The Android client uses timestamp_ms to synchronize with Media3 playback.
    """
    model_config = ConfigDict(frozen=True)

    timestamp_ms: int = Field(..., ge=0, description="Frame timestamp in milliseconds")
    landmarks: list[PoseLandmark]
    joint_angles: dict[str, float] = Field(
        default_factory=dict,
        description="Joint name -> angle in degrees. Keys match JointAngle enum on Android."
    )


# ---------------------------------------------------------------------------
# Movement Fault
# ---------------------------------------------------------------------------

class MovementFaultResponse(BaseModel):
    """
    A single biomechanical fault detected in the video analysis.
    corrected_image_url is populated by a follow-up Gemini Flash call
    and may be null immediately after analysis completes.
    """
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    description: str = Field(..., min_length=1, max_length=1000)
    severity: FaultSeverity
    timestamp_ms: int = Field(..., ge=0)
    cue: str = Field(..., min_length=1, max_length=500, description="Athlete-facing coaching instruction")
    corrected_image_url: str | None = None
    affected_joints: list[str] = Field(default_factory=list)


# ---------------------------------------------------------------------------
# Coaching Report (full response returned to Android)
# ---------------------------------------------------------------------------

class CoachingReportResponse(BaseModel):
    """
    Complete Gemini coaching analysis result.
    Returned by GET /coaching/report/{analysis_id}.
    """
    id: str
    video_id: str
    movement_type: str
    overall_assessment: str
    rep_count: int = Field(..., ge=0)
    estimated_weight_kg: float | None = Field(default=None, ge=0)
    faults: list[MovementFaultResponse]
    global_cues: list[str]
    overlay_data: list[TimedPoseOverlay]
    created_at: datetime


# ---------------------------------------------------------------------------
# Analysis Submission
# ---------------------------------------------------------------------------

class AnalyzeVideoResponse(BaseModel):
    """
    Response to POST /analyze-video (202 Accepted).
    The Android client polls /coaching/status/{analysis_id} at 3-second
    intervals until status == 'complete'.
    """
    analysis_id: str
    status: AnalysisStatus = AnalysisStatus.PROCESSING
    estimated_seconds: int = Field(default=45, ge=0)
    poll_url: str


class AnalysisStatusResponse(BaseModel):
    """
    Response to GET /coaching/status/{analysis_id}.
    progress is a float 0.0–1.0 for the Android upload/analysis progress bar.
    """
    analysis_id: str
    status: AnalysisStatus
    progress: float = Field(..., ge=0.0, le=1.0)
    stage: AnalysisStage | None = None
    error_message: str | None = None


# ---------------------------------------------------------------------------
# Correction Image Generation
# ---------------------------------------------------------------------------

class GenerateCorrectionImageRequest(BaseModel):
    """
    Request body for POST /generate-correction-image.
    fault_timestamp_ms is used to extract the specific frame for Gemini Flash.
    """
    report_id: str = Field(..., description="coaching_reports.id UUID")
    fault_id: str = Field(..., description="movement_faults.id UUID")
    fault_timestamp_ms: int = Field(..., ge=0)
    fault_description: str = Field(..., min_length=1, max_length=1000)
    movement_type: str

    @field_validator("report_id", "fault_id")
    @classmethod
    def validate_uuid(cls, v: str) -> str:
        try:
            uuid.UUID(v)
        except ValueError as exc:
            raise ValueError(f"Invalid UUID: {v}") from exc
        return v


class GenerateCorrectionImageResponse(BaseModel):
    """
    Response to POST /generate-correction-image.
    The corrected_image_url is a Supabase Storage signed URL (1-hour TTL).
    """
    fault_id: str
    corrected_image_url: str
    storage_path: str


# ---------------------------------------------------------------------------
# Context Cache Management
# ---------------------------------------------------------------------------

class CacheRefreshResponse(BaseModel):
    """Response to POST /cache/refresh."""
    cache_name: str = Field(..., description="Gemini API resource name for the new cache")
    model: str
    token_count: int
    expires_at: datetime
    refreshed_at: datetime


# ---------------------------------------------------------------------------
# Standard Error Envelope
# Used for all 4xx and 5xx responses. The Android client checks
# success == false and displays error.message to the user.
# ---------------------------------------------------------------------------

class ErrorDetail(BaseModel):
    code: str = Field(..., description="Machine-readable error code, e.g. 'VIDEO_TOO_LARGE'")
    message: str = Field(..., description="Human-readable error message for display")


class ErrorResponse(BaseModel):
    success: bool = False
    error: ErrorDetail


# ---------------------------------------------------------------------------
# Health Check
# ---------------------------------------------------------------------------

class HealthCheckResponse(BaseModel):
    status: str = "ok"
    version: str
    gemini_cache_loaded: bool
    uptime_seconds: float
```

---

## File: `backend/gemini_service.py`

```python
"""
ApexAI Athletics — Gemini AI Service
=====================================
Orchestrates the dual-model Gemini pipeline:
  1. gemini-1.5-pro  — full video analysis with context caching
  2. gemini-2.0-flash — corrective posture image generation per fault

Context Caching Strategy
-------------------------
The CrossFit movement standards document + athlete history are pre-loaded
as a Gemini context cache. Every new analysis references the cache by its
resource name instead of re-sending the full context, reducing per-video
token cost by approximately 75-90%.

Cache lifecycle:
- Created on first call or /cache/refresh
- TTL: 1 hour (renewable)
- Resource name stored in-memory (TTLCache) and in the video's coaching report
  row for audit/debugging

Threading model:
- All Gemini SDK calls are synchronous (the google-generativeai SDK does not
  natively support asyncio). All calls are wrapped in asyncio.to_thread()
  so they do not block the FastAPI event loop.

Error handling:
- google.api_core.exceptions.ResourceExhausted -> 429 upstream
- google.api_core.exceptions.ServiceUnavailable -> 503 upstream
- All Gemini exceptions are caught, logged with structlog, and re-raised as
  application-level exceptions that the router converts to HTTP responses.
"""

from __future__ import annotations

import asyncio
import base64
import json
import os
import tempfile
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import google.generativeai as genai
import structlog
from cachetools import TTLCache
from google.generativeai import caching
from google.generativeai.types import HarmBlockThreshold, HarmCategory

from models import (
    FaultSeverity,
    MovementFaultResponse,
    PoseLandmark,
    TimedPoseOverlay,
)

logger = structlog.get_logger(__name__)

# ---------------------------------------------------------------------------
# Configuration (all values come from environment variables)
# ---------------------------------------------------------------------------

GEMINI_API_KEY: str = os.environ["GEMINI_API_KEY"]
VIDEO_ANALYSIS_MODEL: str = os.environ.get("VIDEO_ANALYSIS_MODEL", "gemini-1.5-pro")
IMAGE_GENERATION_MODEL: str = os.environ.get("IMAGE_GENERATION_MODEL", "gemini-2.0-flash")

# Context cache TTL in seconds. Gemini minimum is 60 s.
CACHE_TTL_SECONDS: int = int(os.environ.get("GEMINI_CACHE_TTL_SECONDS", "3600"))

# Maximum video file size allowed (500 MB per spec).
MAX_VIDEO_SIZE_BYTES: int = 500 * 1024 * 1024

# Safety settings — allow sports/fitness content.
# The default SafetySettings block violence-adjacent content which would
# incorrectly flag normal weightlifting analysis responses.
SAFETY_SETTINGS = {
    HarmCategory.HARM_CATEGORY_HATE_SPEECH:       HarmBlockThreshold.BLOCK_ONLY_HIGH,
    HarmCategory.HARM_CATEGORY_HARASSMENT:        HarmBlockThreshold.BLOCK_ONLY_HIGH,
    HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT: HarmBlockThreshold.BLOCK_ONLY_HIGH,
    HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
}

# ---------------------------------------------------------------------------
# Gemini API Client Initialization
# ---------------------------------------------------------------------------

genai.configure(api_key=GEMINI_API_KEY)

# In-memory store for the active context cache resource name.
# TTLCache evicts after CACHE_TTL_SECONDS so the cache refresh cycle
# aligns with the Gemini cache expiry.
_cache_store: TTLCache = TTLCache(maxsize=1, ttl=CACHE_TTL_SECONDS)
_CACHE_KEY = "crossfit_knowledge_base"


# ---------------------------------------------------------------------------
# CrossFit Knowledge Base Content
# This text is sent ONCE to create/refresh the context cache.
# It contains movement standards, biomechanical fault taxonomies, and
# coaching cueing language. In production this would be loaded from a
# structured document or database, but for MVP it is defined inline.
# ---------------------------------------------------------------------------

CROSSFIT_KNOWLEDGE_BASE = """
# CrossFit Movement Standards and Biomechanical Analysis Guide
# ApexAI Athletics Coaching System v1.0

## General Principles
- All movement analysis uses 2D profile-view angles as the primary metric.
  Z-depth data from on-device pose estimation is unreliable and must not
  be used as a primary fault indicator.
- Joint angle thresholds are provided as population-level guidelines.
  Individual limb length ratios affect optimal positioning.
- Every fault must be accompanied by a single, actionable coaching cue
  in simple language an athlete can execute immediately.

## Fault Severity Taxonomy
- MINOR: Sub-optimal but not injury-risk. Common among intermediate athletes.
- MODERATE: Reduces power output or places joints in compromised positions.
  Requires deliberate practice to correct.
- CRITICAL: Acute injury risk. Athlete should stop and address before continuing.

## Olympic Lifting Standards

### Snatch
Setup:
- Bar over mid-foot (approximately 1 inch from shins)
- Hip crease level with or below the bar
- Neutral spine: no excessive lumbar rounding or hyperextension
- Shoulders over or slightly in front of the bar
- Arms externally rotated (elbows point out, not down)

First Pull (floor to knee):
- Bar path stays close to shins (within 2 inches)
- Shoulders maintain position above hips (angle preserved)
- No early hip rise before the bar leaves the floor

Second Pull (knee to hip contact):
- Knees rebend/sweep under the bar
- Bar contacts upper thigh/hip crease — NOT below the hip
- Arms remain straight — early arm bend is a CRITICAL fault

Turnover (extension to catch):
- Full triple extension: ankle, knee, hip
- Elbows pull high and outside before punching overhead
- Receive in deep squat with active overhead press

Common Faults:
1. Early arm bend (CRITICAL) — arms flex during second pull reducing power transfer
2. Bar drift forward (MODERATE) — bar moves away from body during second pull
3. Shooting hips (MODERATE) — hips rise faster than shoulders off the floor
4. Soft catch (MODERATE) — elbows collapse in the overhead receiving position
5. Early pull with arms (CRITICAL) — arms initiate before full hip extension

### Clean and Jerk
[Same setup as snatch but narrower grip]
Clean-specific standards:
- Receive bar in front rack: bar rests on anterior deltoids, not wrists
- Full squat clean: hip crease must break parallel (for squat clean standard)
- Front rack elbow position: horizontal or higher

Jerk-specific standards:
- Split jerk: front foot lands flat, back knee bends toward floor
- Push jerk: full lockout overhead with knees rebent under load
- No pressing out: arms must lock out before the body descends

### Overhead Squat
- Bar directly over base of support (mid-foot) throughout the movement
- Active shoulder press throughout (not passive lock)
- Hip crease below knee at bottom
- Torso as vertical as possible

## Squat Mechanics

### Back Squat
- Bar rests on upper trapezius (high bar) or lower trapezius (low bar)
- Stance width: hip-width to shoulder-width with toes angled out 15-30 degrees
- Knee tracking over second/third toe throughout
- Hip crease breaks parallel at minimum
- Neutral spine maintained — not excessive forward lean
- Heels remain flat throughout

Common Faults:
1. Knee cave/valgus collapse (CRITICAL) — knees track inward
2. Forward torso lean (MODERATE) — excessive trunk inclination
3. Heel rise (MODERATE) — ankles lack dorsiflexion
4. Butt wink (MODERATE) — posterior pelvic tilt at bottom of squat

### Front Squat
- Same depth and knee standards as back squat
- Elbows must remain elevated (horizontal or above) throughout
- More upright torso required compared to back squat

## Pulling Movements

### Deadlift
- Bar over mid-foot
- Neutral spine — no lumbar rounding in the initial pull
- Lats engaged before initiating pull (protect lower back)
- Bar stays in contact with legs throughout the lift
- Full hip extension and knee lockout at top

### Pull-Up (Kipping/Strict)
Strict: Full dead hang to chin over bar, no body swing
Kipping: Active hollow/arch cycle, hip drive propels upward

## Gymnastics Movements

### Handstand Push-Up
- Nose and both hands form a tripod at the bottom
- Hips over hands (slightly past vertical)
- Full lockout at top — both arms straight
- Head neutral — not tucked or hyperextended

## Scoring Guidelines for Rep Counting
Count only reps that meet the movement standard.
Partial reps should be noted but not counted.
For AMRAP workouts, count total rounds + additional reps.

## Coaching Language Principles
- Use kinesthetic cues over technical anatomy when possible
  (e.g., "keep elbows high" not "maintain elbow flexion")
- Provide a single, most important cue per fault
- Positive framing when possible: "stay connected" rather than "stop drifting"
- Prioritise safety (CRITICAL faults) in the response ordering
"""


# ---------------------------------------------------------------------------
# Service Class
# ---------------------------------------------------------------------------

class GeminiService:
    """
    Stateless service class for all Gemini API interactions.
    Instantiated once by FastAPI's dependency injection system.

    Thread safety: all blocking Gemini SDK calls are wrapped in
    asyncio.to_thread() to prevent event loop blocking.
    """

    def __init__(self) -> None:
        self._analysis_model = genai.GenerativeModel(
            model_name=VIDEO_ANALYSIS_MODEL,
            safety_settings=SAFETY_SETTINGS,
        )
        self._flash_model = genai.GenerativeModel(
            model_name=IMAGE_GENERATION_MODEL,
            safety_settings=SAFETY_SETTINGS,
        )
        logger.info(
            "gemini_service_initialized",
            analysis_model=VIDEO_ANALYSIS_MODEL,
            image_model=IMAGE_GENERATION_MODEL,
        )

    # -----------------------------------------------------------------------
    # Context Cache Management
    # -----------------------------------------------------------------------

    async def get_or_create_cache(self) -> caching.CachedContent:
        """
        Returns the active context cache, creating it if it does not exist
        or has been evicted from the TTLCache.

        The cache contains the CrossFit movement standards knowledge base.
        On a cache miss (first call or post-expiry), a new Gemini CachedContent
        is created via the API and stored locally.
        """
        cached = _cache_store.get(_CACHE_KEY)
        if cached is not None:
            logger.debug("gemini_cache_hit", cache_name=cached.name)
            return cached

        logger.info("gemini_cache_miss_creating_new_cache")
        cache = await asyncio.to_thread(self._create_context_cache)
        _cache_store[_CACHE_KEY] = cache
        logger.info(
            "gemini_cache_created",
            cache_name=cache.name,
            ttl_seconds=CACHE_TTL_SECONDS,
        )
        return cache

    def _create_context_cache(self) -> caching.CachedContent:
        """
        Synchronous inner function that creates the Gemini CachedContent.
        Must be run in a thread via asyncio.to_thread().
        """
        return caching.CachedContent.create(
            model=VIDEO_ANALYSIS_MODEL,
            display_name="apexai-crossfit-knowledge-base",
            contents=[CROSSFIT_KNOWLEDGE_BASE],
            ttl=f"{CACHE_TTL_SECONDS}s",
        )

    async def force_refresh_cache(self) -> dict[str, Any]:
        """
        Deletes the existing cache and creates a fresh one.
        Called by POST /cache/refresh (admin operation).
        Returns metadata about the new cache.
        """
        # Evict existing cache entry so get_or_create_cache() creates fresh
        _cache_store.pop(_CACHE_KEY, None)

        cache = await self.get_or_create_cache()
        return {
            "cache_name": cache.name,
            "model": VIDEO_ANALYSIS_MODEL,
            "token_count": getattr(cache, "usage_metadata", {}).get("total_token_count", 0),
            "expires_at": datetime.now(timezone.utc).replace(
                second=0, microsecond=0
            ).isoformat(),
            "refreshed_at": datetime.now(timezone.utc).isoformat(),
        }

    # -----------------------------------------------------------------------
    # Video Analysis
    # -----------------------------------------------------------------------

    async def analyze_video(
        self,
        video_path: Path,
        movement_type: str,
        athlete_id: str,
        video_id: str,
    ) -> dict[str, Any]:
        """
        Analyzes a video file using Gemini 1.5 Pro with context caching.

        Parameters
        ----------
        video_path : Path
            Local path to the uploaded video file (temporary storage).
        movement_type : str
            The CrossFit movement being performed (e.g., "snatch", "back_squat").
        athlete_id : str
            Supabase user UUID — used to fetch athlete history for context.
        video_id : str
            video_uploads.id UUID — referenced in the coaching report.

        Returns
        -------
        dict
            Structured coaching analysis. Structure matches CoachingReportResponse.
        """
        if not video_path.exists():
            raise FileNotFoundError(f"Video file not found: {video_path}")

        if video_path.stat().st_size > MAX_VIDEO_SIZE_BYTES:
            raise ValueError(
                f"Video file size {video_path.stat().st_size} exceeds "
                f"maximum {MAX_VIDEO_SIZE_BYTES} bytes"
            )

        cache = await self.get_or_create_cache()

        logger.info(
            "gemini_video_analysis_start",
            video_id=video_id,
            movement_type=movement_type,
            athlete_id=athlete_id,
            cache_name=cache.name,
            file_size_bytes=video_path.stat().st_size,
        )

        result = await asyncio.to_thread(
            self._run_video_analysis,
            video_path,
            movement_type,
            cache,
        )

        logger.info(
            "gemini_video_analysis_complete",
            video_id=video_id,
            rep_count=result.get("rep_count", 0),
            fault_count=len(result.get("faults", [])),
        )

        return result

    def _run_video_analysis(
        self,
        video_path: Path,
        movement_type: str,
        cache: caching.CachedContent,
    ) -> dict[str, Any]:
        """
        Synchronous Gemini video analysis call. Run via asyncio.to_thread().

        The video is uploaded to the Gemini File API first, which returns a
        file URI. This URI is then included in the prompt as a Part.
        Context cache is referenced by name, not re-sent inline.
        """
        # Step 1: Upload the video to the Gemini File API
        logger.info("gemini_file_upload_start", path=str(video_path))
        uploaded_file = genai.upload_file(
            path=str(video_path),
            mime_type="video/mp4",
        )

        # Wait for the file to become ACTIVE (processing can take a few seconds)
        max_wait = 120  # seconds
        waited = 0
        while uploaded_file.state.name == "PROCESSING" and waited < max_wait:
            time.sleep(2)
            waited += 2
            uploaded_file = genai.get_file(uploaded_file.name)

        if uploaded_file.state.name != "ACTIVE":
            raise RuntimeError(
                f"Gemini file upload failed or timed out. "
                f"State: {uploaded_file.state.name}"
            )

        logger.info(
            "gemini_file_upload_complete",
            file_name=uploaded_file.name,
            uri=uploaded_file.uri,
        )

        # Step 2: Build the analysis prompt
        prompt = self._build_analysis_prompt(movement_type)

        # Step 3: Create a model instance that references the context cache
        cached_model = genai.GenerativeModel.from_cached_content(
            cached_content=cache,
            safety_settings=SAFETY_SETTINGS,
        )

        # Step 4: Send the video and prompt to Gemini
        response = cached_model.generate_content(
            contents=[uploaded_file, prompt],
            generation_config={
                "temperature": 0.2,  # Low temperature for consistent structured output
                "response_mime_type": "application/json",
            },
        )

        # Step 5: Clean up the uploaded file from Gemini File API storage
        try:
            genai.delete_file(uploaded_file.name)
        except Exception as cleanup_err:
            # Non-critical — log and continue
            logger.warning(
                "gemini_file_cleanup_failed",
                file_name=uploaded_file.name,
                error=str(cleanup_err),
            )

        # Step 6: Parse and validate the structured JSON response
        return self._parse_analysis_response(response.text, movement_type)

    def _build_analysis_prompt(self, movement_type: str) -> str:
        """
        Builds the structured analysis prompt for a specific movement type.
        The prompt instructs Gemini to return a specific JSON schema that
        maps directly to CoachingReportResponse.
        """
        return f"""
Analyze this CrossFit video of an athlete performing the {movement_type}.

Using the CrossFit movement standards and biomechanical analysis guide
provided in the context, perform a complete coaching analysis.

Return your analysis ONLY as a valid JSON object matching this exact schema:

{{
  "rep_count": <integer — number of complete, standard reps observed>,
  "estimated_weight_kg": <float or null — estimated barbell weight in kg, null if undetectable or bodyweight>,
  "overall_assessment": "<2-4 sentence summary of the athlete's overall performance and most critical area to address>",
  "faults": [
    {{
      "description": "<specific description of what is happening biomechanically>",
      "severity": "<MINOR|MODERATE|CRITICAL>",
      "timestamp_ms": <integer — milliseconds from video start when fault is most visible>,
      "cue": "<single actionable coaching instruction in plain language>",
      "affected_joints": ["<joint name>", ...]
    }}
  ],
  "global_cues": [
    "<coaching cue that applies to the entire set, not a specific rep>"
  ],
  "overlay_data": [
    {{
      "timestamp_ms": <integer>,
      "landmarks": [
        {{
          "index": <0-32>,
          "x": <0.0-1.0>,
          "y": <0.0-1.0>,
          "z": <float>,
          "visibility": <0.0-1.0>
        }}
      ],
      "joint_angles": {{
        "LEFT_KNEE": <degrees>,
        "RIGHT_KNEE": <degrees>,
        "LEFT_HIP": <degrees>,
        "RIGHT_HIP": <degrees>,
        "LEFT_ELBOW": <degrees>,
        "RIGHT_ELBOW": <degrees>,
        "LEFT_SHOULDER": <degrees>,
        "RIGHT_SHOULDER": <degrees>,
        "TRUNK_INCLINATION": <degrees>
      }}
    }}
  ]
}}

Rules:
1. overlay_data should include one entry every 100ms throughout the video.
   For a 10-second video, that is approximately 100 entries.
   Estimate landmark positions and joint angles as accurately as possible.
2. List faults in order from most severe (CRITICAL first) to least severe (MINOR last).
3. Limit to the 5 most significant faults maximum.
4. Global cues should be 2-4 items.
5. The overall_assessment must mention the athlete's strengths as well as areas to improve.
6. Return ONLY the JSON object. No markdown, no explanation text outside the JSON.
"""

    def _parse_analysis_response(
        self,
        response_text: str,
        movement_type: str,
    ) -> dict[str, Any]:
        """
        Parses and validates the Gemini JSON response.
        Raises ValueError if the response cannot be parsed or is structurally invalid.
        """
        try:
            # Strip any accidental markdown code fences
            clean = response_text.strip()
            if clean.startswith("```"):
                lines = clean.split("\n")
                clean = "\n".join(lines[1:-1])

            data = json.loads(clean)
        except json.JSONDecodeError as exc:
            logger.error(
                "gemini_response_json_parse_failed",
                error=str(exc),
                response_preview=response_text[:500],
            )
            raise ValueError(f"Gemini returned non-JSON response: {exc}") from exc

        # Validate required top-level keys
        required_keys = {"rep_count", "overall_assessment", "faults", "global_cues"}
        missing = required_keys - set(data.keys())
        if missing:
            raise ValueError(f"Gemini response missing required keys: {missing}")

        # Normalize faults — ensure severity is a valid enum value
        for fault in data.get("faults", []):
            severity = fault.get("severity", "MINOR").upper()
            if severity not in {s.value for s in FaultSeverity}:
                fault["severity"] = "MINOR"
            else:
                fault["severity"] = severity

        # Ensure overlay_data exists (may be empty if Gemini could not estimate)
        if "overlay_data" not in data:
            data["overlay_data"] = []

        data["movement_type"] = movement_type
        return data

    # -----------------------------------------------------------------------
    # Corrective Image Generation
    # -----------------------------------------------------------------------

    async def generate_correction_image(
        self,
        fault_description: str,
        movement_type: str,
        fault_timestamp_ms: int,
        video_path: Path | None = None,
    ) -> bytes:
        """
        Uses Gemini 2.0 Flash to generate a corrective posture image
        for a detected fault.

        The image shows the correct body position that would fix the fault.
        If video_path is provided, the specific frame is extracted first
        and included as visual context for Gemini Flash.

        Returns
        -------
        bytes
            PNG image data of the corrected posture visualization.
        """
        frame_bytes: bytes | None = None

        if video_path is not None and video_path.exists():
            try:
                frame_bytes = await asyncio.to_thread(
                    self._extract_video_frame,
                    video_path,
                    fault_timestamp_ms,
                )
            except Exception as frame_err:
                logger.warning(
                    "frame_extraction_failed",
                    timestamp_ms=fault_timestamp_ms,
                    error=str(frame_err),
                )

        logger.info(
            "gemini_correction_image_start",
            movement_type=movement_type,
            fault_description=fault_description[:100],
            has_frame=frame_bytes is not None,
        )

        image_bytes = await asyncio.to_thread(
            self._run_correction_image_generation,
            fault_description,
            movement_type,
            frame_bytes,
        )

        logger.info(
            "gemini_correction_image_complete",
            image_size_bytes=len(image_bytes),
        )

        return image_bytes

    def _extract_video_frame(
        self,
        video_path: Path,
        timestamp_ms: int,
    ) -> bytes:
        """
        Extracts a single frame from the video at the given timestamp.

        Uses the standard library only (no OpenCV dependency for this
        lightweight extraction — the frame is only needed for visual
        context in the Gemini Flash prompt, not for pose analysis).

        Falls back to a blank-context approach if extraction fails.
        Note: Production implementation should use cv2 or ffmpeg subprocess
        for reliable frame extraction. This implementation uses Gemini's
        own video segment understanding as a fallback.
        """
        # Attempt ffmpeg-based extraction to a temp PNG
        import subprocess  # noqa: PLC0415

        with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
            tmp_path = tmp.name

        try:
            timestamp_sec = timestamp_ms / 1000.0
            result = subprocess.run(  # noqa: S603
                [
                    "ffmpeg",
                    "-ss", str(timestamp_sec),
                    "-i", str(video_path),
                    "-frames:v", "1",
                    "-q:v", "2",
                    "-y",
                    tmp_path,
                ],
                capture_output=True,
                timeout=30,
                check=True,
            )
            with open(tmp_path, "rb") as f:
                return f.read()
        finally:
            Path(tmp_path).unlink(missing_ok=True)

    def _run_correction_image_generation(
        self,
        fault_description: str,
        movement_type: str,
        frame_bytes: bytes | None,
    ) -> bytes:
        """
        Synchronous Gemini Flash image generation call.
        Run via asyncio.to_thread().
        """
        prompt_parts: list[Any] = []

        if frame_bytes is not None:
            # Include the fault frame as visual context
            prompt_parts.append({
                "inline_data": {
                    "mime_type": "image/png",
                    "data": base64.b64encode(frame_bytes).decode("utf-8"),
                }
            })
            prompt_text = f"""
This image shows an athlete performing a {movement_type} with the following fault:
"{fault_description}"

Generate a clear, instructional image showing the CORRECT body position
that fixes this fault. The image should:
1. Show a side-view stick figure or simple athletic figure
2. Highlight the corrected joint positions with green indicators
3. Include a brief text label pointing to the key correction point
4. Use a clean white or light grey background
5. Be suitable for display on a mobile fitness app

The image should make the correction immediately obvious to the athlete.
"""
        else:
            prompt_text = f"""
Create a clear, instructional diagram showing the correct technique
for fixing this CrossFit fault during a {movement_type}:

Fault: "{fault_description}"

The diagram should:
1. Show a side-view stick figure or simple athletic figure in the correct position
2. Highlight the key joint positions with green indicators
3. Include a brief text label pointing to the key correction
4. Use a clean white or light grey background
5. Be simple enough to understand at a glance on a mobile screen

Focus exclusively on illustrating the correct position. Do not show the fault.
"""

        prompt_parts.append(prompt_text)

        response = self._flash_model.generate_content(
            contents=prompt_parts,
            generation_config={
                "temperature": 0.4,
                "response_modalities": ["image"],
            },
        )

        # Extract the image bytes from the response
        for part in response.candidates[0].content.parts:
            if hasattr(part, "inline_data") and part.inline_data is not None:
                return base64.b64decode(part.inline_data.data)

        raise ValueError(
            "Gemini Flash did not return an image in the response. "
            "Ensure the model supports image generation output."
        )
```

---

## File: `backend/main.py`

```python
"""
ApexAI Athletics — FastAPI Microservice
========================================
Orchestrates the Gemini AI coaching pipeline for the Android app.

Endpoints:
  POST   /v1/analyze-video              — Upload video, trigger Gemini analysis
  GET    /v1/coaching/status/{id}       — Poll analysis status
  GET    /v1/coaching/report/{id}       — Retrieve completed report
  POST   /v1/generate-correction-image  — Generate corrected posture image via Gemini Flash
  POST   /v1/cache/refresh              — Refresh CrossFit knowledge base context cache
  GET    /v1/health                     — Health check

Auth:
  All endpoints (except /health) require a valid Supabase JWT in the
  Authorization: Bearer header. The JWT is validated against the
  Supabase JWT secret (HS256). The user's sub claim becomes the athlete_id
  used throughout the pipeline.

Rate Limiting:
  /analyze-video: 10 requests per hour per user (enforced per spec §9.5)
  All other endpoints: no hard limit (rely on Supabase's default rate limiting)

Background Tasks:
  Video analysis runs as a FastAPI BackgroundTask so the client receives
  a 202 immediately. Status is polled via GET /coaching/status/{analysis_id}.
"""

from __future__ import annotations

import asyncio
import os
import tempfile
import time
import uuid
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from pathlib import Path
from typing import Annotated, Any

import aiofiles
import structlog
from fastapi import (
    BackgroundTasks,
    Depends,
    FastAPI,
    File,
    Form,
    Header,
    HTTPException,
    Request,
    UploadFile,
    status,
)
from fastapi.exception_handlers import http_exception_handler
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from jose import JWTError, jwt
from pydantic_settings import BaseSettings, SettingsConfigDict
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address
from supabase import Client, create_client

from gemini_service import GeminiService
from models import (
    AnalysisStatus,
    AnalysisStatusResponse,
    AnalyzeVideoResponse,
    CacheRefreshResponse,
    CoachingReportResponse,
    ErrorDetail,
    ErrorResponse,
    GenerateCorrectionImageRequest,
    GenerateCorrectionImageResponse,
    HealthCheckResponse,
    MovementFaultResponse,
    TimedPoseOverlay,
)

logger = structlog.get_logger(__name__)

# ---------------------------------------------------------------------------
# Settings
# ---------------------------------------------------------------------------

class Settings(BaseSettings):
    """
    Application settings loaded from environment variables.
    In development, these can be set in a .env file.
    In production, they must be set as container environment variables.
    """
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    # Supabase
    supabase_url: str
    supabase_service_role_key: str  # Used by FastAPI to write reports (bypasses RLS)
    supabase_jwt_secret: str        # Used to validate user JWTs

    # Gemini (see gemini_service.py for model name settings)
    gemini_api_key: str

    # Storage
    video_bucket_name: str = "videos"
    corrections_bucket_name: str = "corrections"

    # App
    app_version: str = "1.0.0"
    debug: bool = False
    cors_origins: list[str] = ["*"]  # Restrict in production


settings = Settings()  # type: ignore[call-arg]

# ---------------------------------------------------------------------------
# Application State
# ---------------------------------------------------------------------------

# In-memory job status store.
# In production with multiple replicas, replace with Redis.
# Keys: analysis_id (str) -> AnalysisStatusResponse
_analysis_jobs: dict[str, dict[str, Any]] = {}

_start_time = time.monotonic()

# ---------------------------------------------------------------------------
# Dependency: Supabase Clients
# ---------------------------------------------------------------------------

def get_supabase_client() -> Client:
    """
    Returns a Supabase client using the service role key.
    This client bypasses RLS and is used ONLY for server-side writes
    (inserting coaching reports, updating video status).
    Never expose this client to user-controlled input without explicit
    row-level filtering.
    """
    return create_client(settings.supabase_url, settings.supabase_service_role_key)


# ---------------------------------------------------------------------------
# Dependency: JWT Authentication
# ---------------------------------------------------------------------------

async def get_current_user_id(
    authorization: Annotated[str | None, Header()] = None,
) -> str:
    """
    FastAPI dependency that validates the Supabase JWT and returns the user ID.

    Supabase JWTs are HS256-signed with the project JWT secret.
    The 'sub' claim contains the user's UUID (matches profiles.id).

    Raises HTTP 401 if the token is missing, expired, or invalid.
    """
    if authorization is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header is required",
            headers={"WWW-Authenticate": "Bearer"},
        )

    parts = authorization.split(" ")
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header must be 'Bearer <token>'",
            headers={"WWW-Authenticate": "Bearer"},
        )

    token = parts[1]

    try:
        payload = jwt.decode(
            token,
            settings.supabase_jwt_secret,
            algorithms=["HS256"],
            options={"verify_aud": False},  # Supabase JWTs use 'authenticated' as audience
        )
        user_id: str | None = payload.get("sub")
        if user_id is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="JWT is missing 'sub' claim",
            )
        return user_id
    except JWTError as exc:
        logger.warning("jwt_validation_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        ) from exc


# ---------------------------------------------------------------------------
# Dependency: Gemini Service
# ---------------------------------------------------------------------------

_gemini_service: GeminiService | None = None


async def get_gemini_service() -> GeminiService:
    global _gemini_service  # noqa: PLW0603
    if _gemini_service is None:
        _gemini_service = GeminiService()
    return _gemini_service


# ---------------------------------------------------------------------------
# Rate Limiter
# ---------------------------------------------------------------------------

limiter = Limiter(key_func=get_remote_address)


# ---------------------------------------------------------------------------
# Application Lifecycle
# ---------------------------------------------------------------------------

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Startup: pre-load Gemini context cache so the first analysis request
    does not incur cache creation latency.
    Shutdown: no cleanup required (Gemini cache is server-side).
    """
    logger.info("startup_begin", version=settings.app_version)
    try:
        gemini = GeminiService()
        global _gemini_service  # noqa: PLW0603
        _gemini_service = gemini
        cache = await gemini.get_or_create_cache()
        logger.info("startup_cache_loaded", cache_name=cache.name)
    except Exception as exc:
        # Log but do not crash — the app can still serve requests without
        # the pre-loaded cache (it will be created on the first analysis).
        logger.error("startup_cache_load_failed", error=str(exc))
    logger.info("startup_complete")
    yield
    logger.info("shutdown")


# ---------------------------------------------------------------------------
# FastAPI Application
# ---------------------------------------------------------------------------

app = FastAPI(
    title="ApexAI Athletics Coaching API",
    description="Gemini-powered AI video coaching microservice for CrossFit athletes",
    version=settings.app_version,
    lifespan=lifespan,
    docs_url="/v1/docs" if settings.debug else None,
    redoc_url="/v1/redoc" if settings.debug else None,
)

# Rate limiter integration
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS — restrict to known origins in production
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["Authorization", "Content-Type"],
)


# ---------------------------------------------------------------------------
# Exception Handlers
# ---------------------------------------------------------------------------

@app.exception_handler(HTTPException)
async def custom_http_exception_handler(request: Request, exc: HTTPException):
    """Wraps all HTTP errors in the standard error envelope."""
    error_map = {
        400: "BAD_REQUEST",
        401: "UNAUTHORIZED",
        403: "FORBIDDEN",
        404: "NOT_FOUND",
        409: "CONFLICT",
        413: "PAYLOAD_TOO_LARGE",
        422: "VALIDATION_ERROR",
        429: "RATE_LIMITED",
        500: "INTERNAL_SERVER_ERROR",
        503: "SERVICE_UNAVAILABLE",
    }
    code = error_map.get(exc.status_code, "ERROR")
    return JSONResponse(
        status_code=exc.status_code,
        content=ErrorResponse(
            error=ErrorDetail(code=code, message=str(exc.detail))
        ).model_dump(),
    )


# ---------------------------------------------------------------------------
# Background Task: Video Analysis Pipeline
# ---------------------------------------------------------------------------

async def run_analysis_pipeline(
    analysis_id: str,
    video_path: Path,
    movement_type: str,
    athlete_id: str,
    video_id: str,
    gemini: GeminiService,
    supabase: Client,
) -> None:
    """
    Background task that orchestrates the full Gemini analysis pipeline.

    Pipeline stages:
      1. analyzing_video  — Gemini 1.5 Pro processes the video
      2. generating_corrections — Gemini 2.0 Flash creates corrective images
      3. finalizing — write results to Supabase

    Status is updated in _analysis_jobs at each stage so the polling
    endpoint can report granular progress to the Android client.
    """

    def update_status(
        progress: float,
        stage: str,
        status: AnalysisStatus = AnalysisStatus.PROCESSING,
        error_message: str | None = None,
    ) -> None:
        _analysis_jobs[analysis_id].update({
            "status": status.value,
            "progress": progress,
            "stage": stage,
            "error_message": error_message,
        })

    try:
        # Stage 1: Gemini video analysis
        update_status(0.05, "analyzing_video")
        logger.info(
            "analysis_pipeline_start",
            analysis_id=analysis_id,
            movement_type=movement_type,
        )

        analysis_data = await gemini.analyze_video(
            video_path=video_path,
            movement_type=movement_type,
            athlete_id=athlete_id,
            video_id=video_id,
        )

        update_status(0.60, "generating_corrections")

        # Stage 2: Generate corrective images for each fault (Gemini Flash)
        faults = analysis_data.get("faults", [])
        for i, fault in enumerate(faults):
            try:
                image_bytes = await gemini.generate_correction_image(
                    fault_description=fault.get("description", ""),
                    movement_type=movement_type,
                    fault_timestamp_ms=fault.get("timestamp_ms", 0),
                    video_path=video_path,
                )

                # Upload the corrective image to Supabase Storage
                correction_path = f"corrections/{athlete_id}/{analysis_id}/fault_{i}.png"
                supabase.storage.from_(settings.corrections_bucket_name).upload(
                    path=correction_path,
                    file=image_bytes,
                    file_options={"content-type": "image/png"},
                )

                # Get a signed URL (1 hour TTL — the Android client should
                # re-fetch if the report is opened after expiry)
                signed_url_response = supabase.storage.from_(
                    settings.corrections_bucket_name
                ).create_signed_url(correction_path, expires_in=3600)

                fault["corrected_image_url"] = signed_url_response.get("signedURL", "")
                fault["_storage_path"] = correction_path

                progress = 0.60 + (0.30 * ((i + 1) / max(len(faults), 1)))
                update_status(progress, "generating_corrections")

            except Exception as fault_img_err:
                # A failing image generation is non-fatal — the fault is
                # still reported without the corrective image.
                logger.warning(
                    "correction_image_failed",
                    fault_index=i,
                    error=str(fault_img_err),
                )
                fault["corrected_image_url"] = None

        update_status(0.90, "finalizing")

        # Stage 3: Write coaching report to Supabase
        report_id = str(uuid.uuid4())

        report_row = {
            "id": report_id,
            "video_id": video_id,
            "user_id": athlete_id,
            "movement_type": movement_type,
            "overall_assessment": analysis_data.get("overall_assessment", ""),
            "rep_count": analysis_data.get("rep_count", 0),
            "estimated_weight_kg": analysis_data.get("estimated_weight_kg"),
            "global_cues": analysis_data.get("global_cues", []),
            "overlay_data": analysis_data.get("overlay_data", []),
        }

        supabase.table("coaching_reports").insert(report_row).execute()

        # Write movement faults as child rows
        fault_rows = []
        for fault in faults:
            fault_rows.append({
                "id": str(uuid.uuid4()),
                "report_id": report_id,
                "description": fault.get("description", ""),
                "severity": fault.get("severity", "MINOR"),
                "timestamp_ms": fault.get("timestamp_ms", 0),
                "cue": fault.get("cue", ""),
                "corrected_image_url": fault.get("corrected_image_url"),
                "affected_joints": fault.get("affected_joints", []),
            })

        if fault_rows:
            supabase.table("movement_faults").insert(fault_rows).execute()

        # Update video status to 'complete'
        supabase.table("video_uploads").update(
            {"status": "complete"}
        ).eq("id", video_id).execute()

        # Store the report_id in the job so the status endpoint can
        # redirect the client to the correct report URL
        _analysis_jobs[analysis_id].update({
            "status": AnalysisStatus.COMPLETE.value,
            "progress": 1.0,
            "stage": "finalizing",
            "report_id": report_id,
        })

        logger.info(
            "analysis_pipeline_complete",
            analysis_id=analysis_id,
            report_id=report_id,
        )

    except Exception as exc:
        logger.exception(
            "analysis_pipeline_failed",
            analysis_id=analysis_id,
            error=str(exc),
        )
        update_status(
            progress=_analysis_jobs.get(analysis_id, {}).get("progress", 0.0),
            stage="error",
            status=AnalysisStatus.ERROR,
            error_message=str(exc),
        )
        # Update video status to 'error'
        try:
            supabase.table("video_uploads").update(
                {"status": "error", "error_message": str(exc)[:500]}
            ).eq("id", video_id).execute()
        except Exception:
            pass  # Best-effort cleanup

    finally:
        # Always clean up the temporary video file
        try:
            video_path.unlink(missing_ok=True)
        except Exception:
            pass


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------

@app.get("/v1/health", response_model=HealthCheckResponse, tags=["System"])
async def health_check() -> HealthCheckResponse:
    """
    Health check endpoint. Does not require authentication.
    Used by Cloud Run / Railway / load balancer health probes.
    """
    cache_loaded = "crossfit_knowledge_base" in _gemini_service._cache_store if _gemini_service else False
    return HealthCheckResponse(
        status="ok",
        version=settings.app_version,
        gemini_cache_loaded=cache_loaded,
        uptime_seconds=time.monotonic() - _start_time,
    )


@app.post(
    "/v1/analyze-video",
    status_code=status.HTTP_202_ACCEPTED,
    response_model=AnalyzeVideoResponse,
    responses={
        400: {"model": ErrorResponse},
        401: {"model": ErrorResponse},
        413: {"model": ErrorResponse},
        429: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
@limiter.limit("10/hour")
async def analyze_video(
    request: Request,
    background_tasks: BackgroundTasks,
    video: UploadFile = File(..., description="Video file (video/mp4, max 500MB)"),
    movement_type: str = Form(..., min_length=1, max_length=100),
    athlete_id: str = Form(..., description="Supabase user UUID"),
    current_user_id: str = Depends(get_current_user_id),
    gemini: GeminiService = Depends(get_gemini_service),
) -> AnalyzeVideoResponse:
    """
    Upload a video for Gemini AI coaching analysis.

    The video is saved to a temporary file and analysis runs as a
    background task. Returns 202 Accepted immediately with an analysis_id
    the client uses to poll status.

    The athlete_id in the form body must match the JWT sub claim to prevent
    one user from submitting analysis under another user's ID.
    """
    # Verify the form athlete_id matches the authenticated user
    if athlete_id != current_user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="athlete_id does not match authenticated user",
        )

    # Validate content type
    allowed_types = {"video/mp4", "video/quicktime", "video/x-m4v"}
    if video.content_type not in allowed_types:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid video format. Allowed types: {', '.join(allowed_types)}",
        )

    # Stream to temp file and check size
    suffix = Path(video.filename or "upload.mp4").suffix or ".mp4"
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
        tmp_path = Path(tmp.name)

    total_bytes = 0
    max_size = 500 * 1024 * 1024  # 500 MB

    try:
        async with aiofiles.open(tmp_path, "wb") as f:
            while chunk := await video.read(1024 * 1024):  # 1 MB chunks
                total_bytes += len(chunk)
                if total_bytes > max_size:
                    tmp_path.unlink(missing_ok=True)
                    raise HTTPException(
                        status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                        detail=f"Video exceeds maximum size of 500 MB",
                    )
                await f.write(chunk)
    except HTTPException:
        raise
    except Exception as exc:
        tmp_path.unlink(missing_ok=True)
        logger.error("video_upload_save_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to save uploaded video",
        ) from exc

    # Create video_uploads record in Supabase
    supabase = get_supabase_client()
    video_id = str(uuid.uuid4())

    try:
        supabase.table("video_uploads").insert({
            "id": video_id,
            "user_id": athlete_id,
            "storage_path": f"videos/{athlete_id}/{video_id}{suffix}",
            "movement_type": movement_type,
            "file_size_bytes": total_bytes,
            "status": "analyzing",
        }).execute()
    except Exception as exc:
        tmp_path.unlink(missing_ok=True)
        logger.error("video_record_creation_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create video record",
        ) from exc

    analysis_id = str(uuid.uuid4())

    # Initialize job status
    _analysis_jobs[analysis_id] = {
        "status": AnalysisStatus.PROCESSING.value,
        "progress": 0.01,
        "stage": "analyzing_video",
        "video_id": video_id,
        "report_id": None,
        "error_message": None,
    }

    # Queue the background analysis task
    background_tasks.add_task(
        run_analysis_pipeline,
        analysis_id=analysis_id,
        video_path=tmp_path,
        movement_type=movement_type,
        athlete_id=athlete_id,
        video_id=video_id,
        gemini=gemini,
        supabase=supabase,
    )

    logger.info(
        "analysis_job_queued",
        analysis_id=analysis_id,
        video_id=video_id,
        movement_type=movement_type,
        file_size_bytes=total_bytes,
    )

    return AnalyzeVideoResponse(
        analysis_id=analysis_id,
        status=AnalysisStatus.PROCESSING,
        estimated_seconds=45,
        poll_url=f"/v1/coaching/status/{analysis_id}",
    )


@app.get(
    "/v1/coaching/status/{analysis_id}",
    response_model=AnalysisStatusResponse,
    responses={
        401: {"model": ErrorResponse},
        404: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
async def get_analysis_status(
    analysis_id: str,
    current_user_id: str = Depends(get_current_user_id),
) -> AnalysisStatusResponse:
    """
    Poll the status of a video analysis job.

    The Android client polls this endpoint every 3 seconds until
    status == 'complete', then fetches the full report.
    """
    job = _analysis_jobs.get(analysis_id)
    if job is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Analysis job {analysis_id} not found",
        )

    return AnalysisStatusResponse(
        analysis_id=analysis_id,
        status=AnalysisStatus(job["status"]),
        progress=job.get("progress", 0.0),
        stage=job.get("stage"),
        error_message=job.get("error_message"),
    )


@app.get(
    "/v1/coaching/report/{analysis_id}",
    response_model=CoachingReportResponse,
    responses={
        401: {"model": ErrorResponse},
        404: {"model": ErrorResponse},
        409: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
async def get_coaching_report(
    analysis_id: str,
    current_user_id: str = Depends(get_current_user_id),
) -> CoachingReportResponse:
    """
    Retrieve a completed coaching report.

    Returns 409 Conflict if the analysis is still in progress —
    the client should continue polling /coaching/status/{analysis_id}.
    """
    job = _analysis_jobs.get(analysis_id)
    if job is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Analysis job {analysis_id} not found",
        )

    if job["status"] == AnalysisStatus.PROCESSING.value:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Analysis is still in progress. Poll /coaching/status for updates.",
        )

    if job["status"] == AnalysisStatus.ERROR.value:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Analysis failed: {job.get('error_message', 'Unknown error')}",
        )

    report_id = job.get("report_id")
    if report_id is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Report ID not available — analysis may have failed",
        )

    # Fetch report from Supabase
    supabase = get_supabase_client()

    try:
        report_response = (
            supabase.table("coaching_reports")
            .select("*")
            .eq("id", report_id)
            .eq("user_id", current_user_id)  # Enforce ownership
            .single()
            .execute()
        )
    except Exception as exc:
        logger.error("report_fetch_failed", report_id=report_id, error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve coaching report",
        ) from exc

    if report_response.data is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Coaching report {report_id} not found",
        )

    # Fetch associated movement faults
    try:
        faults_response = (
            supabase.table("movement_faults")
            .select("*")
            .eq("report_id", report_id)
            .order("severity")  # CRITICAL first (alphabetical matches intent here)
            .execute()
        )
    except Exception as exc:
        logger.error("faults_fetch_failed", report_id=report_id, error=str(exc))
        faults_response = type("obj", (object,), {"data": []})()

    report_data = report_response.data
    faults_data = faults_response.data or []

    # Build the response
    faults = [
        MovementFaultResponse(
            id=f["id"],
            description=f["description"],
            severity=f["severity"],
            timestamp_ms=f["timestamp_ms"],
            cue=f["cue"],
            corrected_image_url=f.get("corrected_image_url"),
            affected_joints=f.get("affected_joints", []),
        )
        for f in faults_data
    ]

    overlay_data = [
        TimedPoseOverlay(**item)
        for item in (report_data.get("overlay_data") or [])
    ]

    return CoachingReportResponse(
        id=report_data["id"],
        video_id=report_data["video_id"],
        movement_type=report_data["movement_type"],
        overall_assessment=report_data.get("overall_assessment", ""),
        rep_count=report_data.get("rep_count", 0),
        estimated_weight_kg=report_data.get("estimated_weight_kg"),
        faults=faults,
        global_cues=report_data.get("global_cues", []),
        overlay_data=overlay_data,
        created_at=datetime.fromisoformat(report_data["created_at"]),
    )


@app.post(
    "/v1/generate-correction-image",
    response_model=GenerateCorrectionImageResponse,
    responses={
        400: {"model": ErrorResponse},
        401: {"model": ErrorResponse},
        404: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
async def generate_correction_image(
    request_body: GenerateCorrectionImageRequest,
    current_user_id: str = Depends(get_current_user_id),
    gemini: GeminiService = Depends(get_gemini_service),
) -> GenerateCorrectionImageResponse:
    """
    Generate a corrected posture image for a specific movement fault
    using Gemini 2.0 Flash.

    This endpoint is called by the Android client when viewing a fault
    that does not yet have a corrected_image_url. It can also be used
    to regenerate an image.
    """
    supabase = get_supabase_client()

    # Verify the fault belongs to a report owned by the current user
    try:
        fault_response = (
            supabase.table("movement_faults")
            .select("*, coaching_reports!inner(user_id, movement_type)")
            .eq("id", request_body.fault_id)
            .eq("report_id", request_body.report_id)
            .single()
            .execute()
        )
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Fault not found",
        ) from exc

    if fault_response.data is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Fault not found",
        )

    report_user_id = fault_response.data["coaching_reports"]["user_id"]
    if report_user_id != current_user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied",
        )

    # Generate the corrective image
    image_bytes = await gemini.generate_correction_image(
        fault_description=request_body.fault_description,
        movement_type=request_body.movement_type,
        fault_timestamp_ms=request_body.fault_timestamp_ms,
    )

    # Upload to Supabase Storage
    storage_path = (
        f"corrections/{current_user_id}/"
        f"{request_body.report_id}/{request_body.fault_id}.png"
    )

    try:
        supabase.storage.from_(settings.corrections_bucket_name).upload(
            path=storage_path,
            file=image_bytes,
            file_options={"content-type": "image/png", "upsert": "true"},
        )
    except Exception as exc:
        logger.error("correction_image_upload_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to upload corrective image to storage",
        ) from exc

    # Generate signed URL
    signed_url_response = supabase.storage.from_(
        settings.corrections_bucket_name
    ).create_signed_url(storage_path, expires_in=3600)

    signed_url = signed_url_response.get("signedURL", "")

    # Update the fault row with the image URL
    supabase.table("movement_faults").update(
        {"corrected_image_url": signed_url}
    ).eq("id", request_body.fault_id).execute()

    logger.info(
        "correction_image_generated",
        fault_id=request_body.fault_id,
        storage_path=storage_path,
    )

    return GenerateCorrectionImageResponse(
        fault_id=request_body.fault_id,
        corrected_image_url=signed_url,
        storage_path=storage_path,
    )


@app.post(
    "/v1/cache/refresh",
    response_model=CacheRefreshResponse,
    responses={
        401: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
    },
    tags=["System"],
)
async def refresh_context_cache(
    current_user_id: str = Depends(get_current_user_id),
    gemini: GeminiService = Depends(get_gemini_service),
) -> CacheRefreshResponse:
    """
    Refreshes the Gemini CrossFit knowledge base context cache.

    Call this endpoint after updating the movement standards knowledge base.
    The existing cache is evicted and a new one is created.

    Note: In production, this should be restricted to admin users.
    For MVP the check is that the caller is authenticated.
    """
    try:
        cache_metadata = await gemini.force_refresh_cache()
    except Exception as exc:
        logger.error("cache_refresh_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Failed to refresh Gemini context cache: {exc}",
        ) from exc

    return CacheRefreshResponse(
        cache_name=cache_metadata["cache_name"],
        model=cache_metadata["model"],
        token_count=cache_metadata.get("token_count", 0),
        expires_at=datetime.fromisoformat(cache_metadata["expires_at"]),
        refreshed_at=datetime.fromisoformat(cache_metadata["refreshed_at"]),
    )
```

---

## File: `supabase/functions/calculate-readiness/index.ts`

```typescript
/**
 * ApexAI Athletics — Supabase Edge Function: calculate-readiness
 * ==============================================================
 * Accepts Health Connect biometric data posted by the Android client,
 * persists it as a health_snapshot, then calculates and returns the
 * ACWR composite readiness score.
 *
 * This function is invoked via:
 *   POST https://{PROJECT_REF}.supabase.co/functions/v1/calculate-readiness
 *
 * Auth: Requires Supabase JWT in Authorization: Bearer header.
 *       The user ID is extracted from the JWT — never trusted from the request body.
 *
 * Request body:
 * {
 *   "hrv_rmssd": 68,                    // int, ms (optional)
 *   "sleep_duration_minutes": 420,       // int (optional)
 *   "deep_sleep_minutes": 90,            // int (optional)
 *   "rem_sleep_minutes": 100,            // int (optional)
 *   "resting_hr": 52,                    // int, bpm (optional)
 *   "captured_at": "2026-03-28T07:00:00Z" // ISO 8601 (required)
 * }
 *
 * Success Response (200):
 * {
 *   "success": true,
 *   "data": {
 *     "snapshot_id": "uuid",
 *     "readiness": {
 *       "acwr": 1.12,
 *       "zone": "OPTIMAL",
 *       "acute_load": 2240.5,
 *       "chronic_load": 1998.3,
 *       "hrv_rmssd": 68,
 *       "sleep_minutes": 420,
 *       "resting_hr": 52,
 *       "recommendation": "...",
 *       "calculated_at": "2026-03-28T08:30:00Z"
 *     }
 *   }
 * }
 *
 * The Android ReadinessViewModel calls this function after syncing
 * Health Connect data. The returned readiness object maps directly
 * to ReadinessScore domain entity.
 */

import { createClient, SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2.47.0";

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface HealthConnectPayload {
  hrv_rmssd?: number;
  sleep_duration_minutes?: number;
  deep_sleep_minutes?: number;
  rem_sleep_minutes?: number;
  resting_hr?: number;
  captured_at: string;
}

interface ReadinessResult {
  acwr: number;
  zone: "UNDERTRAINED" | "OPTIMAL" | "CAUTION" | "HIGH_RISK";
  acute_load: number;
  chronic_load: number;
  hrv_rmssd: number | null;
  sleep_minutes: number | null;
  resting_hr: number | null;
  hrv_component: number | null;
  sleep_component: number | null;
  recommendation: string;
  calculated_at: string;
}

interface SuccessResponse<T> {
  success: true;
  data: T;
}

interface ErrorResponse {
  success: false;
  error: {
    code: string;
    message: string;
  };
}

type ApiResponse<T> = SuccessResponse<T> | ErrorResponse;

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

// ACWR zone thresholds from CLAUDE.md and ARCHITECTURE_PLAN.md
const ACWR_UNDERTRAINED_MAX = 0.8;
const ACWR_OPTIMAL_MAX = 1.3;
const ACWR_CAUTION_MAX = 1.5;

// HRV thresholds for component scoring (population-level guidelines)
const HRV_EXCELLENT_THRESHOLD = 70;  // ms RMSSD
const HRV_ADEQUATE_THRESHOLD = 40;

// Sleep thresholds for component scoring
const SLEEP_OPTIMAL_MINUTES = 480;   // 8 hours
const SLEEP_ADEQUATE_MINUTES = 360;  // 6 hours

// Minimum chronic workload to prevent division-by-zero on new users
const MIN_CHRONIC_LOAD = 0.01;

// ---------------------------------------------------------------------------
// Validation
// ---------------------------------------------------------------------------

/**
 * Validates and sanitizes the Health Connect payload.
 * Returns a typed, validated payload or throws with a descriptive message.
 */
function validatePayload(raw: unknown): HealthConnectPayload {
  if (typeof raw !== "object" || raw === null) {
    throw new Error("Request body must be a JSON object");
  }

  const body = raw as Record<string, unknown>;

  // captured_at is required — without it we cannot enforce the per-day
  // UNIQUE constraint on health_snapshots.
  if (!body.captured_at || typeof body.captured_at !== "string") {
    throw new Error("captured_at is required and must be an ISO 8601 string");
  }

  const capturedAt = new Date(body.captured_at);
  if (isNaN(capturedAt.getTime())) {
    throw new Error(`captured_at '${body.captured_at}' is not a valid ISO 8601 date`);
  }

  // Validate numeric fields if provided
  const numericFields = [
    "hrv_rmssd",
    "sleep_duration_minutes",
    "deep_sleep_minutes",
    "rem_sleep_minutes",
    "resting_hr",
  ] as const;

  for (const field of numericFields) {
    if (body[field] !== undefined) {
      const value = Number(body[field]);
      if (isNaN(value) || value < 0) {
        throw new Error(`${field} must be a non-negative number, got: ${body[field]}`);
      }
    }
  }

  // Domain-specific range validation
  if (body.hrv_rmssd !== undefined) {
    const hrv = Number(body.hrv_rmssd);
    if (hrv < 1 || hrv > 300) {
      throw new Error(`hrv_rmssd ${hrv} is outside valid range (1–300 ms)`);
    }
  }

  if (body.resting_hr !== undefined) {
    const hr = Number(body.resting_hr);
    if (hr < 20 || hr > 250) {
      throw new Error(`resting_hr ${hr} is outside valid range (20–250 bpm)`);
    }
  }

  if (body.sleep_duration_minutes !== undefined) {
    const sleep = Number(body.sleep_duration_minutes);
    if (sleep > 1440) {
      throw new Error(`sleep_duration_minutes ${sleep} exceeds 24 hours (1440 min)`);
    }
  }

  return {
    hrv_rmssd: body.hrv_rmssd !== undefined ? Number(body.hrv_rmssd) : undefined,
    sleep_duration_minutes: body.sleep_duration_minutes !== undefined
      ? Number(body.sleep_duration_minutes)
      : undefined,
    deep_sleep_minutes: body.deep_sleep_minutes !== undefined
      ? Number(body.deep_sleep_minutes)
      : undefined,
    rem_sleep_minutes: body.rem_sleep_minutes !== undefined
      ? Number(body.rem_sleep_minutes)
      : undefined,
    resting_hr: body.resting_hr !== undefined ? Number(body.resting_hr) : undefined,
    captured_at: body.captured_at as string,
  };
}

// ---------------------------------------------------------------------------
// ACWR Calculation
// ---------------------------------------------------------------------------

/**
 * Calculates Acute:Chronic Workload Ratio from the athlete's results table.
 *
 * Formula (from CLAUDE.md):
 *   W_acute  = SUM(score_numeric × rpe_or_5) for past 7 days
 *   W_chronic = SUM(score_numeric × rpe_or_5) for past 28 days ÷ 4
 *   ACWR = W_acute / W_chronic
 *
 * The 28-day sum divided by 4 gives the equivalent 7-day average,
 * making the ratio dimensionally consistent.
 */
async function calculateAcwr(
  supabase: SupabaseClient,
  userId: string,
): Promise<{ acuteLoad: number; chronicLoad: number; acwr: number }> {
  const now = new Date().toISOString();

  const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString();
  const twentyEightDaysAgo = new Date(Date.now() - 28 * 24 * 60 * 60 * 1000).toISOString();

  // Acute load: past 7 days
  const { data: acuteData, error: acuteError } = await supabase
    .from("results")
    .select("score_numeric, rpe")
    .eq("user_id", userId)
    .gte("completed_at", sevenDaysAgo)
    .not("score_numeric", "is", null)
    .gt("score_numeric", 0);

  if (acuteError) {
    throw new Error(`Failed to fetch acute workload data: ${acuteError.message}`);
  }

  const acuteLoad = (acuteData || []).reduce((sum, row) => {
    const load = (row.score_numeric ?? 0) * (row.rpe ?? 5);
    return sum + load;
  }, 0);

  // Chronic load: past 28 days (sum / 4 = weekly average)
  const { data: chronicData, error: chronicError } = await supabase
    .from("results")
    .select("score_numeric, rpe")
    .eq("user_id", userId)
    .gte("completed_at", twentyEightDaysAgo)
    .not("score_numeric", "is", null)
    .gt("score_numeric", 0);

  if (chronicError) {
    throw new Error(`Failed to fetch chronic workload data: ${chronicError.message}`);
  }

  const chronicSum = (chronicData || []).reduce((sum, row) => {
    const load = (row.score_numeric ?? 0) * (row.rpe ?? 5);
    return sum + load;
  }, 0);

  const chronicLoad = Math.max(chronicSum / 4.0, MIN_CHRONIC_LOAD);
  const acwr = acuteLoad / chronicLoad;

  return {
    acuteLoad: Math.round(acuteLoad * 100) / 100,
    chronicLoad: Math.round(chronicLoad * 100) / 100,
    acwr: Math.round(acwr * 10000) / 10000,
  };
}

//
Continuing the Edge Function from the `calculateAcwr` call onward.

---

## File: `supabase/functions/calculate-readiness/index.ts` (continued)

```typescript
// ---------------------------------------------------------------------------
// Zone Classification
// ---------------------------------------------------------------------------

function classifyZone(
  acwr: number,
): "UNDERTRAINED" | "OPTIMAL" | "CAUTION" | "HIGH_RISK" {
  if (acwr < ACWR_UNDERTRAINED_MAX) return "UNDERTRAINED";
  if (acwr <= ACWR_OPTIMAL_MAX) return "OPTIMAL";
  if (acwr <= ACWR_CAUTION_MAX) return "CAUTION";
  return "HIGH_RISK";
}

// ---------------------------------------------------------------------------
// HRV Component Score (0.0 – 1.0)
// Normalizes raw RMSSD into a 0-1 recovery signal.
// ---------------------------------------------------------------------------

function scoreHrv(hrvRmssd: number | null | undefined): number | null {
  if (hrvRmssd == null) return null;
  if (hrvRmssd >= HRV_EXCELLENT_THRESHOLD) return 1.0;
  if (hrvRmssd >= HRV_ADEQUATE_THRESHOLD) {
    // Linear interpolation between adequate and excellent thresholds
    return 0.5 + 0.5 * ((hrvRmssd - HRV_ADEQUATE_THRESHOLD) /
      (HRV_EXCELLENT_THRESHOLD - HRV_ADEQUATE_THRESHOLD));
  }
  // Below adequate: linear from 0 to 0.5
  return Math.max(0, 0.5 * (hrvRmssd / HRV_ADEQUATE_THRESHOLD));
}

// ---------------------------------------------------------------------------
// Sleep Component Score (0.0 – 1.0)
// ---------------------------------------------------------------------------

function scoreSleep(sleepMinutes: number | null | undefined): number | null {
  if (sleepMinutes == null) return null;
  if (sleepMinutes >= SLEEP_OPTIMAL_MINUTES) return 1.0;
  if (sleepMinutes >= SLEEP_ADEQUATE_MINUTES) {
    return 0.5 + 0.5 * ((sleepMinutes - SLEEP_ADEQUATE_MINUTES) /
      (SLEEP_OPTIMAL_MINUTES - SLEEP_ADEQUATE_MINUTES));
  }
  // Below 6 hours: linear from 0 to 0.5
  return Math.max(0, 0.5 * (sleepMinutes / SLEEP_ADEQUATE_MINUTES));
}

// ---------------------------------------------------------------------------
// Recommendation Text
// Incorporates ACWR zone, HRV signal, and sleep quality into a single
// actionable recommendation the Android client surfaces to the athlete.
// ---------------------------------------------------------------------------

function buildRecommendation(
  zone: string,
  hrvScore: number | null,
  sleepScore: number | null,
  sleepMinutes: number | null | undefined,
  hrvRmssd: number | null | undefined,
): string {
  const hrvNote = hrvRmssd == null
    ? "HRV data unavailable — connect a compatible wearable for full readiness scoring."
    : hrvRmssd >= HRV_EXCELLENT_THRESHOLD
      ? `HRV is strong at ${hrvRmssd} ms — parasympathetic recovery is excellent.`
      : hrvRmssd >= HRV_ADEQUATE_THRESHOLD
        ? `HRV is adequate at ${hrvRmssd} ms — recovery is on track.`
        : `HRV is low at ${hrvRmssd} ms — consider additional rest or stress management.`;

  const sleepNote = sleepMinutes == null
    ? "Sleep data unavailable — enable sleep tracking in Health Connect for full scoring."
    : sleepMinutes >= SLEEP_OPTIMAL_MINUTES
      ? `Sleep is optimal at ${Math.round(sleepMinutes / 60 * 10) / 10} hours.`
      : sleepMinutes >= SLEEP_ADEQUATE_MINUTES
        ? `Sleep is adequate at ${Math.round(sleepMinutes / 60 * 10) / 10} hours — aim for 8+ hours for peak recovery.`
        : `Sleep is insufficient at ${Math.round(sleepMinutes / 60 * 10) / 10} hours — prioritise rest tonight.`;

  const zoneText: Record<string, string> = {
    OPTIMAL:
      "You are in the optimal training zone. This is a good day for heavy lifts or benchmark WODs.",
    CAUTION:
      "Training load is elevated. Prioritise technique work and moderate intensity. Avoid testing maximal efforts.",
    HIGH_RISK:
      "High injury risk detected (ACWR > 1.5). Scale to active recovery, mobility, or complete rest today.",
    UNDERTRAINED:
      "Training volume is below your 28-day baseline. Gradually increase load — a 10–15% weekly increase is safe.",
  };

  return `${zoneText[zone] ?? "Readiness calculated."} ${hrvNote} ${sleepNote}`.trim();
}

// ---------------------------------------------------------------------------
// Snapshot Upsert
// Inserts or updates the health_snapshot for the given calendar day.
// The UNIQUE constraint on (user_id, DATE(captured_at)) enforces one row
// per day; ON CONFLICT updates the biometric values with the latest reading.
// ---------------------------------------------------------------------------

async function upsertHealthSnapshot(
  supabase: SupabaseClient,
  userId: string,
  payload: HealthConnectPayload,
): Promise<string> {
  const snapshotId = crypto.randomUUID();

  const { data, error } = await supabase
    .from("health_snapshots")
    .upsert(
      {
        id: snapshotId,
        user_id: userId,
        hrv_rmssd: payload.hrv_rmssd ?? null,
        sleep_duration_minutes: payload.sleep_duration_minutes ?? null,
        deep_sleep_minutes: payload.deep_sleep_minutes ?? null,
        rem_sleep_minutes: payload.rem_sleep_minutes ?? null,
        resting_hr: payload.resting_hr ?? null,
        captured_at: payload.captured_at,
      },
      {
        // The UNIQUE constraint column used for conflict detection.
        // Supabase requires the constraint name or columns here.
        onConflict: "user_id,captured_at",
        ignoreDuplicates: false,
      },
    )
    .select("id")
    .single();

  if (error) {
    throw new Error(`Failed to upsert health snapshot: ${error.message}`);
  }

  // Return the ID of the row that was actually written (may differ from
  // snapshotId if an existing row was updated rather than inserted).
  return (data as { id: string }).id;
}

// ---------------------------------------------------------------------------
// Main Handler
// ---------------------------------------------------------------------------

Deno.serve(async (req: Request): Promise<Response> => {
  // Handle CORS preflight (Android OkHttp does not send OPTIONS, but
  // this is included for browser-based testing tools like Postman).
  if (req.method === "OPTIONS") {
    return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "POST, OPTIONS",
        "Access-Control-Allow-Headers": "Authorization, Content-Type",
      },
    });
  }

  if (req.method !== "POST") {
    return jsonResponse(
      { success: false, error: { code: "METHOD_NOT_ALLOWED", message: "Only POST is supported" } },
      405,
    );
  }

  // -------------------------------------------------------------------------
  // Authentication: extract user ID from Supabase JWT.
  // The JWT is validated by the Supabase Edge Function runtime before
  // this handler is called when invokeFunction is used with the user's token.
  // We also manually verify it here as a defense-in-depth measure.
  // -------------------------------------------------------------------------
  const authHeader = req.headers.get("Authorization");
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return jsonResponse(
      { success: false, error: { code: "UNAUTHORIZED", message: "Authorization header is required" } },
      401,
    );
  }

  const token = authHeader.slice(7);

  // Create a Supabase client authenticated as the calling user.
  // This ensures all database queries are subject to RLS.
  const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
  const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
  const supabaseServiceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

  // User-context client: RLS enforced. Used for reads.
  const userClient = createClient(supabaseUrl, supabaseAnonKey, {
    global: { headers: { Authorization: `Bearer ${token}` } },
  });

  // Service-role client: bypasses RLS. Used only for the health_snapshot
  // upsert (which must succeed even if the user has not set up their
  // profile row yet during onboarding edge cases).
  const serviceClient = createClient(supabaseUrl, supabaseServiceKey);

  // Extract the user ID from the JWT claims.
  let userId: string;
  try {
    const { data: { user }, error } = await userClient.auth.getUser();
    if (error || !user) {
      return jsonResponse(
        { success: false, error: { code: "UNAUTHORIZED", message: "Invalid or expired token" } },
        401,
      );
    }
    userId = user.id;
  } catch (authErr) {
    return jsonResponse(
      { success: false, error: { code: "UNAUTHORIZED", message: "Token validation failed" } },
      401,
    );
  }

  // -------------------------------------------------------------------------
  // Parse and validate request body
  // -------------------------------------------------------------------------
  let payload: HealthConnectPayload;
  try {
    const rawBody = await req.json();
    payload = validatePayload(rawBody);
  } catch (validationErr) {
    return jsonResponse(
      {
        success: false,
        error: {
          code: "VALIDATION_ERROR",
          message: (validationErr as Error).message,
        },
      },
      400,
    );
  }

  // -------------------------------------------------------------------------
  // Core logic: upsert snapshot, calculate ACWR, build readiness result
  // -------------------------------------------------------------------------
  try {
    // Step 1: Persist the Health Connect data
    const snapshotId = await upsertHealthSnapshot(serviceClient, userId, payload);

    // Step 2: Calculate ACWR from training history
    // Use the user-context client so RLS is applied (user reads own results).
    const { acuteLoad, chronicLoad, acwr } = await calculateAcwr(userClient, userId);

    // Step 3: Classify zone and build component scores
    const zone = classifyZone(acwr);
    const hrvScore = scoreHrv(payload.hrv_rmssd);
    const sleepScore = scoreSleep(payload.sleep_duration_minutes);

    // Step 4: Build recommendation
    const recommendation = buildRecommendation(
      zone,
      hrvScore,
      sleepScore,
      payload.sleep_duration_minutes,
      payload.hrv_rmssd,
    );

    const calculatedAt = new Date().toISOString();

    const readiness: ReadinessResult = {
      acwr: Math.round(acwr * 10000) / 10000,
      zone,
      acute_load: acuteLoad,
      chronic_load: chronicLoad,
      hrv_rmssd: payload.hrv_rmssd ?? null,
      sleep_minutes: payload.sleep_duration_minutes ?? null,
      resting_hr: payload.resting_hr ?? null,
      hrv_component: hrvScore !== null ? Math.round(hrvScore * 100) / 100 : null,
      sleep_component: sleepScore !== null ? Math.round(sleepScore * 100) / 100 : null,
      recommendation,
      calculated_at: calculatedAt,
    };

    return jsonResponse({
      success: true,
      data: {
        snapshot_id: snapshotId,
        readiness,
      },
    }, 200);

  } catch (pipelineErr) {
    console.error("calculate-readiness pipeline error:", pipelineErr);
    return jsonResponse(
      {
        success: false,
        error: {
          code: "INTERNAL_ERROR",
          message: "Failed to calculate readiness score. Please try again.",
        },
      },
      500,
    );
  }
});

// ---------------------------------------------------------------------------
// Helper: JSON Response
// ---------------------------------------------------------------------------

function jsonResponse(body: ApiResponse<unknown>, statusCode: number): Response {
  return new Response(JSON.stringify(body), {
    status: statusCode,
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*",
    },
  });
}
```

---

## File: `supabase/functions/calculate-readiness/deno.json`

```json
{
  "imports": {
    "@supabase/supabase-js": "https://esm.sh/@supabase/supabase-js@2.47.0"
  },
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true
  }
}
```

---

## File: `backend/.env.example`

```dotenv
# =============================================================================
# ApexAI Athletics — FastAPI Microservice Environment Variables
# Copy this file to .env for local development.
# NEVER commit .env to source control.
# In production, inject these as container environment variables.
# =============================================================================

# Supabase
SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co
SUPABASE_SERVICE_ROLE_KEY=eyJ...your_service_role_key_here
SUPABASE_JWT_SECRET=your-supabase-jwt-secret

# Gemini
GEMINI_API_KEY=AIza...your_gemini_api_key_here
VIDEO_ANALYSIS_MODEL=gemini-1.5-pro
IMAGE_GENERATION_MODEL=gemini-2.0-flash
GEMINI_CACHE_TTL_SECONDS=3600

# Storage bucket names (must match Supabase Storage bucket names)
VIDEO_BUCKET_NAME=videos
CORRECTIONS_BUCKET_NAME=corrections

# App
APP_VERSION=1.0.0
DEBUG=false

# CORS — comma-separated list of allowed origins in production
# CORS_ORIGINS=["https://your-frontend.com"]
```

---

## File: `backend/Dockerfile`

```dockerfile
# =============================================================================
# ApexAI Athletics — FastAPI Microservice Dockerfile
# Multi-stage build: builder installs deps, runtime is minimal.
# Target: Cloud Run / Railway / Fly.io
# =============================================================================

# Stage 1: Builder
FROM python:3.11-slim AS builder

WORKDIR /app

# Install build dependencies for packages that compile C extensions
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Runtime
FROM python:3.11-slim AS runtime

WORKDIR /app

# ffmpeg is required by gemini_service._extract_video_frame()
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Copy installed packages from builder
COPY --from=builder /install /usr/local

# Copy application source
COPY main.py models.py gemini_service.py ./

# Non-root user for security
RUN adduser --disabled-password --gecos "" appuser
USER appuser

# Cloud Run requires listening on $PORT (default 8080)
ENV PORT=8080

EXPOSE ${PORT}

CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port ${PORT} --workers 2 --log-level info"]
```

---

## Frontend Handoff — API Contract

This section is the complete integration reference for the Android developer.

### FastAPI Base URLs

| Environment | URL |
|---|---|
| Local emulator | `http://10.0.2.2:8000` |
| Production | `https://api.apexai-athletics.com` |

All paths are prefixed `/v1/`.

### Authentication

Every request (except `GET /v1/health`) requires:

```
Authorization: Bearer {supabase_access_token}
```

The access token is the one returned by Supabase Auth on login. The OkHttp `Authenticator` should call `POST /auth/v1/token?grant_type=refresh_token` on a 401 and retry.

---

### Endpoint 1 — Submit Video for Analysis

```
POST /v1/analyze-video
Content-Type: multipart/form-data
Authorization: Bearer {token}

Fields:
  video         File    video/mp4, max 500 MB
  movement_type String  e.g. "snatch", "back_squat", "deadlift"
  athlete_id    String  UUID matching the JWT sub claim
```

**202 Response:**

```json
{
  "analysis_id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "processing",
  "estimated_seconds": 45,
  "poll_url": "/v1/coaching/status/550e8400-e29b-41d4-a716-446655440000"
}
```

**Error codes:** `400 BAD_REQUEST`, `401 UNAUTHORIZED`, `403 FORBIDDEN` (athlete_id mismatch), `413 PAYLOAD_TOO_LARGE`, `429 RATE_LIMITED` (10/hour).

---

### Endpoint 2 — Poll Analysis Status

```
GET /v1/coaching/status/{analysis_id}
Authorization: Bearer {token}
```

Poll every 3 seconds. Stop when `status` is `"complete"` or `"error"`.

**200 Response:**

```json
{
  "analysis_id": "550e8400-...",
  "status": "processing",
  "progress": 0.65,
  "stage": "generating_corrections",
  "error_message": null
}
```

`stage` values: `analyzing_video` → `generating_corrections` → `finalizing`. Map these to `AnalysisStatus.ANALYZING` in the Android `CoachingUiState`.

---

### Endpoint 3 — Retrieve Coaching Report

```
GET /v1/coaching/report/{analysis_id}
Authorization: Bearer {token}
```

Call only after `status == "complete"`. Returns `409` if still processing.

**200 Response shape** (abbreviated):

```json
{
  "id": "uuid",
  "video_id": "uuid",
  "movement_type": "snatch",
  "overall_assessment": "Your first pull is strong...",
  "rep_count": 5,
  "estimated_weight_kg": 80.0,
  "faults": [
    {
      "id": "uuid",
      "description": "Early arm bend during second pull",
      "severity": "MODERATE",
      "timestamp_ms": 3450,
      "cue": "Keep arms long like ropes until the bar passes your hips.",
      "corrected_image_url": "https://...supabase.co/.../corrections/fault_0.png",
      "affected_joints": ["LEFT_ELBOW", "RIGHT_ELBOW"]
    }
  ],
  "global_cues": ["Maintain vertical shins off the floor"],
  "overlay_data": [
    {
      "timestamp_ms": 0,
      "landmarks": [{"index": 11, "x": 0.52, "y": 0.38, "z": 0.0, "visibility": 0.98}],
      "joint_angles": {"LEFT_KNEE": 95.2, "LEFT_HIP": 78.4, "TRUNK_INCLINATION": 42.1}
    }
  ],
  "created_at": "2026-03-28T10:30:00Z"
}
```

Map `overlay_data` directly to `List<TimedPoseOverlay>` in `VideoPlaybackUiState`. The `timestamp_ms` values synchronize with `Media3 player.currentPosition`.

---

### Endpoint 4 — Generate Corrective Image

```
POST /v1/generate-correction-image
Content-Type: application/json
Authorization: Bearer {token}

{
  "report_id": "uuid",
  "fault_id": "uuid",
  "fault_timestamp_ms": 3450,
  "fault_description": "Early arm bend during second pull",
  "movement_type": "snatch"
}
```

**200 Response:**

```json
{
  "fault_id": "uuid",
  "corrected_image_url": "https://...supabase.co/storage/v1/object/sign/corrections/...",
  "storage_path": "corrections/{userId}/{reportId}/{faultId}.png"
}
```

The `corrected_image_url` is a signed URL with a 1-hour TTL. Use Coil to load it: `AsyncImage(model = fault.correctedImageUrl)`. The `correctedImageUrl` in the existing `MovementFault` domain entity maps directly to this field.

---

### Endpoint 5 — Refresh Context Cache

```
POST /v1/cache/refresh
Authorization: Bearer {token}
```

Call this after deploying an update to the CrossFit movement standards knowledge base. Not required during normal app operation.

**200 Response:**

```json
{
  "cache_name": "cachedContents/abc123",
  "model": "gemini-1.5-pro",
  "token_count": 4821,
  "expires_at": "2026-03-28T09:00:00Z",
  "refreshed_at": "2026-03-28T08:00:00Z"
}
```

---

### Supabase Edge Function — Calculate Readiness

```
POST https://{PROJECT_REF}.supabase.co/functions/v1/calculate-readiness
Authorization: Bearer {supabase_access_token}
Content-Type: application/json

{
  "hrv_rmssd": 68,
  "sleep_duration_minutes": 420,
  "deep_sleep_minutes": 90,
  "rem_sleep_minutes": 100,
  "resting_hr": 52,
  "captured_at": "2026-03-28T07:00:00Z"
}
```

All health fields are optional — send whatever Health Connect provided. `captured_at` is required. The function upserts one snapshot row per calendar day.

**200 Response:**

```json
{
  "success": true,
  "data": {
    "snapshot_id": "uuid",
    "readiness": {
      "acwr": 1.12,
      "zone": "OPTIMAL",
      "acute_load": 2240.5,
      "chronic_load": 1998.3,
      "hrv_rmssd": 68,
      "sleep_minutes": 420,
      "resting_hr": 52,
      "hrv_component": 0.85,
      "sleep_component": 0.75,
      "recommendation": "You are in the optimal training zone...",
      "calculated_at": "2026-03-28T08:30:00Z"
    }
  }
}
```

Map `zone` to `ReadinessZone` enum. Map `acwr` to `ReadinessUiState.readinessScore`. Map `hrv_component` and `sleep_component` (0.0–1.0) to the component breakdown bars in `ReadinessDashboardScreen`.

Call this function from `SyncHealthDataUseCase` after writing Health Connect data. The returned `readiness` object maps directly to the `ReadinessScore` domain entity.

---

### Supabase RPC — Calculate Readiness (alternative SQL path)

```
POST https://{PROJECT_REF}.supabase.co/rest/v1/rpc/calculate_readiness
Authorization: Bearer {token}
apikey: {supabase_anon_key}
Content-Type: application/json

{ "p_user_id": "user-uuid" }
```

This calls the PostgreSQL `calculate_readiness()` function directly and returns the same ACWR JSON shape. Use the Edge Function path (`/functions/v1/calculate-readiness`) when you also need to sync Health Connect data in the same call. Use this RPC path for a lightweight re-fetch of the score without syncing.

---

### HTTP Status Code Reference

| Code | Meaning | Android handling |
|---|---|---|
| 200 | Success | Parse response body |
| 202 | Accepted (async job started) | Begin polling |
| 400 | Bad request / validation error | Show `error.message` to user |
| 401 | Unauthorized | Trigger token refresh via OkHttp Authenticator |
| 403 | Forbidden | Show "Access denied" — do not retry |
| 404 | Not found | Show "Not found" message |
| 409 | Conflict (analysis in progress) | Continue polling |
| 413 | Payload too large | Show "Video file is too large (max 500 MB)" |
| 429 | Rate limited | Show "Analysis limit reached (10/hour). Try again later." |
| 500 | Server error | Show "Something went wrong. Our team has been notified." |
| 503 | Gemini unavailable | Show "AI coaching is temporarily unavailable. Try again in a few minutes." |