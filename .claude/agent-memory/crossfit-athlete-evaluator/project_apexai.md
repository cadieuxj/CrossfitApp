---
name: ApexAI Athletics app overview
description: Architecture, feature inventory, and known gaps for the ApexAI Athletics CrossFit Android app
type: project
---

ApexAI Athletics is an Android app (Kotlin/Compose) built for CrossFit athletes.

**Why:** Positioned as an AI-powered coaching and training companion for competitive CrossFit athletes.
**How to apply:** Use this context to frame all evaluation feedback and prioritization relative to competitive CrossFit use cases.

## Architecture
- Language: Kotlin, Jetpack Compose
- DI: Hilt
- Backend: Supabase
- AI: Google Gemini (video movement analysis)
- Health data: Android Health Connect (HRV, sleep, resting HR)
- Video: CameraX + Media3/ExoPlayer
- Pose detection: on-device MediaPipe (landmarks, joint angles, barbell tracking)
- Depth: ARCore DepthPoseFuser — graceful fallback to 2D if hardware unsupported

## Features Present (as of 2026-03-28 v2 evaluation)
- WOD logging: score (string), RxD toggle, RPE 1-10, notes (500 char), session duration stepper (1-240 min), auto PR detection
- Score parsing: ROUNDS_PLUS_REPS (R+R format), TIME (MM:SS), LOAD (kg), numeric (reps)
- TimeDomain enum: AMRAP, EMOM, RFT, TABATA, FOR_TIME, MAX_WEIGHT, CALORIES (FOR_TIME now present)
- ScoringMetric enum: REPS, TIME, LOAD, ROUNDS_PLUS_REPS
- WodDetailScreen: movement list with reps/weight/equipment, per-movement camera record button, Start Timer + Log Result CTAs
- Live camera: pose overlay, joint angle readouts with human-readable JOINT_LABEL_MAP (L.Knee, R.Knee, etc.), FPS, 3D DEPTH / 2D POSE badge, recording timer MM:SS, pause (Outlined.Pause icon), stop, discard with confirmation AlertDialog
- VisionViewModel: CameraX PERFORMANCE mode, ARCore DepthPoseFuser, MediaPipe, flip camera, graceful error handling
- CoachingReport: faults with MINOR/MODERATE/CRITICAL severity color-coded cards, fault timestamp, cue, corrected image, "View in Video" link; AI disclaimer row with AlertTriangle icon; share button
- Readiness dashboard: CircularReadinessRing, ACWR (section labeled "Training Load Ratio" / "Acute:Chronic Workload 7/28 day"), ring label 1 decimal place, HRV/sleep/resting HR biometric cards, zone labels (OPTIMAL/CAUTION/HIGH_RISK/UNDERTRAINED/ONBOARDING), scientific disclaimer, morning check-in entry card
- WellnessCheckInScreen: three sliders (1-5): soreness, perceived readiness, mood/stress — color-coded NeonGreen/Warning/Error; 30-second completion target
- PR dashboard: grouped by category, "NEW" badge for PRs within 7 days, relative date formatting, empty state with Browse Workouts CTA; filter button (stub)
- HomeScreen: readiness summary card, today's WOD card, recent PRs row (horizontal scroll), quick action row (camera, WOD, PRs)
- Health Connect integration for biometric data sync with last-synced timestamp
- Auth: email/password via Supabase

## Verified Fixes (2026-03-28 v4 review — 4 design/UX fixes)

### Fix A — HomeScreen design language rewrite: CONFIRMED FIXED (with one minor discrepancy)
- All `MaterialTheme.typography.*` replaced with `ApexTypography.*` — zero MaterialTheme references remain.
- All `Icons.Default.*` / `Icons.Filled.*` replaced with `Icons.Outlined.*` — confirmed by grep (no Default/Filled imports).
- All `Card`/`CardDefaults` replaced with `ApexCard` — confirmed.
- `CornerSmall`/`CornerMedium` theme tokens used in `ReadinessSummaryCard` background — confirmed.
- MINOR DISCREPANCY: `TodayWodCard` uses `Icons.Outlined.FitnessCenter` (line 236) as the decorative icon inside the card. The spec stated FitnessCenter→WbSunny for WOD. `WbSunny` is correctly used in `QuickActionCard` for the WOD quick-action button (line 333). The TodayWodCard body icon was not migrated — still FitnessCenter. Low visual impact but inconsistent with stated intent.

