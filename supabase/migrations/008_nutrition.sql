-- ============================================================
-- 008_nutrition.sql
-- Nutrition Macro Tracker: daily targets and manual meal log.
-- No external food database — athlete enters macros manually.
-- ============================================================

-- ── Macro targets (one row per user; upsert on change) ───────
CREATE TABLE IF NOT EXISTS macro_targets (
    user_id            UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    calories_kcal      INT NOT NULL DEFAULT 2500,
    protein_g          INT NOT NULL DEFAULT 175,
    carbs_g            INT NOT NULL DEFAULT 250,
    fat_g              INT NOT NULL DEFAULT 80,
    rest_day_calories  INT,          -- NULL = same as training day
    rest_day_protein_g INT,
    rest_day_carbs_g   INT,
    rest_day_fat_g     INT,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE macro_targets IS
    'Per-user daily macro targets. Optional rest-day targets for periodization.';

-- ── Macro entries (individual meal / food logs) ───────────────
CREATE TABLE IF NOT EXISTS macro_entries (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    logged_date DATE NOT NULL,
    meal_type   TEXT NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','DINNER','SNACK','PRE_WORKOUT','POST_WORKOUT')),
    food_name   TEXT NOT NULL,
    calories    INT NOT NULL DEFAULT 0,
    protein_g   DOUBLE PRECISION NOT NULL DEFAULT 0,
    carbs_g     DOUBLE PRECISION NOT NULL DEFAULT 0,
    fat_g       DOUBLE PRECISION NOT NULL DEFAULT 0,
    notes       TEXT,
    logged_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS macro_entries_user_date
    ON macro_entries (user_id, logged_date DESC);

COMMENT ON TABLE macro_entries IS
    'Manual meal entries. One row per food item. Aggregated client-side for daily totals.';

-- ── Common food hints (static autofill, no API) ──────────────
CREATE TABLE IF NOT EXISTS common_foods (
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL UNIQUE,
    serving_g   INT NOT NULL DEFAULT 100,
    calories    INT NOT NULL,
    protein_g   DOUBLE PRECISION NOT NULL,
    carbs_g     DOUBLE PRECISION NOT NULL,
    fat_g       DOUBLE PRECISION NOT NULL,
    category    TEXT    -- e.g. 'Protein', 'Carbs', 'Dairy', 'Vegetables'
);

COMMENT ON TABLE common_foods IS
    'Static autofill hint table. Athletes can search by name and auto-fill macros.';

-- ── RLS ──────────────────────────────────────────────────────
ALTER TABLE macro_targets ENABLE ROW LEVEL SECURITY;
ALTER TABLE macro_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE common_foods  ENABLE ROW LEVEL SECURITY;

CREATE POLICY "targets_own" ON macro_targets
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);

CREATE POLICY "entries_select_own" ON macro_entries
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "entries_insert_own" ON macro_entries
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "entries_update_own" ON macro_entries
    FOR UPDATE USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
CREATE POLICY "entries_delete_own" ON macro_entries
    FOR DELETE USING (auth.uid() = user_id);

-- Everyone can read common foods (public reference data)
CREATE POLICY "foods_select_all" ON common_foods
    FOR SELECT USING (true);

-- ── Seed common CrossFit athlete foods ───────────────────────
INSERT INTO common_foods (name, serving_g, calories, protein_g, carbs_g, fat_g, category)
VALUES
    ('Chicken Breast (grilled)',     100, 165, 31.0,  0.0,  3.6, 'Protein'),
    ('Salmon (baked)',               100, 208, 20.0,  0.0, 13.0, 'Protein'),
    ('Eggs (whole, large)',           50,  78,  6.3,  0.6,  5.3, 'Protein'),
    ('Greek Yogurt (plain, non-fat)', 170, 100, 17.0, 10.0,  0.7, 'Dairy'),
    ('Cottage Cheese (low-fat)',      113,  90, 12.0,  5.0,  1.5, 'Dairy'),
    ('White Rice (cooked)',           100, 130,  2.7, 28.2,  0.3, 'Carbs'),
    ('Brown Rice (cooked)',           100, 112,  2.6, 23.5,  0.9, 'Carbs'),
    ('Sweet Potato (baked)',          100,  86,  1.6, 20.1,  0.1, 'Carbs'),
    ('Oats (dry)',                     80, 308, 10.7, 55.7,  5.3, 'Carbs'),
    ('Banana (medium)',               118, 105,  1.3, 27.0,  0.4, 'Fruit'),
    ('Blueberries',                   148,  84,  1.1, 21.5,  0.5, 'Fruit'),
    ('Broccoli (steamed)',            100,  35,  2.4,  7.2,  0.4, 'Vegetables'),
    ('Spinach (raw)',                  30,   7,  0.9,  1.1,  0.1, 'Vegetables'),
    ('Avocado',                       100, 160,  2.0,  8.5, 14.7, 'Fat'),
    ('Almonds',                        28, 164,  6.0,  6.1, 14.0, 'Fat'),
    ('Olive Oil',                      14, 119,  0.0,  0.0, 13.5, 'Fat'),
    ('Whey Protein Shake (1 scoop)',   30, 120, 24.0,  3.0,  1.5, 'Supplement'),
    ('Protein Bar (generic)',          68, 230, 20.0, 25.0,  7.0, 'Supplement'),
    ('Whole Milk',                    240, 149,  8.0, 12.0,  8.0, 'Dairy'),
    ('Black Beans (cooked)',          172, 227, 15.2, 40.8,  0.9, 'Protein')
ON CONFLICT (name) DO NOTHING;
