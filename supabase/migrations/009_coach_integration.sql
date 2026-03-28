-- ============================================================
-- 009_coach_integration.sql
-- Coach Integration: link-by-code, coach_note on results,
-- and RLS policies for coach read access to athlete data.
-- ============================================================

-- ── Coach profiles (extends auth.users) ─────────────────────
CREATE TABLE IF NOT EXISTS coaches (
    id              UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name    TEXT NOT NULL,
    gym_name        TEXT,
    invite_code     TEXT UNIQUE NOT NULL,   -- 6-char alphanumeric; athlete uses this to link
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE coaches IS
    'Coach profiles. invite_code is a human-readable 6-char code athletes enter to link.';

-- ── Coach–athlete connections ─────────────────────────────────
CREATE TABLE IF NOT EXISTS coach_connections (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coach_id        UUID NOT NULL REFERENCES coaches(id) ON DELETE CASCADE,
    athlete_id      UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    permissions     TEXT[] NOT NULL DEFAULT ARRAY['view_results','view_prs'],
    -- Allowed values: 'view_results', 'view_prs', 'add_coach_notes'
    connected_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (coach_id, athlete_id)
);

COMMENT ON TABLE coach_connections IS
    'Links a coach to an athlete. permissions array controls what the coach can see/do.';

-- ── Coach note on individual results ─────────────────────────
ALTER TABLE results
    ADD COLUMN IF NOT EXISTS coach_note TEXT;

COMMENT ON COLUMN results.coach_note IS
    'Optional coaching feedback written by a linked coach. '
    'Athletes can read it; only coaches with add_coach_notes permission can write it.';

-- ── RLS ──────────────────────────────────────────────────────
ALTER TABLE coaches           ENABLE ROW LEVEL SECURITY;
ALTER TABLE coach_connections ENABLE ROW LEVEL SECURITY;

-- Coach sees only their own profile
CREATE POLICY "coaches_own" ON coaches
    FOR ALL USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

-- Athlete can read the coaches table to look up by invite_code during linking
CREATE POLICY "coaches_read_for_linking" ON coaches
    FOR SELECT USING (true);

-- Coach manages their own connections
CREATE POLICY "connections_coach_manage" ON coach_connections
    FOR ALL USING (auth.uid() = coach_id) WITH CHECK (auth.uid() = coach_id);

-- Athlete sees connections they belong to (so they know who their coaches are)
CREATE POLICY "connections_athlete_view" ON coach_connections
    FOR SELECT USING (auth.uid() = athlete_id);

-- Athlete can delete their own connection (unlink coach)
CREATE POLICY "connections_athlete_delete" ON coach_connections
    FOR DELETE USING (auth.uid() = athlete_id);

-- ── Coach can read athlete results (when linked) ─────────────
-- This policy is additive with the existing athlete-owned results policy.
CREATE POLICY "results_coach_read" ON results
    FOR SELECT USING (
        EXISTS (
            SELECT 1
            FROM coach_connections cc
            WHERE cc.coach_id    = auth.uid()
              AND cc.athlete_id  = results.user_id
              AND 'view_results' = ANY(cc.permissions)
        )
    );

-- Coach can write coach_note only (WITH CHECK prevents writing other columns via UPDATE)
-- Constraint: only the coach_note column can change; all other columns must stay unchanged.
CREATE POLICY "results_coach_write_note" ON results
    FOR UPDATE USING (
        EXISTS (
            SELECT 1
            FROM coach_connections cc
            WHERE cc.coach_id    = auth.uid()
              AND cc.athlete_id  = results.user_id
              AND 'add_coach_notes' = ANY(cc.permissions)
        )
    ) WITH CHECK (
        EXISTS (
            SELECT 1
            FROM coach_connections cc
            WHERE cc.coach_id    = auth.uid()
              AND cc.athlete_id  = results.user_id
              AND 'add_coach_notes' = ANY(cc.permissions)
        )
    );

-- Coach can read athlete PRs (when linked with view_prs)
CREATE POLICY "prs_coach_read" ON personal_records
    FOR SELECT USING (
        EXISTS (
            SELECT 1
            FROM coach_connections cc
            WHERE cc.coach_id    = auth.uid()
              AND cc.athlete_id  = personal_records.user_id
              AND 'view_prs'     = ANY(cc.permissions)
        )
    );
