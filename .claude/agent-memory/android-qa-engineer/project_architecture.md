---
name: ApexAI Athletics — Core Architectural Decisions
description: Critical constraints and architectural patterns for the CrossFit app that must inform all test and implementation work
type: project
---

Clean Architecture + MVVM. ViewModels expose `StateFlow<UiState>` and side-effects via `SharedFlow`/`Channel<UiEffect>`. Screens call `viewModel.onEvent()`. All repository interfaces defined in domain layer.

**Critical constraints from CLAUDE.md:**
- PR detection is handled entirely server-side by a PostgreSQL trigger on `results` INSERT. Android client NEVER computes PRs. Tests for WodLogViewModel should verify that `newPrs` in the result come from the server response, not local logic.
- MediaPipe Z-depth is unreliable. All kinematic angle calculations use only 2D (x, y). Tests MUST verify Z is excluded.
- ExoPlayer pool size is fixed at 2. Never instantiate per-tile. Tests verify pool exhaustion behavior.
- ACWR = acute_load / chronic_load. Optimal: 0.8–1.3. High risk: >1.5. Formula lives server-side (Supabase Edge Function); client-side `AcwrCalculationTest` tests the local domain utility only.
- Never use Firebase/NoSQL — Supabase PostgreSQL only.

**Package root:** `com.apexai.crossfit`

**Key domain models (Models.kt):**
- `Workout`, `WorkoutResult`, `WorkoutResultInput` — WOD domain
- `PersonalRecord`, `PrHistoryEntry`, `PrUnit` — PR domain
- `ReadinessScore`, `ReadinessZone`, `HrvReading`, `SleepSession` — readiness domain
- `PoseOverlayData`, `PoseLandmark`, `JointAngle` — vision domain
- `AuthSession`, `UserProfile` — auth domain

**Why:** Architecture was chosen for testability (repository interfaces injectable as fakes), strict unidirectional data flow (easy to assert state transitions), and hardware access isolation (camera/ML run behind interfaces).

**How to apply:** Always test through the ViewModel/UseCase interface. Never couple tests to Supabase client directly in unit tests — use fakes from `FakeRepositories.kt`.
