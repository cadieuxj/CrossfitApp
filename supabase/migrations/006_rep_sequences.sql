-- ============================================================
-- 006_rep_sequences.sql
-- Adds rep_scheme TEXT column to workout_movements so ladder
-- workouts (21-15-9, 50-40-30-20-10, etc.) can display the
-- full descending scheme rather than just the opening round.
--
-- prescribed_reps is retained as the default logging value
-- (the integer used when auto-populating the score input).
-- rep_scheme is display-only — the client renders it instead
-- of "N×" when non-null.
-- ============================================================

ALTER TABLE workout_movements
    ADD COLUMN IF NOT EXISTS rep_scheme TEXT;

COMMENT ON COLUMN workout_movements.rep_scheme IS
    'Human-readable rep ladder for display, e.g. "21-15-9" or "50-40-30-20-10". '
    'NULL for fixed-rep movements. When non-null the client renders this string '
    'instead of the prescribed_reps integer. prescribed_reps remains the default '
    'for score logging.';

-- ── Seed rep schemes for benchmark Girl WODs ───────────────

-- Fran: 21-15-9 Thrusters + Pull-ups
UPDATE workout_movements
SET rep_scheme = '21-15-9'
WHERE workout_id = 'wod-fran';

-- Annie: 50-40-30-20-10 Double-unders + Sit-ups
UPDATE workout_movements
SET rep_scheme = '50-40-30-20-10'
WHERE workout_id = 'wod-annie';

-- Diane: 21-15-9 Deadlifts + Handstand Push-ups
UPDATE workout_movements
SET rep_scheme = '21-15-9'
WHERE workout_id = 'wod-diane';

-- Chelsea and Cindy: fixed reps per round, no ladder — rep_scheme stays NULL

-- Helen: fixed reps per round — rep_scheme stays NULL

-- Murph: fixed total reps (partitioned) — rep_scheme stays NULL
