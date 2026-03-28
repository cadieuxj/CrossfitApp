-- ============================================================
-- 005_benchmark_wods.sql
-- Seed the benchmark / "Girl" WODs so athletes can search and
-- log against well-known CrossFit workouts out of the box.
-- ============================================================

-- Ensure idempotency: skip if already present
DO $$
BEGIN

-- ── Movements (upsert by name) ──────────────────────────────

INSERT INTO movements (id, name, category, primary_muscles, equipment)
VALUES
  ('mvmt-thruster',      'Thruster',         'Olympic',       ARRAY['Quads','Shoulders','Triceps'],  'Barbell'),
  ('mvmt-pullup',        'Pull-up',          'Gymnastics',    ARRAY['Lats','Biceps'],                'Pull-up Bar'),
  ('mvmt-squat',         'Air Squat',        'Gymnastics',    ARRAY['Quads','Glutes'],               NULL),
  ('mvmt-pushup',        'Push-up',          'Gymnastics',    ARRAY['Chest','Triceps'],              NULL),
  ('mvmt-situp',         'Sit-up',           'Gymnastics',    ARRAY['Abs'],                          NULL),
  ('mvmt-dblunder',      'Double-under',     'Cardio',        ARRAY['Calves','Coordination'],        'Jump Rope'),
  ('mvmt-deadlift',      'Deadlift',         'Olympic',       ARRAY['Hamstrings','Glutes','Back'],   'Barbell'),
  ('mvmt-hspu',          'Handstand Push-up','Gymnastics',    ARRAY['Shoulders','Triceps'],          NULL),
  ('mvmt-muscle-up',     'Muscle-up',        'Gymnastics',    ARRAY['Lats','Triceps','Chest'],       'Rings'),
  ('mvmt-clean-jerk',    'Clean and Jerk',   'Olympic',       ARRAY['Full Body'],                    'Barbell'),
  ('mvmt-row',           'Row (Calories)',   'Cardio',        ARRAY['Back','Legs','Core'],            'Rowing Machine'),
  ('mvmt-burpee',        'Burpee',           'Cardio',        ARRAY['Full Body'],                    NULL),
  ('mvmt-run-400m',      '400 m Run',        'Cardio',        ARRAY['Legs','Cardiovascular'],        NULL),
  ('mvmt-run-1mile',     '1 Mile Run',       'Cardio',        ARRAY['Legs','Cardiovascular'],        NULL),
  ('mvmt-pullup-strict', 'Strict Pull-up',   'Gymnastics',    ARRAY['Lats','Biceps'],                'Pull-up Bar'),
  ('mvmt-dip',           'Ring Dip',         'Gymnastics',    ARRAY['Triceps','Chest'],              'Rings'),
  ('mvmt-kb-swing',      'Kettlebell Swing', 'Weightlifting', ARRAY['Glutes','Hamstrings','Back'],   'Kettlebell'),
  ('mvmt-box-jump',      'Box Jump',         'Plyometric',    ARRAY['Quads','Glutes'],               'Box')
ON CONFLICT (id) DO NOTHING;

-- ── Workouts ────────────────────────────────────────────────

