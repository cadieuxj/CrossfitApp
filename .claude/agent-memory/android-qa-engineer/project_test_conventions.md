---
name: ApexAI Athletics — Test Conventions
description: Test naming, structure, tooling, and patterns established for this project in the 2026-03-28 test suite generation
type: project
---

**Test naming convention:** `methodOrScenario_stateUnderTest_expectedBehavior()`

**Frameworks in use:**
- JUnit 4 (`@RunWith(JUnit4::class)` or default) — required for Android instrumentation compatibility
- MockK for all mocking (Kotlin-idiomatic)
- Turbine for Flow/Channel testing (`flow.test { ... }`)
- Compose UI Test (`createComposeRule()`) for all Compose screen tests
- `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`, `TestCoroutineScheduler`)

**Coroutine test rule:** `app/src/test/kotlin/com/apexai/crossfit/TestCoroutineRule.kt` — wraps `StandardTestDispatcher`, sets `Dispatchers.Main` via `Dispatchers.setMain`, auto-reset on teardown.

**Fake repositories:** `app/src/test/kotlin/com/apexai/crossfit/FakeRepositories.kt` — fake implementations of `WodRepository`, `PrRepository`, `ReadinessRepository`, `AuthRepository`. Used in all ViewModel unit tests to avoid Supabase dependency.

**WodLogViewModel construction in tests:** Requires `SavedStateHandle(mapOf("wodId" to "test-wod-id"))`, a fake `WodRepository`, and a real or fake `SubmitResultUseCase`.

**LoginViewModel construction in tests:** Requires fake `LoginUseCase` and `RegisterUseCase`. Uses `Patterns.EMAIL_ADDRESS` (Android util) — tests must use Robolectric or supply valid/invalid email strings that the real regex will pass/fail.

**PlayerPoolManager:** Pool size hardcoded to 2 in `PlayerPoolManager.kt`. When pool exhausted, acquires a temporary player with a warning log rather than blocking or throwing.

**Kinematic angle tests:** Use known 2D landmark coordinates (PointF) and verify output against hand-calculated angles via `Math.acos(dot/(magA*magC))`. Must assert that Z coordinates on `PoseLandmark` do NOT influence angle output.

**ACWR zones (from CLAUDE.md):**
- < 0.8 → UNDERTRAINED
- 0.8–1.3 → OPTIMAL
- 1.3–1.5 → CAUTION
- > 1.5 → HIGH_RISK

**Integration test env vars:** Supabase test project credentials passed via environment variables `SUPABASE_URL` and `SUPABASE_ANON_KEY`. Integration tests annotated `@LargeTest` and skipped when env vars absent.

**Why:** Established 2026-03-28 when generating the initial complete test suite. JUnit 4 chosen over JUnit 5 for Android instrumentation runner compatibility.

**How to apply:** Follow these conventions in all future test files. Do not switch to JUnit 5 unless instrumentation runner support is confirmed.