### Fix B — Dead stub buttons removed: FULLY CONFIRMED
- `WodDetailScreen.kt`: `actions = {}` on line 83 — Share button gone, confirmed zero "Share|share" grep matches.
- `CoachingReportScreen.kt`: `actions = {}` on line 98 — Share button gone, confirmed zero matches.
- `PrDashboardScreen.kt`: `actions = {}` on line 63 — Filter button gone, confirmed zero "SlidersHorizontal|Filter" matches.
- All three dead stubs cleanly removed with empty lambda, no residual imports.

### Fix C — FaultTimeline composable added: CONFIRMED IMPLEMENTED, WELL ENGINEERED
- Located at lines 365-445 of CoachingReportScreen.kt.
- Canvas composable draws a rounded track (4dp height) + colored dots per fault with glow ring effect (25% alpha at 1.8× radius).
- Severity sizing: CRITICAL 8dp radius, MODERATE 6dp, MINOR 4dp — good visual hierarchy.
- Dot X position = `(timestampMs / maxMs) * width` — correctly proportional.
- Legend row: colored 8dp circles with CRITICAL/MODERATE/MINOR labels.
- Duration range label: "0:00 → {maxFaultTimestamp}" — shows clip length estimate.
- Placed above fault list (lines 221-225), only rendered when `faults.isNotEmpty()`.
- ONE KNOWN LIMITATION: The timeline infers total video duration from the max fault timestamp (line 367: `faults.maxOf { it.timestampMs }.coerceAtLeast(1L)`). If the last fault occurs at 0:30 but the clip is 2:00, the timeline scale is wrong — last fault dot always renders at the right edge. Not a crash, but can misrepresent fault density.

### Fix D — DurationPickerSection long-press acceleration: CONFIRMED IMPLEMENTED
- Uses `combinedClickable` (ExperimentalFoundationApi) on both − and + boxes.
- `onLongClick`: coroutine launched, 400ms initial delay, then loops at 150ms intervals with ±5 step.
- Decrement loop guard: `while (held > 1)` with `coerceAtLeast(1)` — correct lower bound.
- Increment loop guard: `while (held < 240)` with `coerceAtMost(240)` — correct upper bound.
- Decrement button conditionally applies `combinedClickable` only when `canDecrement` is true (`durationMinutes != null && current > 1`) — correct null-safety.
- Hint text updated: "1–240 min  •  hold to jump ±5" — line 395.
- ONE BEHAVIORAL CONCERN: The long-press coroutine has no external cancellation mechanism (e.g., `InteractionSource` or a `isHeld` flag). If the user releases mid-loop, the coroutine will continue firing until `held` reaches the bound. For a 240-min ceiling this resolves quickly, but at low values (e.g., start at 200, hold decrement) the loop fires ~39 additional iterations after release before stopping at 1. Not a crash — just slight overshoot. Acceptable for V1 pilot.

## Verified Bug Fixes (2026-03-28 v3 review — 8 targeted fixes)

### Fix 1 — PrDashboardScreen `remember` shadow: CONFIRMED FIXED
File ends cleanly at line 204 (only a blank line after the extension function). The `private val remember = @Composable { ... }` line is gone. No compile-breaking shadow present.

### Fix 2 — HomeScreen `readiness?.score` crash: CONFIRMED FIXED
Lines 168-174: `val score = when (readiness?.zone) { OPTIMAL->85, UNDERTRAINED->55, CAUTION->35, HIGH_RISK->15, ONBOARDING/null->0 }`. Zone-to-int mapping is exhaustive, null-safe, and correct. No `.score` property call anywhere in the file.

### Fix 3 — WodDetailScreen non-exhaustive `when()`: CONFIRMED FIXED
Lines 173-181: `when (wod.timeDomain)` now handles all 7 cases — AMRAP, EMOM, RFT, TABATA, FOR_TIME (BlazeOrange), MAX_WEIGHT (NeonGreen), CALORIES (ElectricBlue). Fully exhaustive, no compiler warning possible.

### Fix 4 — WellnessCheckIn wired to persistence: CONFIRMED FIXED
- WellnessCheckInViewModel.kt EXISTS at `feature/readiness/presentation/` (not `feature/wellness/` as spec stated — minor path discrepancy, no impact).
- ViewModel is @HiltViewModel, injects ReadinessRepository + SupabaseClient.
- `submit()` builds a HealthSnapshot with soreness/perceivedReadiness/moodScore, calls `repository.syncHealthSnapshot(snapshot)`, sends NavigateBack on success or ShowError on failure.
- WellnessCheckInScreen takes `viewModel: WellnessCheckInViewModel` parameter.
- LaunchedEffect collects effects: NavigateBack triggers onNavigateBack(), ShowError sets errorMessage.
- AlertDialog renders on non-null errorMessage with OK dismissal.
- Submit button calls `viewModel.submit(soreness.toInt(), perceivedReadiness.toInt(), mood.toInt())`.
- Full persistence loop confirmed end-to-end.

