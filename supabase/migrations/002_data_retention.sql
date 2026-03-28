-- ==========================================================================
-- Migration 002 — Data Retention & Automated Destruction
--
-- Quebec Law 25 (Loi 25), Art. 23 requires that personal information be
-- destroyed or anonymised when the purpose for which it was collected has
-- been fulfilled. This migration implements automated retention schedules
-- using pg_cron (available on Supabase Pro and above).
--
-- Retention policy (documented in Privacy Policy):
--   health_snapshots   — 18 months from capture date
--   video_uploads      — 90 days from analysis completion
--   coaching_reports   — life of account (deleted via account erasure)
--   results            — life of account
--   personal_records   — life of account
-- ==========================================================================

-- Enable pg_cron extension (must be enabled in Supabase dashboard first)
-- CREATE EXTENSION IF NOT EXISTS pg_cron;

-- ---------------------------------------------------------------------------
-- 2.1 Health Snapshot Retention — delete records older than 18 months
-- ---------------------------------------------------------------------------
SELECT cron.schedule(
    'purge_old_health_snapshots',
    '0 3 * * *',  -- Run daily at 03:00 UTC
    $$
    DELETE FROM health_snapshots
    WHERE captured_at < NOW() - INTERVAL '18 months';
    $$
);

-- ---------------------------------------------------------------------------
-- 2.2 Video Upload Retention — delete completed analysis records after 90 days
-- Note: Raw video files in Storage must be deleted separately via the
--       backend /v1/account endpoint or a Storage lifecycle rule.
-- ---------------------------------------------------------------------------
SELECT cron.schedule(
    'purge_old_video_uploads',
    '0 4 * * *',  -- Run daily at 04:00 UTC
    $$
    DELETE FROM video_uploads
    WHERE status IN ('complete', 'error')
      AND created_at < NOW() - INTERVAL '90 days';
    $$
);

-- ---------------------------------------------------------------------------
-- 2.3 Orphaned Correction Images — delete movement_faults rows with no
--     parent coaching_report (can happen if a report is partially deleted)
-- ---------------------------------------------------------------------------
SELECT cron.schedule(
    'purge_orphaned_faults',
    '0 5 * * 0',  -- Run weekly on Sunday at 05:00 UTC
    $$
    DELETE FROM movement_faults
    WHERE report_id NOT IN (SELECT id FROM coaching_reports);
    $$
);

-- ---------------------------------------------------------------------------
-- 2.4 Retention audit log table — records when automated purges run
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS retention_audit_log (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    job_name        TEXT        NOT NULL,
    rows_deleted    INT         NOT NULL DEFAULT 0,
    run_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE retention_audit_log IS
    'Audit log for automated data retention/destruction jobs (Law 25, Art. 23)';
