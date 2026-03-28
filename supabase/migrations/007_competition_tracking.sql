-- ============================================================
-- 007_competition_tracking.sql
-- Competition Season Hub: events calendar, standings, and
-- official submission flag on results.
-- ============================================================

-- ── Competition events table ─────────────────────────────────
CREATE TABLE IF NOT EXISTS competition_events (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    competition_type TEXT NOT NULL CHECK (competition_type IN ('OPEN','QUARTERFINALS','SEMIFINALS','GAMES','LOCAL','VIRTUAL')),
    status          TEXT NOT NULL DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING','ACTIVE','COMPLETED')),
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    description     TEXT,
    leaderboard_url TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE competition_events IS
    'CrossFit season calendar events. Seeded by admin; athletes cannot insert.';

-- ── Competition standings (manual entry by athlete) ──────────
CREATE TABLE IF NOT EXISTS competition_standings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    event_id        TEXT NOT NULL REFERENCES competition_events(id) ON DELETE CASCADE,
    workout_name    TEXT NOT NULL,
    score           TEXT NOT NULL,
    score_numeric   DOUBLE PRECISION,
    division        TEXT DEFAULT 'RX',
    rank_overall    INT,
    rank_age_group  INT,
    percentile      DOUBLE PRECISION,
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, event_id, workout_name)
);

-- ── Official submission flag on results ──────────────────────
ALTER TABLE results
    ADD COLUMN IF NOT EXISTS is_official_submission BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN results.is_official_submission IS
    'TRUE when athlete logged this result as their official competition submission.';

-- ── RLS ──────────────────────────────────────────────────────
ALTER TABLE competition_events    ENABLE ROW LEVEL SECURITY;
ALTER TABLE competition_standings ENABLE ROW LEVEL SECURITY;

-- Everyone can read events
CREATE POLICY "events_select_all" ON competition_events
    FOR SELECT USING (true);

-- Athletes manage their own standings
CREATE POLICY "standings_select_own" ON competition_standings
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "standings_insert_own" ON competition_standings
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "standings_update_own" ON competition_standings
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "standings_delete_own" ON competition_standings
    FOR DELETE USING (auth.uid() = user_id);

-- ── Seed 2026 CrossFit Open ───────────────────────────────────
INSERT INTO competition_events (id, name, competition_type, status, start_date, end_date, description)
VALUES
    ('cf-open-2026', '2026 CrossFit Open', 'OPEN', 'UPCOMING',
     '2026-02-26', '2026-03-17',
     'The CrossFit Open is the first stage of the CrossFit Games season. '
     'Three workouts released over three weeks. Open to everyone.'),
    ('cf-open-2025', '2025 CrossFit Open', 'OPEN', 'COMPLETED',
     '2025-02-27', '2025-03-18',
     'The 2025 CrossFit Open. Three workouts over three weeks.'),
    ('cf-qf-2026-na-east', '2026 Quarterfinals — NA East', 'QUARTERFINALS', 'UPCOMING',
     '2026-05-07', '2026-05-11',
     'Top 10% of Open qualifiers compete in the North America East Quarterfinals.'),
    ('local-box-throwdown-2026', 'Box Spring Throwdown 2026', 'LOCAL', 'UPCOMING',
     '2026-04-19', '2026-04-19',
     'Local in-house throwdown. Sign up at the front desk.')
ON CONFLICT (id) DO NOTHING;