### Fix 5 — RPE tap targets increased: CONFIRMED FIXED
Lines 308-337 (RpeSelector): Row with `Modifier.weight(1f).height(48.dp)` on each Box. Full-width Row, 10 equal segments at 48dp height. Gloves-on usable.

### Fix 6 — Duration decrement disabled when null: CONFIRMED FIXED
Lines 353-363 (IconButton decrement): `enabled = durationMinutes != null && current > 1`. Text color also conditionally dimmed to TextSecondary when disabled. Guard is correct.

### Fix 7 — Biometric card icons fixed: CONFIRMED FIXED
- HRV: `Icons.Outlined.MonitorHeart` (line 289)
- Sleep: `Icons.Outlined.Bedtime` (line 309)
- Resting HR: `Icons.Outlined.Favorite` (line 323)
All three are semantically correct and use the Outlined style matching the app's icon language.

### Fix 8 — Slider descriptions added: CONFIRMED FIXED
`WellnessSlider` has a `description: String = ""` parameter. All three call sites pass descriptions:
- Soreness: "Rate your whole-body muscle soreness right now"
- Perceived Readiness: "How ready do you feel to train hard today?"
- Mood/Stress: "How is your mental state? 1 = highly stressed, 5 = calm & motivated"
Description renders as `bodySmall` below the label inside the card, conditionally shown when `description.isNotBlank()`.

## Verified Improvements (2026-03-28 v2 review — all confirmed correct)
- FOR_TIME, MAX_WEIGHT, CALORIES added to TimeDomain enum
- JOINT_LABEL_MAP: all 11 joints mapped to short human-readable labels
- 3D DEPTH (NeonGreen) / 2D POSE (ElectricBlue) badge in AngleReadoutsRow
- Discard confirmation AlertDialog with Cancel / Discard (ColorError) buttons
- Pause icon is Icons.Outlined.Pause (correct)
- AI disclaimer on CoachingReportScreen: AlertTriangle + text at bottom of report
- ACWR section header "Training Load Ratio" + subtitle "Acute:Chronic Workload (7/28 day)"
- ACWR ring label uses String.format("%.1f", score) — 1 decimal place
- Morning wellness check-in card on ReadinessDashboard linking to WellnessCheckInScreen
- DurationPickerSection: minus/plus stepper, 1-240 min, tooltip "session duration x RPE" explanation

## Pass 5 Evaluation — 2026-03-28 (4 targeted fixes from Pass 3 gaps)

### Fix 1 — FaultTimeline clip duration scaling: CONFIRMED FIXED
- `CoachingReport` domain model now has `clipDurationMs: Long? = null` (Models.kt line 162).
- `CoachingReportRow` DTO has `clip_duration_ms: Long? = null` (CoachingRepositoryImpl.kt line 42).
- Migration 004 adds `clip_duration_ms BIGINT` column to `coaching_reports` (line 45).
- `FaultTimeline` composable signature: `FaultTimeline(faults, clipDurationMs: Long? = null)` (line 381).
- `maxMs = (clipDurationMs ?: faults.maxOf { it.timestampMs }).coerceAtLeast(1L)` — correct fallback (line 382).
- `toDomain()` maps `clip_duration_ms` to domain field (line 148).
- End-to-end data flow is complete: DB -> DTO -> domain -> composable. FULLY FIXED.
- ONE REMAINING GAP: The backend (FastAPI/Gemini) must actually populate `clip_duration_ms` on the coaching_reports row. The Android client correctly consumes it, but if the server never writes it, clipDurationMs will always be null and timeline falls back to max fault timestamp. This is a backend implementation risk, not a client bug.

### Fix 2 — Long-press stepper overshoot: CONFIRMED FIXED
- Replaced `combinedClickable` with `pointerInput` + `awaitEachGesture` + `awaitFirstDown` + `waitForUpOrCancellation` (WodLogScreen.kt lines 369-395 and 407-433).
- `holdJob = scope.launch { ... }` starts on finger-down; `waitForUpOrCancellation()` immediately cancels it on release.
- `longPressTriggered` boolean prevents tap-vs-hold ambiguity.
- Coroutine is structurally cancelled on pointer release — overshoot bug is gone. FULLY FIXED.