INSERT INTO workouts (id, name, description, time_domain, scoring_metric, time_cap_seconds, rounds)
VALUES
  -- Fran: 21-15-9 Thrusters (43 kg) + Pull-ups, for time
  ('wod-fran',   'Fran',   '21-15-9 reps for time of Thrusters (95/65 lb) and Pull-ups.',
   'FOR_TIME', 'TIME', NULL, NULL),

  -- Cindy: 20 min AMRAP of 5 Pull-ups / 10 Push-ups / 15 Air Squats
  ('wod-cindy',  'Cindy',  'As many rounds as possible in 20 minutes: 5 Pull-ups, 10 Push-ups, 15 Air Squats.',
   'AMRAP', 'ROUNDS_PLUS_REPS', 1200, NULL),

  -- Murph: 1 Mile Run, 100 Pull-ups, 200 Push-ups, 300 Air Squats, 1 Mile Run — partition as needed
  ('wod-murph',  'Murph',  'For time: 1 mile run, 100 pull-ups, 200 push-ups, 300 air squats, 1 mile run. Partition as needed. With a 20 lb vest.',
   'FOR_TIME', 'TIME', NULL, NULL),

  -- Annie: 50-40-30-20-10 Double-unders & Sit-ups, for time
  ('wod-annie',  'Annie',  '50-40-30-20-10 reps for time of Double-unders and Sit-ups.',
   'FOR_TIME', 'TIME', NULL, NULL),

  -- Chelsea: Every minute on the minute for 30 min: 5 Pull-ups, 10 Push-ups, 15 Air Squats
  ('wod-chelsea','Chelsea','Every minute on the minute for 30 minutes: 5 Pull-ups, 10 Push-ups, 15 Air Squats.',
   'EMOM', 'ROUNDS_PLUS_REPS', 1800, 30),

  -- Diane: 21-15-9 Deadlifts (225/155 lb) + Handstand Push-ups, for time
  ('wod-diane',  'Diane',  '21-15-9 reps for time of Deadlifts (225/155 lb) and Handstand Push-ups.',
   'FOR_TIME', 'TIME', NULL, NULL),

  -- Grace: 30 Clean and Jerks for time (135/95 lb)
  ('wod-grace',  'Grace',  '30 Clean and Jerks (135/95 lb) for time.',
   'FOR_TIME', 'TIME', NULL, NULL),

  -- Helen: 3 rounds for time: 400 m Run, 21 KB Swings (53/35 lb), 12 Pull-ups
  ('wod-helen',  'Helen',  '3 rounds for time: 400 m run, 21 Kettlebell Swings (53/35 lb), 12 Pull-ups.',
   'FOR_TIME', 'TIME', NULL, 3)

ON CONFLICT (id) DO NOTHING;

-- ── Workout movements ───────────────────────────────────────

-- Fran
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-fran', 'mvmt-thruster', 21, 43.0, 1),
  ('wod-fran', 'mvmt-pullup',   21, NULL, 2)
ON CONFLICT DO NOTHING;

-- Cindy
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-cindy', 'mvmt-pullup',  5,  NULL, 1),
  ('wod-cindy', 'mvmt-pushup',  10, NULL, 2),
  ('wod-cindy', 'mvmt-squat',   15, NULL, 3)
ON CONFLICT DO NOTHING;

-- Murph
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-murph', 'mvmt-run-1mile', NULL, NULL, 1),
  ('wod-murph', 'mvmt-pullup',    100,  NULL, 2),
  ('wod-murph', 'mvmt-pushup',    200,  NULL, 3),
  ('wod-murph', 'mvmt-squat',     300,  NULL, 4),
  ('wod-murph', 'mvmt-run-1mile', NULL, NULL, 5)
ON CONFLICT DO NOTHING;

-- Annie
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-annie', 'mvmt-dblunder', 50, NULL, 1),
  ('wod-annie', 'mvmt-situp',    50, NULL, 2)
ON CONFLICT DO NOTHING;

-- Chelsea
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-chelsea', 'mvmt-pullup', 5,  NULL, 1),
  ('wod-chelsea', 'mvmt-pushup', 10, NULL, 2),
  ('wod-chelsea', 'mvmt-squat',  15, NULL, 3)
ON CONFLICT DO NOTHING;

-- Diane
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-diane', 'mvmt-deadlift', 21, 102.0, 1),
  ('wod-diane', 'mvmt-hspu',     21, NULL,  2)
ON CONFLICT DO NOTHING;

-- Grace
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-grace', 'mvmt-clean-jerk', 30, 61.0, 1)
ON CONFLICT DO NOTHING;

-- Helen
INSERT INTO workout_movements (workout_id, movement_id, prescribed_reps, prescribed_weight_kg, sort_order)
VALUES
  ('wod-helen', 'mvmt-run-400m', NULL, NULL, 1),
  ('wod-helen', 'mvmt-kb-swing', 21,   24.0, 2),
  ('wod-helen', 'mvmt-pullup',   12,   NULL, 3)
ON CONFLICT DO NOTHING;

END $$;
