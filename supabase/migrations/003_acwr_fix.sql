-- =============================================================================
-- ApexAI Athletics — ACWR Scientific Fix + Session-RPE Load Metric
-- Migration: 003_acwr_fix.sql
-- Target: Supabase (PostgreSQL 15+)
--
-- Scientific basis:
--   Gabbett (2016) non-overlapping ACWR formulation:
--     acute  = training load for days 1–7
--     chronic = training load for days 8–28 (excludes the acute window)
--   Including the acute window in the chronic average creates autocorrelation
--   and biases the ratio toward 1.0 regardless of actual load changes.
--
--   Session-RPE load metric (Foster 1998):
--     load = session_duration_minutes × perceived_exertion (RPE 1–10)
--   This is a validated, dimensionally consistent load unit that applies
--   equally to all CrossFit modalities (lifting, metcons, gymnastics).
--   Previous schema mixed duration (seconds), kg, and reps — dimensionally
--   incoherent and meaningless as a workload accumulator.
--
-- Changes:
--   1. Add session_duration_minutes to results
--   2. Replace the ACWR compute_health_snapshot() function with the
--      correct non-overlapping formulation using session-RPE load
--   3. Add ONBOARDING guard: suppress ACWR recommendation when fewer than
--      7 results exist (insufficient chronic window data)
--   4. Default RPE coerced to 7 when null (replaces previous default of 5)
--      Rationale: 7/10 is the median RPE for a standard CrossFit class;
--      5/10 systematically under-counts load for athletes who skip RPE entry.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Add session_duration_minutes to results
-- ---------------------------------------------------------------------------

ALTER TABLE results
    ADD COLUMN IF NOT EXISTS session_duration_minutes INT
        CHECK (session_duration_minutes BETWEEN 1 AND 240);

COMMENT ON COLUMN results.session_duration_minutes IS
    'Duration of the workout session in minutes. Combined with rpe to compute '
    'session-RPE training load (Foster 1998): load = duration × rpe.';

-- ---------------------------------------------------------------------------
-- 2. Replace ACWR compute function with non-overlapping Gabbett 2016 formulation
-- ---------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION compute_health_snapshot(p_user_id UUID)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_result_count     INT;
    v_acute_load       NUMERIC;
    v_chronic_load     NUMERIC;
    v_acwr             NUMERIC;
    v_snapshot_id      UUID;
    v_soreness         INT;
    v_perceived        INT;
    v_mood             INT;
BEGIN
    -- Count results in the chronic window (28 days) for ONBOARDING guard
    SELECT COUNT(*) INTO v_result_count
    FROM results
    WHERE user_id = p_user_id
      AND logged_at >= NOW() - INTERVAL '28 days';

    -- Acute load: session-RPE summed over days 1–7
    -- COALESCE(rpe, 7): use 7 as default RPE (median CrossFit class intensity)
    -- COALESCE(session_duration_minutes, 20): use 20 min as conservative default
    SELECT COALESCE(
        SUM(COALESCE(session_duration_minutes, 20) * COALESCE(rpe, 7)),
        0
    ) INTO v_acute_load
    FROM results
    WHERE user_id = p_user_id
      AND logged_at >= NOW() - INTERVAL '7 days';

    -- Chronic load: session-RPE averaged over days 8–28 (non-overlapping window)
    -- Divides total load by 21 days (the width of the chronic window) to produce
    -- a consistent daily average regardless of training frequency.
    SELECT COALESCE(
        SUM(COALESCE(session_duration_minutes, 20) * COALESCE(rpe, 7)),
        0
    ) / 21.0 INTO v_chronic_load
    FROM results
    WHERE user_id = p_user_id
      AND logged_at >= NOW() - INTERVAL '28 days'
      AND logged_at <  NOW() - INTERVAL '7 days';

    -- ACWR ratio (null if ONBOARDING or zero chronic load)
    IF v_result_count < 7 OR v_chronic_load = 0 THEN
        v_acwr := NULL;  -- ONBOARDING: insufficient data, suppress recommendation
    ELSE
        v_acwr := v_acute_load / v_chronic_load;
    END IF;

    -- Pull latest subjective wellness scores from health_snapshots if available
    SELECT soreness_score, perceived_readiness, mood_score
    INTO v_soreness, v_perceived, v_mood
    FROM health_snapshots
    WHERE user_id = p_user_id
    ORDER BY synced_at DESC
    LIMIT 1;

    -- Upsert a new snapshot row
    INSERT INTO health_snapshots (
        id,
        user_id,
        acwr_ratio,
        acute_load,
        chronic_load,
        soreness_score,
        perceived_readiness,
        mood_score,
        synced_at
    ) VALUES (
        gen_random_uuid(),
        p_user_id,
        v_acwr,
        v_acute_load,
        v_chronic_load,
        v_soreness,
        v_perceived,
        v_mood,
        NOW()
    );
END;
$$;

COMMENT ON FUNCTION compute_health_snapshot(UUID) IS
    'Computes ACWR using Gabbett (2016) non-overlapping windows: '
    'acute=days 1-7, chronic=days 8-28. Load metric=session-RPE (Foster 1998). '
    'Returns NULL acwr_ratio when <7 results exist (ONBOARDING cold-start guard).';

-- ---------------------------------------------------------------------------
-- 3. Add acwr_ratio column to health_snapshots if not present
-- ---------------------------------------------------------------------------

ALTER TABLE health_snapshots
    ADD COLUMN IF NOT EXISTS acwr_ratio          NUMERIC(5,3),
    ADD COLUMN IF NOT EXISTS acute_load          NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS chronic_load        NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS soreness_score      INT CHECK (soreness_score BETWEEN 1 AND 5),
    ADD COLUMN IF NOT EXISTS perceived_readiness INT CHECK (perceived_readiness BETWEEN 1 AND 5),
    ADD COLUMN IF NOT EXISTS mood_score          INT CHECK (mood_score BETWEEN 1 AND 5);

COMMENT ON COLUMN health_snapshots.acwr_ratio IS
    'Acute:Chronic Workload Ratio (Gabbett 2016). NULL = ONBOARDING (<7 results).';
COMMENT ON COLUMN health_snapshots.acute_load IS
    'Total session-RPE load for days 1-7 (Foster 1998).';
COMMENT ON COLUMN health_snapshots.chronic_load IS
    'Average daily session-RPE load for days 8-28 (non-overlapping chronic window).';
