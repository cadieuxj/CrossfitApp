# ApexAI Athletics — Code Review Report

**Reviewed Code:** Full codebase generated for the ApexAI Athletics CrossFit Android app — 13 files spanning Android Kotlin (media, vision, WOD, readiness, auth), FastAPI backend, Gemini service, and Supabase SQL migration.
**Agent Addressed:** Frontend Agent (Android/Kotlin), Backend Agent (Python/FastAPI/SQL)
**Date:** 2026-03-28
**Overall Assessment:** Requires Significant Revision

---

## CRITICAL ISSUES — Read These First

Two critical defects must be fixed before any testing begins:

1. The recording discard button in `LiveCameraScreen.kt` has a no-op `onClick = {}` — tapping Discard does nothing, making it impossible to cancel a recording.
2. The `graphicsLayer` and `animateFloat` extension functions in `LiveCameraScreen.kt` are stub overrides that silently swallow the pulse animation — the recording indicator is permanently invisible.

---

## Section 1: Critical Constraint Violations (CLAUDE.md Compliance)

### ExoPlayer Pooling

The pool size is correctly set to 2 (line 29, `PlayerPoolManager.kt`). The pool is pre-initialized in `init`, and `acquire`/`release` follow the expected contract. This constraint is **met**.

One concern: `acquire()` silently creates an overflow player when the pool is exhausted (line 54) with only a logcat warning. The comment says the caller is responsible, but there is no enforcement. In a real `LazyColumn` with more than 2 visible tiles this becomes the failure mode the mandate was written to prevent. The overflow path should `throw IllegalStateException` in production builds rather than silently consuming a third hardware decoder slot. This is not a constraint violation but it does undermine the constraint's intent.

### MediaPipe Running Mode

`setRunningMode(RunningMode.LIVE_STREAM)` is present (line 56, `MediaPipePoseLandmarkerHelper.kt`). `resultListener` and `errorListener` are wired correctly. Z-depth is stored in the landmark model (`.z()` at line 95) for overlay completeness, but the `calculateJointAngles` function uses only `x` and `y` (line 122–129). This constraint is **met**.

### PR Detection

The Android client does not compute PRs. `WodLogViewModel` calls `submitResultUseCase`, which calls `repository.logResult()`. The `WodLogUiState.newPrs` field is populated from the server response (`result.newPrs`, line 106) — the server determines which PRs were set and returns them. The PostgreSQL trigger `check_and_update_pr()` is the only place PR logic lives. This constraint is **met**.

### Health Connect

Only `androidx.health.connect.client.*` APIs are used. No Oura, Garmin, or Apple HealthKit SDKs appear anywhere. The manifest declares the correct Health Connect permissions. This constraint is **met** with one caveat noted in Section 4.

### API Keys