### Fix 3 — Share with Coach: CONFIRMED IMPLEMENTED
- `SecondaryButton("Share with Coach")` in `CoachingReportScreen` bottom bar, conditioned on `uiState.report != null` (lines 114-124).
- `shareReport()` top-level function builds formatted plain-text summary with all required fields plus AI disclaimer (lines 462-494).
- Opens `Intent.ACTION_SEND` via `createChooser` — works with any app on share sheet.
- NOTE: Share is plain-text only. No PDF export, no structured data. Coach receives a text dump, not an in-app link. Functional but basic for professional coaching workflows.

### Fix 4 — Benchmark WODs seeded: CONFIRMED IMPLEMENTED (with one factual issue)
- 8 benchmark workouts seeded: Fran, Cindy, Murph, Annie, Chelsea, Diane, Grace, Helen.
- 18 movements seeded in `movements` table with category, muscles, equipment.
- All `ON CONFLICT DO NOTHING` for idempotency.
- Workout movements table populated with prescribed reps/weights and sort order.
- FACTUAL ISSUE: Grace seeds `mvmt-clean` (Clean) but the workout is Clean AND JERK. A clean is half the movement. The movement should be `Clean and Jerk`. This is a coaching accuracy problem — an athlete logging Grace will have "Clean" in their movement history, which misrepresents what they actually did and will not correctly auto-detect C&J PRs.
- FACTUAL ISSUE: Helen KB swing prescribes 24.0 kg (53 lb men's Rx is correct). Weight is accurate.
- Annie seeds 50 reps for both movements — correct for the opening round, but the description says 50-40-30-20-10. The movement rows only list sort_order without capturing the descending rep scheme. This is a schema limitation, not a seed error — but coaches looking at movement data won't see the 50-40-30-20-10 structure.

## Verified Fixes (Pass 6 — 2026-03-28)

### Fix A — Grace movement corrected: CONFIRMED FIXED
- `005_benchmark_wods.sql` line 125: `('wod-grace', 'mvmt-clean-jerk', 30, 61.0, 1)` — movement ID is now `mvmt-clean-jerk`.
- `mvmt-clean-jerk` movement row (line 24) has name `'Clean and Jerk'`, category `'Olympic'`, muscles `ARRAY['Full Body']`, equipment `'Barbell'`.
- Grace workout description (line 64) reads `'30 Clean and Jerks (135/95 lb) for time.'` — consistent.
- No `mvmt-clean` entry exists anywhere in the file. The old incorrect reference is completely gone.
- PR attribution will now correctly credit C&J PRs. FULLY FIXED.

### Fix B — uploadVideo() real implementation: CONFIRMED FIXED
- `CoachingRepositoryImpl.kt` line 62: constructor signature is `@Inject constructor(@ApplicationContext private val context: Context, ...)` — proper Hilt injection.
- Lines 77-79: `context.contentResolver.openInputStream(videoUri)?.use { stream -> stream.readBytes() } ?: error("Unable to open video URI: $videoUri")` — real bytes read from content URI.
- Line 92: `"file_size_bytes" to videoBytes.size.toLong()` — accurate file size persisted.
- Lines 98 and 108: progress emissions use `videoBytes.size.toLong()` as totalBytes — correct.
- No `ByteArrayOutputStream`, no bare `android.app.Application()` call, no placeholder code anywhere in the file. FULLY FIXED.

## Remaining Gaps (updated after Pass 6 — 2026-03-28)

### Still Present — Minor Issues
- TodayWodCard body decorative icon still `Icons.Outlined.FitnessCenter` — minor, incomplete vs stated scope.
- WellnessCheckInViewModel package path mismatch — no runtime impact.
- Annie/Fran/Diane descending rep schemes (21-15-9, 50-40-30-20-10) not representable in current `prescribed_reps` schema — schema limitation affecting all ladder workouts.
- Backend must populate `clip_duration_ms` for FaultTimeline fix to have real effect (client side is correct).

### Still Absent — Strategic Feature Gaps
- No coach portal or coach-view mode — "Share with Coach" is share-sheet text, not structured coach access.
- No competition feature set (Open/Quarterfinal/Semifinal tracking, leaderboard, qualifier calendar).
- No nutrition module.
- No community/benchmark comparison features.
- No WHOOP/Garmin/Oura direct integration — relies solely on Health Connect.
- No training session tagging (AM/PM session, session type: strength vs metcon vs gymnastics).
- No WOTD subscription / coach programming feed.
- UserProfile has no athlete-specific fields: weight class, competition division, affiliate, coach assignment.
- No barbell trajectory chart or rep-by-rep angle trend surfaced in coaching report UI.
