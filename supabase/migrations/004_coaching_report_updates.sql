-- =============================================================================
-- ApexAI Athletics — Coaching Report Schema Updates
-- Migration: 004_coaching_report_updates.sql
-- Target: Supabase (PostgreSQL 15+)
--
-- Removes fields that were hallucinated by Gemini (overlay_data,
-- estimated_weight_kg) and adds scientifically meaningful replacements
-- (analysis_confidence, prompt_version).
--
-- Scientific peer review findings (2026-03):
--   - overlay_data: LLMs cannot produce valid pixel-space pose coordinates
--     from video. The field was populated with plausible-looking but
--     scientifically invalid numbers. Real 3D pose data is now computed
--     on-device by DepthPoseFuser (ARCore depth API + MediaPipe).
--   - estimated_weight_kg: barbell weight is not reliably inferrable from
--     monocular video without calibration markers. Removed to prevent
--     false precision in coaching recommendations.
--
-- Replacements:
--   - analysis_confidence (HIGH/MEDIUM/LOW): Gemini self-reports confidence
--     based on video quality, occlusion, and movement visibility.
--   - prompt_version (TEXT): tracks the schema version sent to Gemini,
--     enabling longitudinal audit if the prompt changes.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1. Add analysis_confidence enum type
-- ---------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'analysis_confidence') THEN
        CREATE TYPE analysis_confidence AS ENUM ('HIGH', 'MEDIUM', 'LOW');
    END IF;
END
$$;

-- ---------------------------------------------------------------------------
-- 2. Add new columns to coaching_reports
-- ---------------------------------------------------------------------------

ALTER TABLE coaching_reports
    ADD COLUMN IF NOT EXISTS analysis_confidence analysis_confidence NOT NULL DEFAULT 'MEDIUM',
    ADD COLUMN IF NOT EXISTS prompt_version       TEXT                NOT NULL DEFAULT 'v1.0',
    ADD COLUMN IF NOT EXISTS clip_duration_ms     BIGINT;

COMMENT ON COLUMN coaching_reports.clip_duration_ms IS
    'Total duration of the analyzed video clip in milliseconds. '
    'Used by the client FaultTimeline to correctly scale fault markers '
    'relative to the full clip length rather than the last fault timestamp.';

COMMENT ON COLUMN coaching_reports.analysis_confidence IS
    'Gemini self-reported confidence in the analysis quality. '
    'HIGH: clear full-body video; MEDIUM: minor occlusion/angle issues; '
    'LOW: poor quality or significant occlusion.';

COMMENT ON COLUMN coaching_reports.prompt_version IS
    'Version of the Gemini analysis prompt schema used. '
    'Enables longitudinal audit when the prompt changes.';

-- ---------------------------------------------------------------------------
-- 3. Deprecate overlay_data and estimated_weight_kg
--    Columns are retained (not dropped) for backwards compatibility with
--    any existing reports, but new rows will not populate them.
--    They will be dropped in a future migration after the retention period.
-- ---------------------------------------------------------------------------

COMMENT ON COLUMN coaching_reports.overlay_data IS
    '[DEPRECATED 2026-03] LLM-generated pose coordinates — scientifically invalid. '
    'Not populated by prompt_version >= v1.0. '
    'Real 3D pose data is computed on-device by DepthPoseFuser. '
    'Scheduled for removal after 90-day retention window.';

COMMENT ON COLUMN coaching_reports.estimated_weight_kg IS
    '[DEPRECATED 2026-03] Barbell weight estimated from monocular video — unreliable. '
    'Not populated by prompt_version >= v1.0. '
    'Scheduled for removal after 90-day retention window.';

-- ---------------------------------------------------------------------------
-- 4. Index on prompt_version for audit queries
-- ---------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_coaching_reports_prompt_version
    ON coaching_reports (prompt_version);

-- ---------------------------------------------------------------------------
-- 5. Table comment
-- ---------------------------------------------------------------------------

COMMENT ON TABLE coaching_reports IS
    'Gemini AI coaching analysis results. '
    'From prompt_version v1.0: overlay_data and estimated_weight_kg are not populated. '
    'Pose data is computed on-device; analysis_confidence reflects video quality.';