`build.gradle.kts` reads `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `FASTAPI_BASE_URL` from environment variables first and falls back to `local.properties` (lines 34–45). No keys are hardcoded in source. `gemini_service.py` reads `GEMINI_API_KEY` from `os.environ["GEMINI_API_KEY"]` (line 64), raising `KeyError` on startup if absent. The backend uses `pydantic_settings` `BaseSettings` for the remaining secrets. This constraint is **met**.

---

## Section 2: Security Review

### Finding 1 — Supabase RLS: `personal_records` Has No INSERT or UPDATE Policy

- **Location:** `001_initial_schema.sql`, lines 479–481
- **Severity:** HIGH
- **Issue:** RLS is enabled on `personal_records`, but only a `SELECT` policy exists. The comment says writes come from the trigger (SECURITY DEFINER), which is correct — but the lack of an explicit `INSERT` restriction means a sufficiently privileged JWT with service-role or a future code change could write directly from an application client. More importantly, the trigger itself runs as `SECURITY DEFINER`, so the absence of an explicit `INSERT` policy for the `authenticated` role is only safe because `SECURITY DEFINER` bypasses RLS. A future developer who removes `SECURITY DEFINER` from the trigger will silently break the write barrier without any policy to catch it. Add an explicit `DENY` comment or a `WITH CHECK (false)` INSERT/UPDATE policy for the `authenticated` role so the intent is enforced at the schema level.
- **Recommendation:** Add the following directly after line 481:
  ```sql
  -- Explicitly deny direct writes; only the SECURITY DEFINER trigger may write.
  CREATE POLICY "personal_records_no_direct_insert"
      ON personal_records FOR INSERT
      WITH CHECK (false);
  CREATE POLICY "personal_records_no_direct_update"
      ON personal_records FOR UPDATE
      USING (false);
  ```

### Finding 2 — `POST /v1/cache/refresh` Is Not Admin-Protected

- **Location:** `backend/main.py`, lines 906–934
- **Severity:** HIGH
- **Issue:** The `/v1/cache/refresh` endpoint requires only that the caller is authenticated. Any registered athlete can invalidate the Gemini context cache. During a cache rebuild, every concurrent analysis request will either fail (cache miss) or create a second cache, wasting API budget. The code itself notes "In production, this should be restricted to admin users" (line 916) and does not act on that note.
- **Recommendation:** Add an `is_admin` field to the `profiles` table and check it inside `refresh_context_cache`, or read a separate admin claim from the JWT. At minimum, reject the request for any `current_user_id` not in a hardcoded set of admin UUIDs loaded from an environment variable (e.g., `ADMIN_USER_IDS`). Do not ship this endpoint without role restriction.

### Finding 3 — CORS Wildcard Default

- **Location:** `backend/main.py`, line 110
- **Severity:** HIGH
- **Issue:** `cors_origins` defaults to `["*"]`. This means any website can make credentialed requests to the API in a browser context. The comment says "Restrict in production" but there is no enforcement mechanism — nothing prevents deployment with the default intact.
- **Recommendation:** Change the default to `[]` (empty list). Fail fast at startup if `cors_origins` is empty in a non-debug environment:
  ```python
  cors_origins: list[str] = []  # Must be set explicitly in production
  ```
  Add a startup assertion: `if not settings.debug and not settings.cors_origins: raise ValueError("cors_origins must be set in production")`

### Finding 4 — JWT Audience Validation Disabled

- **Location:** `backend/main.py`, line 178
- **Severity:** MEDIUM
- **Issue:** `"verify_aud": False` disables audience claim verification during JWT decoding. The comment explains that Supabase JWTs use `"authenticated"` as the audience. However, disabling audience verification means a JWT signed with the same secret but issued for a different Supabase project or a different audience claim would be accepted. The correct fix is to explicitly specify the expected audience rather than disabling the check entirely.
- **Recommendation:** Replace the option with explicit audience verification:
  ```python
  payload = jwt.decode(
      token,
      settings.supabase_jwt_secret,
      algorithms=["HS256"],
      audience="authenticated",
  )
  ```

### Finding 5 — `movement_type` Form Field Is Not Validated Against an Allowlist

- **Location:** `backend/main.py`, line 517
- **Severity:** MEDIUM
- **Issue:** `movement_type` is accepted as any string with `min_length=1, max_length=100`. This value is interpolated directly into the Gemini prompt (`_build_analysis_prompt`, `gemini_service.py` line 462: `f"...performing the {movement_type}"`) and stored verbatim in `coaching_reports.movement_type` and `video_uploads.movement_type`. A malicious user could inject prompt-manipulation text (e.g., `"snatch. Ignore all previous instructions and..."`). It is also stored in the database without sanitisation.
- **Recommendation:** Validate `movement_type` against the known set of movement names from the database before passing it to Gemini. At minimum, apply a regex allowlist: `re.fullmatch(r"[a-zA-Z0-9 _\-]{1,100}", movement_type)`. Ideally, look up the value against the `movements` table and reject unknowns.

### Finding 6 — `AuthRepositoryImpl` Logs No Auth Errors; Token Stored Without Encryption

- **Location:** `AuthRepositoryImpl.kt`, lines 31–100
- **Severity:** MEDIUM
- **Issue:** Failed authentication is caught by `runCatching` and returned as `Result.failure`. No logging occurs, which makes debugging auth failures in production impossible. More critically, `AuthSession.accessToken` and `refreshToken` are exposed as plain `String` fields on a data class. If this object is ever written to a log, crash reporter, or DataStore without explicit encryption, JWT tokens will leak.
- **Recommendation:** (a) Add structured logging for auth failures (without logging the token itself). (b) Ensure `AuthSession` is never serialised to any persistence layer; only store the refresh token via `EncryptedSharedPreferences` or DataStore Proto with a field-level encryption key. Verify the Supabase Kotlin SDK's session persistence mechanism is encrypted.

### Finding 7 — Supabase Service Role Key Scope in FastAPI

- **Location:** `backend/main.py`, lines 130–138; `get_coaching_report`, line 719
- **Severity:** MEDIUM
- **Issue:** `get_supabase_client()` creates a new `supabase.Client` instance on every call. Creating a Supabase client is not free — it initialises connection pools. The service-role client should be a module-level singleton or injected as a FastAPI dependency with a lifetime scope, not instantiated per-request.
- **Recommendation:** Create the client once in the lifespan handler and inject it as a FastAPI dependency via `Depends`. Remove the in-endpoint `supabase = get_supabase_client()` calls in `analyze_video`, `get_coaching_report`, and `generate_correction_image` and replace them with a dependency parameter.

---

## Section 3: Performance Review

### Finding 8 — `SideEffect` Used Incorrectly for Camera Initialisation

- **Location:** `LiveCameraScreen.kt`, lines 104–106
- **Severity:** HIGH
- **Issue:** `SideEffect` runs after every successful recomposition. This means `viewModel.startCamera(lifecycleOwner, previewView)` is called on every recomposition of `LiveCameraScreen` — not just once. If `startCamera` is not idempotent and guarded by an internal state check, this will reinitialise CameraX on every state update, causing frame drops, lifecycle leaks, and potential camera stream restarts.
- **Recommendation:** Replace `SideEffect` with `LaunchedEffect(Unit)` or `LaunchedEffect(lifecycleOwner)` so camera initialisation fires exactly once:
  ```kotlin
  LaunchedEffect(lifecycleOwner) {
      viewModel.startCamera(lifecycleOwner, previewView)
  }
  ```

### Finding 9 — MediaPipe Bitmap Allocation on Every Camera Frame

- **Location:** `MediaPipePoseLandmarkerHelper.kt`, lines 69–83
- **Severity:** HIGH
- **Issue:** `detectAsync` allocates two new `Bitmap` objects on every camera frame (lines 69 and 78). At 30 fps, this is 60 bitmap allocations per second. Each frame allocation triggers GC pressure which manifests as jank on the UI thread. The rotated bitmap is never recycled.
- **Recommendation:** Pre-allocate the `bitmapBuffer` once and reuse it across frames using a `@GuardedBy` field. Recycle intermediate bitmaps explicitly:
  ```kotlin
  bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer)
  val rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, ...)
  // After building mpImage:
  rotatedBitmap.recycle()
  ```
  Consider using `YuvToRgbConverter` (available in CameraX samples) to avoid the `ARGB_8888` intermediate entirely.

### Finding 10 — `resultListener` and `errorListener` Are Public Mutable Vars (Thread Safety)

- **Location:** `MediaPipePoseLandmarkerHelper.kt`, lines 42–43
- **Severity:** HIGH
- **Issue:** `resultListener` and `errorListener` are `var` fields with no synchronisation. MediaPipe calls `onResult` on its own background thread. If the ViewModel sets or clears these listeners on the main thread while MediaPipe is mid-callback, there is a data race. On ARM processors with relaxed memory ordering, the listener reference may be partially visible.
- **Recommendation:** Use `@Volatile` on both fields, or expose them via thread-safe setter methods with `@Synchronized`. Alternatively, funnel all results through a `Channel` or `MutableSharedFlow` owned by the helper, which is inherently thread-safe and aligns with the coroutine architecture.

### Finding 11 — `LaunchedEffect` Collects Effects Inside the Root Composable

- **Location:** `LiveCameraScreen.kt`, lines 71–78
- **Severity:** MEDIUM
- **Issue:** `viewModel.effects.collect` is launched inside a `LaunchedEffect(Unit)` that runs once. If the screen is recomposed and the ViewModel is replaced (e.g., during configuration change), the coroutine scope is tied to the old ViewModel's effects flow. The current code is safe only because `VisionViewModel` is `@HiltViewModel` and survives configuration changes. However, this is fragile — any future change that causes ViewModel re-creation would orphan the effects collector. The same pattern appears at lines 71–78.
- **Recommendation:** Use `collectAsStateWithLifecycle` for effects, or wrap the collect call in `LaunchedEffect(viewModel)` rather than `LaunchedEffect(Unit)` so the collector restarts if the ViewModel reference changes.

### Finding 12 — `AngleReadoutsRow` Calls `jointAngles.entries.toList()` in Composition

- **Location:** `LiveCameraScreen.kt`, line 245
- **Severity:** MEDIUM
- **Issue:** `jointAngles.entries.toList()` is called every time `AngleReadoutsRow` recomposes, which is every frame (angle values change at camera framerate). This creates a new `List` object on every frame, bypassing any `LazyRow` item stability optimisations and forcing full re-layout.
- **Recommendation:** Stabilise the list before passing it into `AngleReadoutsRow` by converting it in the ViewModel or computing it with `remember(jointAngles) { jointAngles.entries.toList() }`. Consider using `@Stable` or `@Immutable` annotations on the `JointAngle` enum-keyed map wrapper.

### Finding 13 — `ReadinessRepositoryImpl.getReadinessHistory` Is a Stub

- **Location:** `ReadinessRepositoryImpl.kt`, lines 77–81
- **Severity:** MEDIUM (Performance / Correctness)
- **Issue:** `getReadinessHistory` always emits an empty list. Any screen consuming this flow will silently show no readiness history, with no error or indication that the feature is not implemented.
- **Recommendation:** Either implement the function (query `health_snapshots` and compute per-day ACWR via the `calculate_readiness` RPC for each day) or throw `NotImplementedError` with a clear message so callers know the feature is absent during development. Do not emit an empty list silently.

---

## Section 4: Correctness Issues

### Finding 14 — ACWR RPC Response Key Mismatch: `sleep_minutes` vs `sleep_duration_minutes`

- **Location:** `001_initial_schema.sql` line 792; `ReadinessRepositoryImpl.kt` line 27
- **Severity:** CRITICAL
- **Issue:** The PostgreSQL `calculate_readiness` function returns the sleep field as `'sleep_minutes'` (SQL line 792). The Kotlin `ReadinessRpcResponse` data class declares the field as `sleep_duration_minutes` (line 27 of `ReadinessRepositoryImpl.kt`). These names do not match. When the RPC response is deserialised, `sleep_duration_minutes` will always be `null` even when the database has valid sleep data. The readiness recommendation will always show "Sleep data unavailable."
- **Recommendation:** Align the names. Either change the SQL to `'sleep_duration_minutes', v_sleep_minutes` or change the Kotlin data class field to `val sleep_minutes: Int? = null` with a `@SerialName("sleep_minutes")` annotation.

### Finding 15 — Recording Discard Button Is a No-Op

- **Location:** `LiveCameraScreen.kt`, line 307
- **Severity:** CRITICAL
- **Issue:** The Discard button's `onClick = {}` is an empty lambda. Tapping it does nothing. No discard event is dispatched to the ViewModel, and the recording is not cancelled. The user has no way to abandon a recording without force-stopping the app.
- **Recommendation:** Dispatch `VisionEvent.DiscardRecording` (which needs to be added to the `VisionEvent` sealed interface) or call `viewModel.onEvent(VisionEvent.StopRecording)` followed by discarding the output file. The ViewModel must handle cleanup of the in-progress recording and reset state to `READY`.

### Finding 16 — `graphicsLayer` Modifier Extension Is a Stub (Recording Pulse Never Animates)

- **Location:** `LiveCameraScreen.kt`, lines 410–411
- **Severity:** CRITICAL
- **Issue:** A private extension function `Modifier.graphicsLayer` is defined at line 410 that returns `this` unchanged. This shadows the real Compose `graphicsLayer` modifier. The `RecordingPulse` composable at line 224 applies `Modifier.graphicsLayer { scaleX = scale; scaleY = scale }`, but since the override discards the block entirely, the red recording dot never scales. The recording indicator is always invisible to the user (it is behind the outer `Box`). Similarly, `private fun animateFloat` at line 413 returns `initialValue` statically, meaning `scale` is always `1f`.
- **Recommendation:** Delete both private stub functions (`graphicsLayer` at line 410 and `animateFloat` at line 413). Import and use the real Compose `graphicsLayer` modifier from `androidx.compose.ui.graphics`. Ensure the `RecordingPulse` composable uses the properly animated `scale` value from `infiniteTransition.animateFloat` rather than a local shadow.

### Finding 17 — Health Connect Permission Check Uses String Literals Instead of Type-Safe Constants

- **Location:** `HealthConnectDataSource.kt`, lines 30–33
- **Severity:** MEDIUM
- **Issue:** The required permissions are hard-coded as raw strings:
  ```kotlin
  "android.permission.health.READ_HEART_RATE_VARIABILITY"
  "android.permission.health.READ_SLEEP"
  "android.permission.health.READ_HEART_RATE"
  ```
  The Health Connect library provides type-safe permission constants via `HealthPermission.getReadPermission(...)`. Using raw strings means a typo or SDK rename will silently fail the permission check and return `false`, blocking Health Connect access with no indication of what went wrong. Note also that the manifest declares `READ_RESTING_HEART_RATE` as a required permission, but the permission check set does not include it.
- **Recommendation:** Replace string literals with typed permission constants:
  ```kotlin
  val required = setOf(
      HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
      HealthPermission.getReadPermission(SleepSessionRecord::class),
      HealthPermission.getReadPermission(HeartRateRecord::class),
      HealthPermission.getReadPermission(RestingHeartRateRecord::class),
  )
  ```
  Add `RestingHeartRateRecord` to the set to match the manifest declaration.

### Finding 18 — PR Trigger Does Not Fire for Workouts With `score_numeric IS NULL`

- **Location:** `001_initial_schema.sql`, lines 568–571
- **Severity:** MEDIUM
- **Issue:** The trigger function exits immediately if `score_numeric IS NULL OR score_numeric <= 0`. This is documented in the code. However, `ROUNDS_PLUS_REPS` format results (e.g., "5+12") will almost never have a `score_numeric` value set by the client unless it parses and converts the format. The `WodLogViewModel` passes `state.score.toDoubleOrNull()` (line 99) which returns `null` for "5+12" strings. This means all AMRAP WODs will never trigger PR detection for athletes who use standard CrossFit scoring notation.
- **Recommendation:** Either (a) add client-side parsing for `ROUNDS_PLUS_REPS` notation in `WodLogViewModel` to produce a single comparable numeric (e.g., `rounds * 1000 + reps`), or (b) extend the trigger to handle the string parsing in SQL. The simpler fix is (a): parse "5+12" into `5012.0` before setting `scoreNumeric`. Document the encoding convention in both client and trigger code.

### Finding 19 — `calculate_readiness` ACWR Formula Divides 28-Day Sum by 4 — Matches Spec but Has Edge-Case Risk

- **Location:** `001_initial_schema.sql`, lines 709–716
- **Severity:** LOW
- **Issue:** The chronic load is computed as `SUM(28 days) / 4.0`, yielding the average weekly load. This is mathematically equivalent to the CLAUDE.md spec (`rolling average over 28 days`). However, the GREATEST guard at line 717 forces `v_chronic` to at minimum `0.01` even when the athlete has never trained. For a brand-new athlete with zero workouts, `v_acute = 0` and `v_chronic = 0.01`, so `v_acwr = 0.0`, which correctly triggers `UNDERTRAINED`. This is acceptable but the boundary condition should be explicitly tested.
- **Recommendation:** Add a database-level test or comment explaining what the output should be for a new athlete with zero results. No code change required unless the team wants `NULL` instead of `0.0` returned for athletes with insufficient data.

---

## Section 5: Missing Implementations

### Finding 20 — `VisionEffect.ShowError` Is Silently Ignored

- **Location:** `LiveCameraScreen.kt`, line 76
- **Severity:** HIGH
- **Issue:** The `is VisionEffect.ShowError -> {}` branch in the effects collector is an empty block. Any error raised during camera setup, MediaPipe initialisation, or recording will be silently dropped. The user will see no error message.
- **Recommendation:** Display a `Snackbar` or update `uiState.cameraState` to `ERROR` when this effect is received:
  ```kotlin
  is VisionEffect.ShowError -> {
      // Trigger snackbar via snackbarHostState.showSnackbar(effect.message)
  }
  ```
  Wire a `SnackbarHost` into the `LiveCameraScreen` scaffold or pass a snackbar lambda from the calling navigation graph.

### Finding 21 — `ReadinessRepositoryImpl.getReadinessHistory` Stub (Already noted in Finding 13)

See Finding 13 above. This is a functional gap, not just a performance issue — the readiness history screen will show an empty state indefinitely.

### Finding 22 — No `DiscardRecording` Event in the Vision Event Sealed Interface

- **Location:** Related to Finding 15
- **Severity:** HIGH
- **Issue:** The Discard button UI exists but no `VisionEvent.DiscardRecording` variant exists in the sealed interface. The ViewModel has no handler for recording discard, and the recording file on disk will not be cleaned up.
- **Recommendation:** Add `data object DiscardRecording : VisionEvent` to the sealed interface and implement the handler in `VisionViewModel` to stop recording, delete the output file, and reset state to `READY`.

### Finding 23 — `gemini_service.py` `_parse_analysis_response` Is Incomplete

- **Location:** `gemini_service.py`, lines 524–536
- **Severity:** CRITICAL
- **Issue:** The file was read through line 536, where the `_parse_analysis_response` function begins with `if clean.startswith("` but the code is cut off. If this reflects the actual file state (e.g., the file has a syntax error or the function body was not written), then calling `analyze_video` will raise an `AttributeError` or `SyntaxError` at import time, preventing the FastAPI service from starting at all.
- **Recommendation:** Verify the complete content of `gemini_service.py` beyond line 536. If the function body is incomplete, implement it: strip markdown fences, call `json.loads(clean)`, validate that `rep_count`, `faults`, and `overall_assessment` keys exist, and return the dict. Add a fallback that returns a default structure rather than raising if Gemini returns malformed JSON.

### Finding 24 — No Compose Accessibility Semantics on Camera Controls

- **Location:** `LiveCameraScreen.kt`, lines 333–408
- **Severity:** LOW
- **Issue:** `RecordButton` and `StopButton` use `Modifier.clickable` but have no `semantics { contentDescription = "..." }` blocks. These controls are not accessible via TalkBack and will fail any accessibility audit.
- **Recommendation:** Add `Modifier.semantics { contentDescription = "Start recording" }` to `RecordButton` and `"Stop recording"` to `StopButton`.

---

## Section 6: Recommendations (Prioritised)

### P0 — Must Fix Before Any Testing

1. **Fix the `_parse_analysis_response` stub** in `gemini_service.py` — the backend service cannot start without a complete function body. Verify the file is not truncated.
2. **Fix the recording Discard button no-op** (`LiveCameraScreen.kt` line 307) — add `VisionEvent.DiscardRecording` and implement cleanup in the ViewModel.
3. **Delete the stub `graphicsLayer` and `animateFloat` private functions** (`LiveCameraScreen.kt` lines 410–418) — these shadow real Compose APIs and break the recording pulse indicator.
4. **Fix the `sleep_minutes` / `sleep_duration_minutes` key mismatch** between the SQL function and the Kotlin `ReadinessRpcResponse` data class — readiness sleep data is always null without this fix.
5. **Handle `VisionEffect.ShowError`** in `LiveCameraScreen.kt` — all camera and MediaPipe errors are silently swallowed.

### P1 — Should Fix Before Beta Release

6. **Replace `SideEffect` with `LaunchedEffect`** for `viewModel.startCamera(...)` — `SideEffect` on every recomposition is a correctness and performance defect.
7. **Replace Health Connect permission string literals** with type-safe `HealthPermission.getReadPermission(...)` constants and add `RestingHeartRateRecord` to the permission set.
8. **Restrict `/v1/cache/refresh`** to admin users — any authenticated user can currently invalidate the Gemini cache.
9. **Fix CORS wildcard default** in `backend/main.py` — change default to empty list and enforce non-empty in production builds.
10. **Fix JWT audience validation** — replace `"verify_aud": False` with `audience="authenticated"` in `jwt.decode`.
11. **Validate `movement_type` against an allowlist** before interpolating into Gemini prompts — prompt injection risk.
12. **Pre-allocate and recycle MediaPipe frame bitmaps** to eliminate per-frame allocation pressure on the GC.
13. **Fix the PR trigger for `ROUNDS_PLUS_REPS` scoring** — implement client-side parsing of "N+M" notation so `score_numeric` is populated for AMRAP workouts.
14. **Implement `getReadinessHistory`** in `ReadinessRepositoryImpl` — stub silently returns empty data.
15. **Make `resultListener`/`errorListener` thread-safe** in `MediaPipePoseLandmarkerHelper` using `@Volatile` or a Channel.

### P2 — Nice to Have Improvements

16. Stabilise `jointAngles.entries.toList()` in `AngleReadoutsRow` with `remember(jointAngles)` to reduce unnecessary recomposition during live camera frames.
17. Move `get_supabase_client()` to a singleton/dependency-injected pattern in the FastAPI backend to avoid connection pool churn.
18. Add an explicit `WITH CHECK (false)` INSERT policy on `personal_records` to document and enforce the trigger-only write contract at the schema level.
19. Wrap the pool-exhausted path in `PlayerPoolManager.acquire()` to throw in debug builds (`if (BuildConfig.DEBUG) throw IllegalStateException(...)`) while logging-and-creating in release, making over-budget decoder usage visible in tests.
20. Add a startup assertion in `main.py` that fails fast if `cors_origins` is empty in a non-debug deployment.
21. Add accessibility `contentDescription` semantics to `RecordButton` and `StopButton` for TalkBack compliance.
22. Invest in a `core-testing` module with fake Health Connect and Supabase implementations before beta; currently no test coverage exists for the readiness, auth, or WOD paths.

---

## Summary of Required Actions (Ordered by Priority)

1. Verify `gemini_service.py` is not truncated — if `_parse_analysis_response` is incomplete, the backend will not start. (P0 — Backend Agent)
2. Add `VisionEvent.DiscardRecording`, implement its ViewModel handler, and wire the Discard button's `onClick` in `LiveCameraScreen.kt`. (P0 — Frontend Agent)
3. Delete the private `graphicsLayer` and `animateFloat` stub functions in `LiveCameraScreen.kt` so the real Compose APIs are used and the recording indicator animates. (P0 — Frontend Agent)
4. Align the `calculate_readiness` SQL return key from `'sleep_minutes'` to `'sleep_duration_minutes'` (or update the Kotlin DTO) so sleep data is not always null. (P0 — Backend Agent)
5. Add error display for `VisionEffect.ShowError` in `LiveCameraScreen.kt` instead of the empty block. (P0 — Frontend Agent)
6. Change `SideEffect` to `LaunchedEffect(lifecycleOwner)` for `viewModel.startCamera(...)`. (P1 — Frontend Agent)
7. Replace Health Connect permission string literals with type-safe `HealthPermission.getReadPermission()` and add `RestingHeartRateRecord`. (P1 — Frontend Agent)
8. Add admin role check to `POST /v1/cache/refresh`. (P1 — Backend Agent)
9. Change CORS default to empty list and add production startup guard. (P1 — Backend Agent)
10. Replace `"verify_aud": False` with `audience="authenticated"` in JWT decode. (P1 — Backend Agent)
11. Add `movement_type` allowlist validation before Gemini prompt interpolation. (P1 — Backend Agent)
12. Pre-allocate and recycle MediaPipe bitmaps in `detectAsync`. (P1 — Frontend Agent)
13. Implement ROUNDS_PLUS_REPS numeric parsing in `WodLogViewModel`. (P1 — Frontend Agent)
14. Implement `getReadinessHistory` or make it throw `NotImplementedError`. (P1 — Frontend Agent)
15. Add `@Volatile` to `resultListener`/`errorListener` in `MediaPipePoseLandmarkerHelper`. (P1 — Frontend Agent)

---

## Optional Improvements

- Stabilise `jointAngles.entries.toList()` in `AngleReadoutsRow` with `remember(jointAngles)`.
- Refactor Supabase client creation in the FastAPI backend to a module-level singleton.
- Add explicit `WITH CHECK (false)` INSERT/UPDATE policies on `personal_records`.
- Make `PlayerPoolManager.acquire()` throw in debug builds on pool exhaustion.
- Add TalkBack `contentDescription` semantics to camera control buttons.
- Establish a `core-testing` module with fake Health Connect and Supabase implementations before beta.