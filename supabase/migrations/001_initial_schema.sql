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
        'sleep_duration_minutes', v_sleep_minutes,
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
