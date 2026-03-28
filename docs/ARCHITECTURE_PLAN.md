

# ApexAI Athletics -- Android Architecture Plan

## Version: 1.0 | Date: 2026-03-27 | Prepared for: Frontend Agent + Backend Agent

---

## 1. Executive Summary

ApexAI Athletics is a native Android CrossFit intelligence platform that fuses three distinct technical domains into a single application: (1) relational workout/PR tracking backed by Supabase PostgreSQL, (2) physiological readiness scoring via Android Health Connect and the ACWR algorithm, and (3) real-time and asynchronous AI biomechanical coaching powered by on-device MediaPipe BlazePose and cloud-based Gemini multimodal LLMs.

The app targets competitive CrossFit athletes who need objective kinematic analysis of Olympic weightlifting movements, data-driven readiness assessments, and longitudinal performance tracking -- capabilities that do not exist in any current consumer fitness application.

The architecture follows Clean Architecture with MVVM at the presentation layer, enforcing strict unidirectional data flow. The one-month delivery timeline demands aggressive reuse of managed services (Supabase, Health Connect, MediaPipe) and zero custom infrastructure.

---

## 2. Artifact Analysis Summary

| Artifact | Key Extractions |
|---|---|
| PDF Blueprint | Tech stack rationale, ACWR formula, MediaPipe landmark indices, Gemini dual-model pipeline, context caching economics, CI/CD pipeline design, database schema |
| CLAUDE.md | Confirmed tech stack, layer structure, ExoPlayer pooling mandate, PR detection via PostgreSQL trigger, Z-depth limitations, threading constraints |
| Eng plan.html | 4-week sprint plan: W1 schema+auth+WOD, W2 Health Connect+ACWR+PR, W3 CameraX+MediaPipe+Gemini, W4 beta+profiling+deploy |

**Contradictions found:** None. All artifacts are internally consistent. The PDF and CLAUDE.md are fully aligned.

**Gaps identified:** No wireframes or screen mockups exist. Screen inventory below is derived from feature specifications. Frontend agent should treat the screen list as authoritative and design UI/UX inline.

---

## 3. Project Structure and Module Organization

```
CrossfitApp/
+-- app/                          # Application module -- NavHost, Hilt entry point, MainActivity
+-- core/
|   +-- core-common/              # Shared utilities, extensions, constants
|   +-- core-ui/                  # Design system: theme, colors, typography, shared composables
|   +-- core-model/               # Domain entities (pure Kotlin, zero Android deps)
|   +-- core-data/                # Repository implementations, Supabase client, DataStore
|   +-- core-network/             # Retrofit service interfaces, OkHttp interceptors, DTOs
|   +-- core-database/            # Room DAOs, entities, type converters (local cache)
|   +-- core-health/              # Health Connect client wrapper
|   +-- core-media/               # Media3 player pool manager, CameraX lifecycle helpers
|   +-- core-testing/             # Shared test fakes, fixtures, coroutine test rules
+-- feature/
|   +-- feature-auth/             # Login, registration, profile
|   +-- feature-wod/              # WOD browsing, logging, timer, history
|   +-- feature-pr/               # Personal records dashboard, trends
|   +-- feature-readiness/        # Readiness score dashboard, Health Connect sync
|   +-- feature-vision/           # Live camera, MediaPipe overlay, recording
|   +-- feature-coaching/         # AI coaching report, video playback with kinematic overlay
+-- backend/                      # Python FastAPI microservice (separate deployment)
    +-- app/
    +-- routers/
    +-- services/
    +-- models/
```

### Package Naming Convention

```
com.apexai.crossfit                          # app module
com.apexai.crossfit.core.common
com.apexai.crossfit.core.ui
com.apexai.crossfit.core.ui.theme
com.apexai.crossfit.core.ui.component
com.apexai.crossfit.core.model
com.apexai.crossfit.core.model.entity
com.apexai.crossfit.core.model.enums
com.apexai.crossfit.core.data
com.apexai.crossfit.core.data.repository
com.apexai.crossfit.core.data.datastore
com.apexai.crossfit.core.network
com.apexai.crossfit.core.network.dto
com.apexai.crossfit.core.network.interceptor
com.apexai.crossfit.core.database
com.apexai.crossfit.core.database.dao
com.apexai.crossfit.core.database.entity
com.apexai.crossfit.core.health
com.apexai.crossfit.core.media
com.apexai.crossfit.core.media.player
com.apexai.crossfit.core.media.camera
com.apexai.crossfit.core.testing
com.apexai.crossfit.feature.auth
com.apexai.crossfit.feature.auth.navigation
com.apexai.crossfit.feature.auth.screen
com.apexai.crossfit.feature.auth.viewmodel
com.apexai.crossfit.feature.wod
com.apexai.crossfit.feature.wod.navigation
com.apexai.crossfit.feature.wod.screen
com.apexai.crossfit.feature.wod.viewmodel
com.apexai.crossfit.feature.wod.component
com.apexai.crossfit.feature.wod.usecase
com.apexai.crossfit.feature.pr
com.apexai.crossfit.feature.pr.navigation
com.apexai.crossfit.feature.pr.screen
com.apexai.crossfit.feature.pr.viewmodel
com.apexai.crossfit.feature.readiness
com.apexai.crossfit.feature.readiness.navigation
com.apexai.crossfit.feature.readiness.screen
com.apexai.crossfit.feature.readiness.viewmodel
com.apexai.crossfit.feature.readiness.usecase
com.apexai.crossfit.feature.vision
com.apexai.crossfit.feature.vision.navigation
com.apexai.crossfit.feature.vision.screen
com.apexai.crossfit.feature.vision.viewmodel
com.apexai.crossfit.feature.vision.analyzer
com.apexai.crossfit.feature.vision.overlay
com.apexai.crossfit.feature.coaching
com.apexai.crossfit.feature.coaching.navigation
com.apexai.crossfit.feature.coaching.screen
com.apexai.crossfit.feature.coaching.viewmodel
com.apexai.crossfit.feature.coaching.component
```

### Dependency Direction Rules

```
feature-* --> core-model, core-data, core-ui, core-common
core-data --> core-model, core-network, core-database, core-health
core-network --> core-model
core-database --> core-model
core-media --> (standalone, no core-data dependency)
core-ui --> core-model (for preview data only)

FORBIDDEN:
  core-* --> feature-*
  feature-A --> feature-B (features never depend on each other)
  core-model --> anything (pure Kotlin, leaf node)
```

---

## 4. Architecture Pattern

### Clean Architecture + MVVM

**Rationale:** The app has three fundamentally different data sources (Supabase REST, Health Connect on-device API, CameraX/MediaPipe real-time stream). Clean Architecture isolates each behind repository interfaces, making features testable in isolation. MVVM at the presentation layer maps directly to Compose's state-driven rendering model.

### Layer Responsibilities

```
+---------------------------------------------------------------------+
|  PRESENTATION LAYER                                                  |
|  Jetpack Compose screens + ViewModels                                |
|  - Screens observe StateFlow from ViewModel                         |
|  - User actions dispatched as sealed interface Events                |
|  - Side effects emitted as SharedFlow Effects                        |
+---------------------------------------------------------------------+
        |  exposes: UiState (data class), UiEvent (sealed), UiEffect (sealed)
        v
+---------------------------------------------------------------------+
|  DOMAIN LAYER (core-model + feature use cases)                       |
|  - Pure Kotlin, no Android imports                                   |
|  - Use case classes: single invoke() operator                        |
|  - Repository interfaces defined here                                |
|  - ACWR calculation logic lives here                                 |
|  - Kinematic angle calculation logic lives here                      |
+---------------------------------------------------------------------+
        |  depends on: repository interfaces, domain entities
        v
+---------------------------------------------------------------------+
|  DATA LAYER                                                          |
|  - Repository implementations (core-data)                            |
|  - Supabase REST client (core-network)                               |
|  - Room local cache (core-database)                                  |
|  - Health Connect client (core-health)                               |
|  - CameraX + MediaPipe pipeline (core-media)                         |
|  - DTO <-> Entity mappers                                            |
+---------------------------------------------------------------------+
```

### Communication Pattern

- **Screens** call `viewModel.onEvent(SomeEvent)`.
- **ViewModels** call use cases which return `Flow<T>` or `suspend fun`.
- **Use cases** call repository interfaces.
- **Repositories** implement the interfaces, coordinating between network, cache, and device APIs.
- **ViewModels** expose `StateFlow<UiState>` for the screen to collect.
- **One-shot side effects** (navigation, snackbar, toast) flow through `SharedFlow<UiEffect>`.

---

## 5. Tech Stack Specification

| Component | Choice | Version Target | Rationale |
|---|---|---|---|
| Language | Kotlin | 2.0+ | Required for native hardware access, K2 compiler |
| UI Framework | Jetpack Compose | BOM 2025.01+ | Declarative, state-driven, Canvas overlay support |
| Compose Navigation | Navigation Compose | 2.8+ | Type-safe routes with Kotlin serialization |
| DI | Hilt | 2.51+ | Compile-time DI, ViewModel injection, Android-native |
| Async | Coroutines + Flow | 1.9+ | Structured concurrency for camera, ML, network streams |
| Networking | Retrofit 2 + OkHttp 4 | Latest stable | Supabase REST and FastAPI communication |
| Serialization | Kotlinx Serialization | 1.7+ | JSON parsing for API responses, type-safe nav args |
| Local Cache | Room | 2.7+ | Offline WOD/PR cache, typed queries |
| Preferences | DataStore Proto | 1.1+ | Session tokens, user settings (non-relational) |
| Image Loading | Coil 3 | 3.0+ | Compose-native, coroutine-based |
| Video Playback | Media3 ExoPlayer | 1.5+ | Hardware decoder management, Compose interop |
| Camera | CameraX | 1.4+ | Lifecycle-aware camera, ImageAnalysis pipeline |
| Pose Detection | MediaPipe Tasks Vision | 0.10+ | 33 3D landmarks, LIVE_STREAM mode, on-device |
| Health Data | Health Connect | 1.1+ | Unified wearable data, no per-vendor OAuth |
| AI Backend | Python FastAPI | 0.115+ | Gemini orchestration microservice |
| AI Models | Gemini 3.1/1.5 Pro + Flash | Latest | Multimodal video reasoning + image generation |
| Testing | JUnit 5, MockK, Turbine | Latest | Coroutine/Flow testing |
| UI Testing | Compose UI Test | BOM-aligned | Compose-native assertions |
| Build System | Gradle Kotlin DSL | 8.6+ | Version catalog, convention plugins |
| CI/CD | GitHub Actions + Fastlane | N/A | Automated test/build/sign/deploy |
| minSdk | 26 (Android 8.0) | -- | Health Connect requires 26+, MediaPipe requires 24+ |
| targetSdk | 35 (Android 15) | -- | Current Play Store requirement |
| compileSdk | 35 | -- | Latest stable APIs |

---

## 6. Screen and Navigation Map

### Screen Inventory

| ID | Screen | Feature Module | Description |
|---|---|---|---|
| S01 | SplashScreen | app | Animated logo, auth state check, route to login or home |
| S02 | LoginScreen | feature-auth | Email/password login via Supabase Auth |
| S03 | RegisterScreen | feature-auth | Account creation with athlete profile fields |
| S04 | HomeScreen | app | Dashboard: today's readiness score, recent WODs, quick actions |
| S05 | WodBrowseScreen | feature-wod | Browse/search WOD catalog (AMRAP, EMOM, RFT, Tabata) |
| S06 | WodDetailScreen | feature-wod | Single WOD view: movements, prescribed weights, timer |
| S07 | WodLogScreen | feature-wod | Log completed WOD: score, notes, RPE |
| S08 | WodHistoryScreen | feature-wod | Historical WOD results with filters |
| S09 | WodTimerScreen | feature-wod | Active workout timer (AMRAP countdown, EMOM intervals) |
| S10 | PrDashboardScreen | feature-pr | All PRs grouped by movement category |
| S11 | PrDetailScreen | feature-pr | Single movement PR history with trend chart |
| S12 | ReadinessDashboardScreen | feature-readiness | Composite readiness score, ACWR gauge, HRV, sleep |
| S13 | HealthConnectSetupScreen | feature-readiness | Health Connect permissions flow |
| S14 | LiveCameraScreen | feature-vision | Real-time camera feed with MediaPipe overlay |
| S15 | RecordingReviewScreen | feature-vision | Review recorded video before upload |
| S16 | CoachingReportScreen | feature-coaching | Gemini analysis results: faults, cues, corrected images |
| S17 | VideoPlaybackScreen | feature-coaching | Media3 player with kinematic overlay Canvas |
| S18 | ProfileScreen | feature-auth | User profile, settings, logout |

### Navigation Graph (Compose Navigation)

```
NavHost(startDestination = "splash")
|
+-- "splash" -> SplashScreen
|     |-- authenticated -> "home"
|     |-- unauthenticated -> "auth/login"
|
+-- "auth" (nested graph)
|     +-- "auth/login" -> LoginScreen
|     +-- "auth/register" -> RegisterScreen
|
+-- BottomNavigation (shown on S04, S05, S10, S12, S18)
|     |
|     +-- "home" -> HomeScreen
|     |     +-- "wod/{wodId}" -> WodDetailScreen
|     |     +-- "wod/{wodId}/log" -> WodLogScreen
|     |     +-- "wod/{wodId}/timer" -> WodTimerScreen
|     |
|     +-- "wod" (tab) -> WodBrowseScreen
|     |     +-- "wod/history" -> WodHistoryScreen
|     |     +-- "wod/{wodId}" -> WodDetailScreen
|     |     +-- "wod/{wodId}/log" -> WodLogScreen
|     |     +-- "wod/{wodId}/timer" -> WodTimerScreen
|     |
|     +-- "pr" (tab) -> PrDashboardScreen
|     |     +-- "pr/{movementId}" -> PrDetailScreen
|     |
|     +-- "readiness" (tab) -> ReadinessDashboardScreen
|     |     +-- "readiness/setup" -> HealthConnectSetupScreen
|     |
|     +-- "profile" (tab) -> ProfileScreen
|
+-- "vision" (full-screen, no bottom nav)
|     +-- "vision/live" -> LiveCameraScreen
|     +-- "vision/review/{videoUri}" -> RecordingReviewScreen
|
+-- "coaching" (full-screen, no bottom nav)
      +-- "coaching/report/{analysisId}" -> CoachingReportScreen
      +-- "coaching/playback/{videoId}" -> VideoPlaybackScreen
```

### Bottom Navigation Structure

```
+-------+-------+-----------+----------+---------+
| Home  |  WOD  |  Camera*  | Readiness| Profile |
+-------+-------+-----------+----------+---------+
                     ^
                     |
            FAB-style center button
            Opens vision/live directly
```

The center camera button is a FloatingActionButton overlaid on the BottomNavigation bar. It navigates to `vision/live` as a full-screen destination outside the bottom nav scaffold.

### Back Stack Behavior

- **Auth flow**: Login/Register share a single back stack entry. Successful auth clears the auth graph and navigates to `home` with `popUpTo("splash") { inclusive = true }`.
- **WOD timer**: `WodTimerScreen` sets `popUpTo("wod/{wodId}")` on completion so the timer is removed from the back stack after logging.
- **Vision flow**: `LiveCameraScreen` -> `RecordingReviewScreen` -> `CoachingReportScreen`. Upload confirmation pops back to `home`. Back press from `LiveCameraScreen` returns to previous bottom nav destination.
- **Deep links**: `apexai://wod/{wodId}` and `apexai://coaching/report/{analysisId}` are supported for push notification navigation.

---

## 7. Feature Architecture

### 7.1 Feature: Auth

**Description:** User authentication and profile management via Supabase Auth (email/password). Session token stored in EncryptedSharedPreferences.

**Screens:** S02 LoginScreen, S03 RegisterScreen, S18 ProfileScreen

**ViewModel: AuthViewModel**

```kotlin
// --- UI State ---
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

// --- Events ---
sealed interface AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent
    data class PasswordChanged(val password: String) : AuthEvent
    data class DisplayNameChanged(val name: String) : AuthEvent
    data object LoginClicked : AuthEvent
    data object RegisterClicked : AuthEvent
    data object LogoutClicked : AuthEvent
}

// --- Effects ---
sealed interface AuthEffect {
    data object NavigateToHome : AuthEffect
    data object NavigateToLogin : AuthEffect
    data class ShowError(val message: String) : AuthEffect
}
```

**Use Cases:**
- `LoginUseCase(email, password): Result<AuthSession>`
- `RegisterUseCase(email, password, displayName): Result<AuthSession>`
- `LogoutUseCase(): Result<Unit>`
- `GetCurrentSessionUseCase(): Flow<AuthSession?>`

**Repository Interface:**
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    fun observeSession(): Flow<AuthSession?>
    suspend fun refreshToken(): Result<AuthSession>
}
```

**Domain Entities:**
```kotlin
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val expiresAt: Long
)

data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: Instant,
    val avatarUrl: String?
)
```

---

### 7.2 Feature: WOD

**Description:** Browse, search, and log CrossFit workouts. Supports AMRAP, EMOM, RFT, and Tabata time domains. Includes active workout timer.

**Screens:** S05 WodBrowseScreen, S06 WodDetailScreen, S07 WodLogScreen, S08 WodHistoryScreen, S09 WodTimerScreen

**ViewModel: WodBrowseViewModel**

```kotlin
data class WodBrowseUiState(
    val workouts: List<WorkoutSummary> = emptyList(),
    val searchQuery: String = "",
    val selectedTimeDomain: TimeDomain? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface WodBrowseEvent {
    data class SearchChanged(val query: String) : WodBrowseEvent
    data class TimeDomainSelected(val domain: TimeDomain?) : WodBrowseEvent
    data class WodClicked(val wodId: String) : WodBrowseEvent
}

sealed interface WodBrowseEffect {
    data class NavigateToDetail(val wodId: String) : WodBrowseEffect
}
```

**ViewModel: WodDetailViewModel**

```kotlin
data class WodDetailUiState(
    val workout: Workout? = null,
    val movements: List<WorkoutMovement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface WodDetailEvent {
    data object StartWorkoutClicked : WodDetailEvent
    data object LogResultClicked : WodDetailEvent
}
```

**ViewModel: WodTimerViewModel**

```kotlin
data class WodTimerUiState(
    val workout: Workout? = null,
    val elapsedMillis: Long = 0L,
    val currentRound: Int = 1,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    // EMOM-specific
    val currentIntervalSecondsRemaining: Int = 0
)

sealed interface WodTimerEvent {
    data object StartPause : WodTimerEvent
    data object Reset : WodTimerEvent
    data object Complete : WodTimerEvent
}
```

**ViewModel: WodLogViewModel**

```kotlin
data class WodLogUiState(
    val workout: Workout? = null,
    val score: String = "",            // reps for AMRAP, time for RFT
    val rxd: Boolean = true,           // as prescribed
    val notes: String = "",
    val rpe: Int? = null,              // 1-10 perceived exertion
    val isSubmitting: Boolean = false,
    val newPrs: List<PersonalRecord> = emptyList()  // returned by server after log
)

sealed interface WodLogEvent {
    data class ScoreChanged(val score: String) : WodLogEvent
    data class RxdToggled(val rxd: Boolean) : WodLogEvent
    data class NotesChanged(val notes: String) : WodLogEvent
    data class RpeSelected(val rpe: Int) : WodLogEvent
    data object SubmitClicked : WodLogEvent
}

sealed interface WodLogEffect {
    data class PrAchieved(val prs: List<PersonalRecord>) : WodLogEffect
    data object NavigateBack : WodLogEffect
    data class ShowError(val message: String) : WodLogEffect
}
```

**Use Cases:**
- `GetWorkoutsUseCase(query, timeDomain): Flow<List<WorkoutSummary>>`
- `GetWorkoutDetailUseCase(wodId): Flow<Workout>`
- `LogWorkoutResultUseCase(wodId, score, rxd, notes, rpe): Result<WorkoutResult>`
- `GetWorkoutHistoryUseCase(userId): Flow<List<WorkoutResult>>`

**Repository Interface:**
```kotlin
interface WorkoutRepository {
    fun getWorkouts(query: String?, timeDomain: TimeDomain?): Flow<List<WorkoutSummary>>
    fun getWorkoutById(wodId: String): Flow<Workout>
    fun getWorkoutMovements(wodId: String): Flow<List<WorkoutMovement>>
    suspend fun logResult(result: WorkoutResultInput): Result<WorkoutResult>
    fun getHistory(userId: String): Flow<List<WorkoutResult>>
}
```

**Domain Entities:**
```kotlin
enum class TimeDomain { AMRAP, EMOM, RFT, TABATA }
enum class ScoringMetric { REPS, TIME, LOAD, ROUNDS_PLUS_REPS }

data class Workout(
    val id: String,
    val name: String,
    val description: String,
    val timeDomain: TimeDomain,
    val scoringMetric: ScoringMetric,
    val timeCap: Duration?,
    val rounds: Int?,
    val movements: List<WorkoutMovement>
)

data class WorkoutSummary(
    val id: String,
    val name: String,
    val timeDomain: TimeDomain,
    val movementCount: Int
)

data class WorkoutMovement(
    val id: String,
    val movement: Movement,
    val prescribedReps: Int?,
    val prescribedWeight: Double?,  // kg
    val prescribedDistance: Double?, // meters
    val prescribedCalories: Int?,
    val sortOrder: Int
)

data class Movement(
    val id: String,
    val name: String,
    val category: String,           // Olympic Lifting, Gymnastics, Monostructural, etc.
    val primaryMuscles: List<String>,
    val equipment: String?
)

data class WorkoutResult(
    val id: String,
    val workoutId: String,
    val userId: String,
    val score: String,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?,
    val completedAt: Instant,
    val newPrs: List<PersonalRecord>
)

data class WorkoutResultInput(
    val workoutId: String,
    val score: String,
    val rxd: Boolean,
    val notes: String?,
    val rpe: Int?
)
```

---

### 7.3 Feature: PR

**Description:** Displays personal records auto-detected by the PostgreSQL trigger on `Results` insert. No client-side PR computation. Read-only display with trend charts.

**Screens:** S10 PrDashboardScreen, S11 PrDetailScreen

**ViewModel: PrDashboardViewModel**

```kotlin
data class PrDashboardUiState(
    val prsByCategory: Map<String, List<PersonalRecord>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface PrDashboardEvent {
    data class MovementClicked(val movementId: String) : PrDashboardEvent
}
```

**ViewModel: PrDetailViewModel**

```kotlin
data class PrDetailUiState(
    val movement: Movement? = null,
    val currentPr: PersonalRecord? = null,
    val prHistory: List<PrHistoryEntry> = emptyList(),  // for trend chart
    val isLoading: Boolean = false
)
```

**Use Cases:**
- `GetAllPrsUseCase(userId): Flow<Map<String, List<PersonalRecord>>>`
- `GetPrHistoryUseCase(userId, movementId): Flow<List<PrHistoryEntry>>`

**Repository Interface:**
```kotlin
interface PrRepository {
    fun getAllPrs(userId: String): Flow<Map<String, List<PersonalRecord>>>
    fun getPrHistory(userId: String, movementId: String): Flow<List<PrHistoryEntry>>
}
```

**Domain Entities:**
```kotlin
data class PersonalRecord(
    val id: String,
    val userId: String,
    val movementId: String,
    val movementName: String,
    val category: String,
    val value: Double,          // kg for lifts, reps for bodyweight, seconds for time
    val unit: PrUnit,
    val achievedAt: Instant
)

enum class PrUnit { KG, LBS, REPS, SECONDS }

data class PrHistoryEntry(
    val value: Double,
    val unit: PrUnit,
    val achievedAt: Instant
)
```

---

### 7.4 Feature: Readiness

**Description:** Synthesizes Health Connect biometrics (HRV, sleep, heart rate) with training load data to compute ACWR-based readiness score. ACWR calculation runs server-side via Supabase Edge Function; Health Connect data is read on-device and synced to Supabase.

**Screens:** S12 ReadinessDashboardScreen, S13 HealthConnectSetupScreen

**ViewModel: ReadinessViewModel**

```kotlin
data class ReadinessUiState(
    val readinessScore: Float? = null,          // 0.0 - 2.0+ (ACWR)
    val readinessZone: ReadinessZone? = null,   // OPTIMAL, CAUTION, HIGH_RISK
    val acuteLoad: Float? = null,
    val chronicLoad: Float? = null,
    val latestHrv: Int? = null,                 // ms
    val sleepDuration: Duration? = null,
    val sleepQuality: SleepQuality? = null,
    val healthConnectPermissionsGranted: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ReadinessZone { OPTIMAL, CAUTION, HIGH_RISK, UNDERTRAINED }

enum class SleepQuality { EXCELLENT, GOOD, FAIR, POOR }

sealed interface ReadinessEvent {
    data object RefreshClicked : ReadinessEvent
    data object SyncHealthData : ReadinessEvent
    data object RequestPermissions : ReadinessEvent
}

sealed interface ReadinessEffect {
    data object NavigateToHealthConnectSetup : ReadinessEffect
    data class ShowRecommendation(val message: String) : ReadinessEffect
}
```

**Use Cases:**
- `SyncHealthDataUseCase(): Result<Unit>` -- reads Health Connect, pushes to Supabase
- `GetReadinessScoreUseCase(userId): Flow<ReadinessScore>` -- fetches ACWR from Edge Function
- `CheckHealthConnectPermissionsUseCase(): Result<Boolean>`

**Repository Interface:**
```kotlin
interface HealthRepository {
    suspend fun checkPermissions(): Boolean
    suspend fun requestPermissions(): Boolean
    suspend fun readHrvData(start: Instant, end: Instant): List<HrvReading>
    suspend fun readSleepData(start: Instant, end: Instant): List<SleepSession>
    suspend fun readHeartRateData(start: Instant, end: Instant): List<HeartRateReading>
    suspend fun syncToSupabase(healthSnapshot: HealthSnapshot): Result<Unit>
}

interface ReadinessRepository {
    fun getReadinessScore(userId: String): Flow<ReadinessScore>
    fun getReadinessHistory(userId: String, days: Int): Flow<List<ReadinessScore>>
}
```

**Domain Entities:**
```kotlin
data class ReadinessScore(
    val acwr: Float,
    val zone: ReadinessZone,
    val acuteLoad: Float,
    val chronicLoad: Float,
    val hrvComponent: Float?,
    val sleepComponent: Float?,
    val calculatedAt: Instant,
    val recommendation: String
)

data class HrvReading(
    val value: Int,       // RMSSD in ms
    val timestamp: Instant
)

data class SleepSession(
    val startTime: Instant,
    val endTime: Instant,
    val totalDuration: Duration,
    val deepSleepDuration: Duration,
    val remSleepDuration: Duration,
    val lightSleepDuration: Duration
)

data class HeartRateReading(
    val bpm: Int,
    val timestamp: Instant
)

data class HealthSnapshot(
    val userId: String,
    val hrv: List<HrvReading>,
    val sleep: List<SleepSession>,
    val heartRate: List<HeartRateReading>,
    val capturedAt: Instant
)
```

---

### 7.5 Feature: Vision (Real-Time Pose Detection)

**Description:** Live camera feed with MediaPipe BlazePose overlay rendering joint angles, skeletal connections, and barbell trajectory in real time. Supports video recording for subsequent AI coaching analysis.

**Screens:** S14 LiveCameraScreen, S15 RecordingReviewScreen

**ViewModel: VisionViewModel**

```kotlin
data class VisionUiState(
    val cameraState: CameraState = CameraState.INITIALIZING,
    val isRecording: Boolean = false,
    val recordingDuration: Duration = Duration.ZERO,
    val currentPoseResult: PoseOverlayData? = null,
    val selectedMovement: Movement? = null,
    val fps: Int = 0,
    val error: String? = null
)

enum class CameraState { INITIALIZING, READY, RECORDING, ERROR }

sealed interface VisionEvent {
    data object StartRecording : VisionEvent
    data object StopRecording : VisionEvent
    data object FlipCamera : VisionEvent
    data class MovementSelected(val movement: Movement) : VisionEvent
}

sealed interface VisionEffect {
    data class NavigateToReview(val videoUri: String) : VisionEffect
    data class ShowError(val message: String) : VisionEffect
}
```

**Key Data Structures for Pose Overlay:**
```kotlin
data class PoseOverlayData(
    val landmarks: List<PoseLandmark>,
    val jointAngles: Map<JointAngle, Float>,
    val barbellPosition: PointF?,              // null if no barbell detected
    val barbellTrajectory: List<PointF>,        // historical path
    val frameTimestamp: Long
)

data class PoseLandmark(
    val index: Int,           // 0-32 per BlazePose topology
    val x: Float,             // normalized 0..1
    val y: Float,             // normalized 0..1
    val z: Float,             // depth -- UNRELIABLE, use with caution
    val visibility: Float     // confidence 0..1
)

enum class JointAngle {
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_HIP, RIGHT_HIP,
    LEFT_ELBOW, RIGHT_ELBOW,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ANKLE, RIGHT_ANKLE,
    TRUNK_INCLINATION
}
```

**Use Cases:**
- `StartPoseDetectionUseCase(cameraProvider): Flow<PoseOverlayData>`
- `CalculateJointAnglesUseCase(landmarks): Map<JointAngle, Float>`
- `StartRecordingUseCase(outputUri): Result<Unit>`
- `StopRecordingUseCase(): Result<Uri>`

**Repository Interface:**
```kotlin
interface VisionRepository {
    fun startPoseStream(cameraProvider: ProcessCameraProvider): Flow<PoseOverlayData>
    fun stopPoseStream()
    suspend fun startRecording(outputFile: File): Result<Unit>
    suspend fun stopRecording(): Result<Uri>
}
```

---

### 7.6 Feature: Coaching (AI Video Analysis)

**Description:** Uploads recorded video to FastAPI backend which orchestrates Gemini 3.1/1.5 Pro for movement analysis and Gemini Flash for corrective image generation. Displays structured coaching report with faults, cues, and kinematic overlay playback.

**Screens:** S16 CoachingReportScreen, S17 VideoPlaybackScreen

**ViewModel: CoachingViewModel**

```kotlin
data class CoachingUiState(
    val analysisStatus: AnalysisStatus = AnalysisStatus.IDLE,
    val uploadProgress: Float = 0f,
    val report: CoachingReport? = null,
    val selectedFault: MovementFault? = null,
    val error: String? = null
)

enum class AnalysisStatus { IDLE, UPLOADING, ANALYZING, COMPLETE, ERROR }

sealed interface CoachingEvent {
    data class UploadVideo(val videoUri: Uri) : CoachingEvent
    data class FaultSelected(val fault: MovementFault) : CoachingEvent
    data object RetryAnalysis : CoachingEvent
}

sealed interface CoachingEffect {
    data class NavigateToPlayback(val videoId: String, val timestampMs: Long) : CoachingEffect
}
```

**ViewModel: VideoPlaybackViewModel**

```kotlin
data class VideoPlaybackUiState(
    val videoUrl: String? = null,
    val overlayData: List<TimedPoseOverlay> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val faultMarkers: List<FaultMarker> = emptyList(),
    val isLoading: Boolean = false
)

data class TimedPoseOverlay(
    val timestampMs: Long,
    val landmarks: List<PoseLandmark>,
    val jointAngles: Map<JointAngle, Float>
)

data class FaultMarker(
    val timestampMs: Long,
    val label: String,
    val severity: FaultSeverity
)

enum class FaultSeverity { MINOR, MODERATE, CRITICAL }
```

**Use Cases:**
- `UploadVideoUseCase(videoUri, movementType): Flow<UploadProgress>`
- `GetCoachingReportUseCase(analysisId): Flow<CoachingReport>`
- `GetVideoOverlayDataUseCase(videoId): Flow<List<TimedPoseOverlay>>`

**Repository Interface:**
```kotlin
interface CoachingRepository {
    fun uploadVideo(videoUri: Uri, movementType: String): Flow<UploadProgress>
    fun getReport(analysisId: String): Flow<CoachingReport>
    fun getOverlayData(videoId: String): Flow<List<TimedPoseOverlay>>
}
```

**Domain Entities:**
```kotlin
data class CoachingReport(
    val id: String,
    val videoId: String,
    val movementType: String,
    val overallAssessment: String,
    val repCount: Int,
    val estimatedWeight: Double?,
    val faults: List<MovementFault>,
    val globalCues: List<String>,
    val createdAt: Instant
)

data class MovementFault(
    val id: String,
    val description: String,
    val severity: FaultSeverity,
    val timestampMs: Long,
    val cue: String,                      // coaching instruction
    val correctedImageUrl: String?,       // Gemini Flash generated
    val affectedJoints: List<JointAngle>
)

sealed class UploadProgress {
    data class Uploading(val fraction: Float) : UploadProgress()
    data class Analyzing(val stage: String) : UploadProgress()
    data class Complete(val analysisId: String) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}
```

---

## 8. Data Architecture

### 8.1 Domain Entity Relationships

```
UserProfile (1) ---< (N) WorkoutResult
    |                        |
    |                        v
    |                 Workout (1) ---< (N) WorkoutMovement >--- (1) Movement
    |
    +---< (N) PersonalRecord >--- (1) Movement
    |
    +---< (N) HealthSnapshot
    |         |
    |         +--- HrvReading[]
    |         +--- SleepSession[]
    |         +--- HeartRateReading[]
    |
    +---< (N) ReadinessScore
    |
    +---< (N) CoachingReport >--- (N) MovementFault
    |
    +---< (N) VideoUpload
```

### 8.2 Supabase PostgreSQL Schema

```sql
-- Users (managed by Supabase Auth, extended with profile)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id),
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Movement catalog (seeded from ExerciseDB)
CREATE TABLE movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL UNIQUE,
    category TEXT NOT NULL,           -- 'Olympic Lifting', 'Gymnastics', 'Monostructural', 'Powerlifting'
    primary_muscles TEXT[] NOT NULL,
    secondary_muscles TEXT[],
    equipment TEXT,                    -- 'Barbell', 'Kettlebell', 'Bodyweight', etc.
    biomechanical_class TEXT,         -- 'Push', 'Pull', 'Hinge', 'Squat', 'Carry', 'Rotation'
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Workouts
CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT,
    time_domain TEXT NOT NULL CHECK (time_domain IN ('AMRAP', 'EMOM', 'RFT', 'TABATA')),
    scoring_metric TEXT NOT NULL CHECK (scoring_metric IN ('REPS', 'TIME', 'LOAD', 'ROUNDS_PLUS_REPS')),
    time_cap_seconds INT,
    rounds INT,
    created_by UUID REFERENCES profiles(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Junction: workout <-> movements
CREATE TABLE workout_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_id UUID NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    movement_id UUID NOT NULL REFERENCES movements(id),
    prescribed_reps INT,
    prescribed_weight_kg DECIMAL(6,2),
    prescribed_distance_m DECIMAL(8,2),
    prescribed_calories INT,
    sort_order INT NOT NULL DEFAULT 0
);

-- Athlete results
CREATE TABLE results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id),
    workout_id UUID NOT NULL REFERENCES workouts(id),
    score TEXT NOT NULL,              -- flexible: "155 reps", "12:34", "225 lbs"
    score_numeric DECIMAL(10,2),     -- parsed for comparisons
    rxd BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    rpe INT CHECK (rpe BETWEEN 1 AND 10),
    completed_at TIMESTAMPTZ DEFAULT NOW()
);

-- Personal records (auto-populated by trigger)
CREATE TABLE personal_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id),
    movement_id UUID NOT NULL REFERENCES movements(id),
    value DECIMAL(10,2) NOT NULL,    -- kg, reps, or seconds
    unit TEXT NOT NULL CHECK (unit IN ('KG', 'LBS', 'REPS', 'SECONDS')),
    achieved_at TIMESTAMPTZ DEFAULT NOW(),
    result_id UUID REFERENCES results(id),
    UNIQUE(user_id, movement_id, unit)
);

-- PR detection trigger
CREATE OR REPLACE FUNCTION check_and_update_pr()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO personal_records (user_id, movement_id, value, unit, achieved_at, result_id)
    SELECT
        NEW.user_id,
        wm.movement_id,
        NEW.score_numeric,
        CASE
            WHEN m.category IN ('Olympic Lifting', 'Powerlifting') THEN 'KG'
            WHEN m.category = 'Gymnastics' THEN 'REPS'
            ELSE 'REPS'
        END,
        NEW.completed_at,
        NEW.id
    FROM workout_movements wm
    JOIN movements m ON m.id = wm.movement_id
    WHERE wm.workout_id = NEW.workout_id
    ON CONFLICT (user_id, movement_id, unit) DO UPDATE
    SET value = EXCLUDED.value,
        achieved_at = EXCLUDED.achieved_at,
        result_id = EXCLUDED.result_id
    WHERE EXCLUDED.value > personal_records.value;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_pr
    AFTER INSERT ON results
    FOR EACH ROW
    EXECUTE FUNCTION check_and_update_pr();

-- Health data snapshots (synced from Health Connect)
CREATE TABLE health_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id),
    hrv_rmssd INT,
    sleep_duration_minutes INT,
    deep_sleep_minutes INT,
    rem_sleep_minutes INT,
    resting_hr INT,
    captured_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Video uploads
CREATE TABLE video_uploads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles(id),
    storage_path TEXT NOT NULL,        -- Supabase Storage bucket path
    movement_type TEXT NOT NULL,
    duration_seconds INT,
    status TEXT NOT NULL DEFAULT 'uploaded' CHECK (status IN ('uploaded', 'analyzing', 'complete', 'error')),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Coaching reports (from Gemini analysis)
CREATE TABLE coaching_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id UUID NOT NULL REFERENCES video_uploads(id),
    user_id UUID NOT NULL REFERENCES profiles(id),
    movement_type TEXT NOT NULL,
    overall_assessment TEXT,
    rep_count INT,
    estimated_weight_kg DECIMAL(6,2),
    global_cues TEXT[],
    overlay_data JSONB,                -- serialized List<TimedPoseOverlay>
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Movement faults (child of coaching_reports)
CREATE TABLE movement_faults (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES coaching_reports(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    severity TEXT NOT NULL CHECK (severity IN ('MINOR', 'MODERATE', 'CRITICAL')),
    timestamp_ms BIGINT NOT NULL,
    cue TEXT NOT NULL,
    corrected_image_url TEXT,
    affected_joints TEXT[]
);

-- Row Level Security
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE results ENABLE ROW LEVEL SECURITY;
ALTER TABLE personal_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE health_snapshots ENABLE ROW LEVEL SECURITY;
ALTER TABLE video_uploads ENABLE ROW LEVEL SECURITY;
ALTER TABLE coaching_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE movement_faults ENABLE ROW LEVEL SECURITY;

-- RLS policies (users can only access their own data)
CREATE POLICY "users_own_data" ON profiles FOR ALL USING (auth.uid() = id);
CREATE POLICY "users_own_results" ON results FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "users_own_prs" ON personal_records FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "users_own_health" ON health_snapshots FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "users_own_videos" ON video_uploads FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "users_own_reports" ON coaching_reports FOR ALL USING (auth.uid() = user_id);
CREATE POLICY "users_own_faults" ON movement_faults FOR ALL
    USING (EXISTS (
        SELECT 1 FROM coaching_reports cr WHERE cr.id = report_id AND cr.user_id = auth.uid()
    ));
-- Workouts and movements are readable by all authenticated users
CREATE POLICY "workouts_read" ON workouts FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "movements_read" ON movements FOR SELECT USING (auth.role() = 'authenticated');
```

### 8.3 ACWR Edge Function

```sql
-- Supabase Edge Function: calculate_readiness
-- Called by Android after syncing health data

CREATE OR REPLACE FUNCTION calculate_readiness(p_user_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_acute DECIMAL;
    v_chronic DECIMAL;
    v_acwr DECIMAL;
    v_zone TEXT;
    v_hrv INT;
    v_sleep INT;
    v_recommendation TEXT;
BEGIN
    -- Acute workload: sum of (score_numeric * rpe) over past 7 days
    SELECT COALESCE(SUM(score_numeric * COALESCE(rpe, 5)), 0)
    INTO v_acute
    FROM results
    WHERE user_id = p_user_id
      AND completed_at >= NOW() - INTERVAL '7 days';

    -- Chronic workload: average weekly load over past 28 days
    SELECT COALESCE(SUM(score_numeric * COALESCE(rpe, 5)) / 4.0, 1)
    INTO v_chronic
    FROM results
    WHERE user_id = p_user_id
      AND completed_at >= NOW() - INTERVAL '28 days';

    -- ACWR
    v_acwr := v_acute / GREATEST(v_chronic, 0.01);

    -- Zone classification
    v_zone := CASE
        WHEN v_acwr < 0.8 THEN 'UNDERTRAINED'
        WHEN v_acwr BETWEEN 0.8 AND 1.3 THEN 'OPTIMAL'
        WHEN v_acwr BETWEEN 1.3 AND 1.5 THEN 'CAUTION'
        ELSE 'HIGH_RISK'
    END;

    -- Latest HRV and sleep
    SELECT hrv_rmssd, sleep_duration_minutes
    INTO v_hrv, v_sleep
    FROM health_snapshots
    WHERE user_id = p_user_id
    ORDER BY captured_at DESC
    LIMIT 1;

    -- Recommendation
    v_recommendation := CASE v_zone
        WHEN 'OPTIMAL' THEN 'You are in the optimal training zone. Consider attempting a heavy single or benchmark WOD.'
        WHEN 'CAUTION' THEN 'Training load is elevated. Prioritize technique work and moderate intensity.'
        WHEN 'HIGH_RISK' THEN 'High injury risk detected. Scale to active recovery or mobility session.'
        WHEN 'UNDERTRAINED' THEN 'Training volume is below baseline. Gradually increase load this week.'
    END;

    RETURN jsonb_build_object(
        'acwr', v_acwr,
        'zone', v_zone,
        'acute_load', v_acute,
        'chronic_load', v_chronic,
        'hrv', v_hrv,
        'sleep_minutes', v_sleep,
        'recommendation', v_recommendation,
        'calculated_at', NOW()
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

### 8.4 Room Local Cache Schema

Room is used exclusively for offline caching of read-heavy data. Writes always go to Supabase first; Room is populated from network responses.

```kotlin
@Database(
    entities = [
        CachedWorkout::class,
        CachedMovement::class,
        CachedWorkoutMovement::class,
        CachedResult::class,
        CachedPersonalRecord::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ApexDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun movementDao(): MovementDao
    abstract fun resultDao(): ResultDao
    abstract fun prDao(): PersonalRecordDao
}
```

### 8.5 DataStore Keys

```kotlin
object PrefsKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")       // stored in EncryptedDataStore
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")     // stored in EncryptedDataStore
    val USER_ID = stringPreferencesKey("user_id")
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val HEALTH_CONNECT_SYNCED = booleanPreferencesKey("hc_synced")
    val LAST_HEALTH_SYNC = longPreferencesKey("last_health_sync") // epoch millis
    val SELECTED_UNIT_SYSTEM = stringPreferencesKey("unit_system") // KG or LBS
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}
```

### 8.6 Caching Strategy

| Data Type | Strategy | Rationale |
|---|---|---|
| Movements catalog | Cache-first (seed once, refresh weekly) | 11,000+ entries, rarely changes |
| Workouts | Network-first with Room fallback | User-generated, needs freshness |
| Results/History | Network-first with Room fallback | Must reflect latest PR triggers |
| Personal Records | Network-first, cache on read | PRs computed server-side |
| Health Snapshots | Write-through (device -> Supabase) | Health Connect is source of truth |
| Readiness Score | Network-only (computed server-side) | Always fresh calculation |
| Coaching Reports | Network-first, cache complete reports | Large payloads, read-heavy after creation |
| Video files | Supabase Storage only (no local cache) | Too large for device storage |

---

## 9. API Contract Specification

### 9.1 Supabase REST API

Supabase auto-generates REST endpoints via PostgREST. Base URL: `https://{PROJECT_REF}.supabase.co/rest/v1/`

**Authentication:** All requests include:
```
Authorization: Bearer {access_token}
apikey: {supabase_anon_key}
Content-Type: application/json
```

**Standard Supabase REST Patterns:**

| Operation | Method | URL | Body |
|---|---|---|---|
| List workouts | GET | `/workouts?select=*,workout_movements(*)&order=created_at.desc` | -- |
| Get workout | GET | `/workouts?id=eq.{id}&select=*,workout_movements(*,movements(*))` | -- |
| Log result | POST | `/results` | `{ workout_id, score, score_numeric, rxd, notes, rpe }` |
| Get history | GET | `/results?user_id=eq.{id}&order=completed_at.desc` | -- |
| Get PRs | GET | `/personal_records?user_id=eq.{id}&select=*,movements(name,category)` | -- |
| Sync health | POST | `/health_snapshots` | `{ hrv_rmssd, sleep_duration_minutes, ... }` |
| Calculate readiness | POST | `/rpc/calculate_readiness` | `{ "p_user_id": "{userId}" }` |

### 9.2 FastAPI Coaching Microservice

**Base URL:** Configured via BuildConfig: `https://api.apexai-athletics.com/v1/`

#### POST `/coaching/analyze`

Upload video for Gemini analysis.

**Request:**
```
Content-Type: multipart/form-data
Authorization: Bearer {supabase_access_token}

Fields:
  video: File (video/mp4, max 500MB)
  movement_type: String ("snatch", "clean_and_jerk", "squat", "deadlift", etc.)
  athlete_id: String (UUID)
```

**Success Response (202 Accepted):**
```json
{
  "analysis_id": "uuid-string",
  "status": "processing",
  "estimated_seconds": 45,
  "poll_url": "/coaching/status/{analysis_id}"
}
```

**Error Responses:**
- `400` -- Invalid video format or missing fields
- `401` -- Invalid/expired token
- `413` -- Video exceeds size limit
- `429` -- Rate limited (max 10 analyses/hour/user)
- `503` -- Gemini API unavailable

#### GET `/coaching/status/{analysis_id}`

Poll analysis status.

**Success Response (200):**
```json
{
  "analysis_id": "uuid-string",
  "status": "processing|complete|error",
  "progress": 0.75,
  "stage": "analyzing_video|generating_corrections|finalizing"
}
```

#### GET `/coaching/report/{analysis_id}`

Retrieve completed coaching report.

**Success Response (200):**
```json
{
  "id": "uuid-string",
  "video_id": "uuid-string",
  "movement_type": "snatch",
  "overall_assessment": "Your first pull is strong but the second pull shows early arm bend...",
  "rep_count": 5,
  "estimated_weight_kg": 80.0,
  "faults": [
    {
      "id": "uuid-string",
      "description": "Early arm bend during second pull",
      "severity": "MODERATE",
      "timestamp_ms": 3450,
      "cue": "Keep arms long like ropes until the bar passes your hips. Think about pushing the floor away.",
      "corrected_image_url": "https://storage.apexai-athletics.com/corrections/abc123.png",
      "affected_joints": ["LEFT_ELBOW", "RIGHT_ELBOW"]
    }
  ],
  "global_cues": [
    "Focus on maintaining vertical shins off the floor",
    "Your catch position is excellent -- keep that overhead stability"
  ],
  "overlay_data": [
    {
      "timestamp_ms": 0,
      "landmarks": [
        { "index": 0, "x": 0.52, "y": 0.18, "z": -0.02, "visibility": 0.99 }
      ],
      "joint_angles": {
        "LEFT_KNEE": 95.2,
        "LEFT_HIP": 78.4,
        "LEFT_ELBOW": 172.1
      }
    }
  ],
  "created_at": "2026-03-27T10:30:00Z"
}
```

**Error Responses:**
- `404` -- Analysis not found
- `409` -- Analysis not yet complete (redirect to status endpoint)

### 9.3 Authentication Mechanism

- **Primary:** Supabase Auth (email/password). Returns JWT access token + refresh token.
- **Token storage:** EncryptedSharedPreferences (Android Keystore backed).
- **Token refresh:** OkHttp Authenticator intercepts 401 responses, calls Supabase `/auth/v1/token?grant_type=refresh_token`, retries original request.
- **FastAPI auth:** The FastAPI microservice validates the same Supabase JWT by verifying against the Supabase JWT secret. No separate auth system.

### 9.4 Base URL Configuration

```kotlin
// build.gradle.kts (:app)
android {
    buildTypes {
        debug {
            buildConfigField("String", "SUPABASE_URL", "\"https://xxx.supabase.co\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJ...\"")
            buildConfigField("String", "FASTAPI_BASE_URL", "\"http://10.0.2.2:8000/v1/\"")
        }
        release {
            buildConfigField("String", "SUPABASE_URL", "\"https://xxx.supabase.co\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJ...\"")
            buildConfigField("String", "FASTAPI_BASE_URL", "\"https://api.apexai-athletics.com/v1/\"")
        }
    }
}
```

### 9.5 Retry and Rate Limiting Policy

| API | Retry Strategy | Rate Limit |
|---|---|---|
| Supabase REST | Exponential backoff, 3 retries, base 1s | PostgREST default (1000 req/s) |
| FastAPI upload | No retry on upload (idempotency risk) | 10 analyses/hour/user (server-enforced) |
| FastAPI poll | Fixed 3s interval, timeout after 120s | No limit |
| Supabase Auth | Exponential backoff, 3 retries | GoTrue default |

---

## 10. State Management

### 10.1 Global App State

```kotlin
// Managed by Hilt singleton, observed throughout the app
@Singleton
class AppStateManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val connectivityMonitor: ConnectivityMonitor
) {
    val authState: StateFlow<AuthState>          // AUTHENTICATED, UNAUTHENTICATED, LOADING
    val isOnline: StateFlow<Boolean>             // network connectivity
    val currentUserId: StateFlow<String?>
}

sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(val session: AuthSession) : AuthState
    data object Unauthenticated : AuthState
}
```

### 10.2 ViewModel State Exposure Pattern

Every ViewModel follows this pattern:

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val someUseCase: SomeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ExampleUiState())
    val state: StateFlow<ExampleUiState> = _state.asStateFlow()

    private val _effects = Channel<ExampleEffect>(Channel.BUFFERED)
    val effects: Flow<ExampleEffect> = _effects.receiveAsFlow()

    fun onEvent(event: ExampleEvent) {
        when (event) {
            is ExampleEvent.SomeAction -> handleSomeAction(event)
        }
    }
}
```

**Screen collection pattern (Compose):**
```kotlin
@Composable
fun ExampleScreen(viewModel: ExampleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ExampleEffect.Navigate -> { /* navigate */ }
                is ExampleEffect.ShowError -> { /* snackbar */ }
            }
        }
    }

    ExampleContent(state = state, onEvent = viewModel::onEvent)
}
```

### 10.3 Real-Time Camera State Management

The VisionViewModel handles high-frequency pose data (up to 30 FPS) differently from standard CRUD ViewModels:

```kotlin
@HiltViewModel
class VisionViewModel @Inject constructor(
    private val visionRepository: VisionRepository,
    private val calculateAnglesUseCase: CalculateJointAnglesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VisionUiState())
    val state: StateFlow<VisionUiState> = _state.asStateFlow()

    // Pose data flows at 30Hz -- use conflate() to drop stale frames
    // if the UI cannot keep up with the rendering
    fun startPoseDetection(cameraProvider: ProcessCameraProvider) {
        viewModelScope.launch {
            visionRepository.startPoseStream(cameraProvider)
                .conflate()  // CRITICAL: drop frames if UI is behind
                .collect { poseData ->
                    _state.update { it.copy(currentPoseResult = poseData) }
                }
        }
    }
}
```

Key considerations for real-time state:
- `conflate()` on the pose Flow ensures the UI always renders the latest frame, dropping intermediate frames if Compose recomposition is slower than the camera frame rate.
- `PoseOverlayData` is an immutable data class. Each frame produces a new instance. No shared mutable state.
- The Canvas overlay reads `state.currentPoseResult` on each recomposition. Since Compose Canvas draw operations are fast (sub-millisecond for line drawing), this keeps up with 30 FPS.

### 10.4 Error Handling Strategy

```kotlin
// Unified error model
sealed class AppError {
    data class Network(val code: Int?, val message: String) : AppError()
    data class Auth(val message: String) : AppError()
    data class Validation(val field: String, val message: String) : AppError()
    data class HealthConnect(val message: String) : AppError()
    data class Camera(val message: String) : AppError()
    data class Unknown(val throwable: Throwable) : AppError()
}

// Extension for mapping to user-facing messages
fun AppError.toUserMessage(): String = when (this) {
    is AppError.Network -> "Connection error. Check your internet and try again."
    is AppError.Auth -> "Session expired. Please log in again."
    is AppError.Validation -> "$field: $message"
    is AppError.HealthConnect -> "Could not read health data: $message"
    is AppError.Camera -> "Camera error: $message"
    is AppError.Unknown -> "Something went wrong. Please try again."
}
```

---

## 11. Security Architecture

### 11.1 Token Storage

```kotlin
// EncryptedSharedPreferences for auth tokens
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### 11.2 OkHttp Interceptor Chain

```
Request
  -> AuthInterceptor (adds Bearer token + apikey header)
  -> LoggingInterceptor (debug only, redacts auth headers)
  -> RetryInterceptor (exponential backoff on 5xx)
  -> OkHttp -> Network

Response
  <- TokenRefreshAuthenticator (intercepts 401, refreshes token, retries)
```

### 11.3 Certificate Pinning

```kotlin
val certificatePinner = CertificatePinner.Builder()
    .add("*.supabase.co", "sha256/AAAA...")   // pin Supabase TLS cert
    .add("api.apexai-athletics.com", "sha256/BBBB...")
    .build()
```

**IMPORTANT:** Certificate pins must be rotated before certificate expiry. Document pin hashes and expiry dates in the CI/CD configuration. Use a backup pin for each host.

### 11.4 ProGuard/R8 Rules

```proguard
# Keep Supabase/Retrofit models
-keep class com.apexai.crossfit.core.network.dto.** { *; }
-keep class com.apexai.crossfit.core.model.entity.** { *; }

# Keep MediaPipe
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.apexai.crossfit.**$$serializer { *; }
```

### 11.5 Sensitive Data Guidelines

- Never log access tokens, refresh tokens, or API keys.
- Video files in transit use HTTPS only.
- Health data (HRV, sleep) is PII: encrypt at rest, transmit only to user's own Supabase row (RLS enforced).
- Supabase anon key is NOT secret (it is a public key gated by RLS). It may be in BuildConfig.
- Supabase service role key is NEVER in the Android app. Only the FastAPI backend uses it.

---

## 12. Testing Architecture

### 12.1 Unit Test Strategy

| Layer | Tool | What to Test |
|---|---|---|
| ViewModel | JUnit 5 + MockK + Turbine | State transitions for every event, effect emissions |
| Use Cases | JUnit 5 + MockK | Business logic, ACWR calculation, angle calculations |
| Repository | JUnit 5 + MockK | DTO-to-entity mapping, cache-vs-network decisions |
| Mappers | JUnit 5 (no mocks) | All DTO to Entity conversions |

### 12.2 Integration Test Strategy

| Component | Tool | What to Test |
|---|---|---|
| Room DAOs | AndroidX Test + Room in-memory DB | Insert/query/delete, migration correctness |
| Retrofit | MockWebServer | Request/response serialization, error handling |
| Health Connect | Robolectric + FakeHealthConnectClient | Permission flows, data read correctness |

### 12.3 UI Test Strategy

| Scope | Tool | What to Test |
|---|---|---|
| Screen-level | Compose UI Test | Render states (loading, error, success), click handlers |
| Navigation | Compose Navigation Testing | Route transitions, argument passing, back stack |
| End-to-end | Compose UI Test + Hilt Test | Full WOD logging flow (browse -> detail -> log -> PR) |

### 12.4 Test Data Strategy

```kotlin
// core-testing module provides:
object TestFixtures {
    fun workout(id: String = "test-wod-1") = Workout(...)
    fun movement(id: String = "test-mvmt-1") = Movement(...)
    fun personalRecord(movementId: String = "test-mvmt-1") = PersonalRecord(...)
    fun coachingReport(faultCount: Int = 2) = CoachingReport(...)
    fun poseLandmarks() = List(33) { PoseLandmark(index = it, ...) }
}

// Fake repositories for ViewModel tests
class FakeWorkoutRepository : WorkoutRepository {
    var workoutsToReturn: List<WorkoutSummary> = emptyList()
    override fun getWorkouts(...) = flowOf(workoutsToReturn)
    // ...
}
```

### 12.5 Coverage Targets

| Module | Target |
|---|---|
| core-model | 95% (pure Kotlin) |
| Use cases | 90% |
| ViewModels | 85% |
| Repositories | 80% |
| UI (Compose) | 70% (critical flows) |
| Overall | 80% |

---

## 13. Android Studio and Device Testability

### 13.1 SDK Levels

- **minSdk:** 26 (Android 8.0 Oreo)
- **targetSdk:** 35 (Android 15)
- **compileSdk:** 35

### 13.2 Emulator Configuration (AVD)

| AVD | API | Purpose |
|---|---|---|
| Pixel 7 Pro | 35 | Primary development, camera emulation |
| Pixel 4a | 30 | Mid-range device testing |
| Pixel 3a | 26 | minSdk boundary testing |

**IMPORTANT:** MediaPipe pose detection requires a real device camera or the Android Emulator extended controls camera passthrough. The emulator's virtual camera scene will not produce meaningful pose landmarks. Physical device testing is mandatory for the vision feature.

### 13.3 Build Variants

| Variant | Supabase | FastAPI | ProGuard | Signing |
|---|---|---|---|---|
| debug | Project staging instance | localhost:8000 | OFF | Debug keystore |
| release | Project production instance | api.apexai-athletics.com | ON (R8 full mode) | Release keystore (GitHub Secrets) |

### 13.4 Required Permissions (AndroidManifest.xml)

```xml
<!-- Camera for pose detection -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Internet for Supabase and FastAPI -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Health Connect permissions -->
<uses-permission android:name="android.permission.health.READ_SLEEP" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE_VARIABILITY" />
<uses-permission android:name="android.permission.health.READ_RESTING_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />

<!-- Video recording -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />

<!-- Required hardware features -->
<uses-feature android:name="android.hardware.camera" android:required="true" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

### 13.5 Health Connect Intent Filter

```xml
<!-- Required for Health Connect permission request flow -->
<intent-filter>
    <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
</intent-filter>
<meta-data
    android:name="health_permissions"
    android:resource="@array/health_permissions" />
```

### 13.6 Required Third-Party Service Accounts

**The following accounts must be created before development begins:**

1. **Supabase project** -- Free tier sufficient for development. Needed for database, auth, storage, and edge functions.
2. **Google Cloud project with Gemini API enabled** -- Required for the FastAPI microservice. Billing must be enabled. Context Caching incurs storage fees ($1-4.50/1M tokens/hour).
3. **Google Play Console account** -- $25 one-time fee. Needed for Play Store deployment and internal testing track.
4. **Health Connect test app** -- Health Connect must be installed on test devices (bundled on Android 14+, available from Play Store on Android 9+).

---

## 14. Build and CI Configuration

### 14.1 Version Catalog (gradle/libs.versions.toml)

```toml
[versions]
kotlin = "2.0.21"
agp = "8.7.3"
compose-bom = "2025.01.01"
hilt = "2.51.1"
room = "2.7.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
coroutines = "1.9.0"
media3 = "1.5.1"
camerax = "1.4.1"
mediapipe = "0.10.21"
health-connect = "1.1.0-alpha10"
coil = "3.0.4"
navigation = "2.8.6"
datastore = "1.1.2"
kotlinx-serialization = "1.7.3"
mockk = "1.13.13"
turbine = "1.2.0"
junit5 = "5.11.4"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Media
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }
media3-compose = { group = "androidx.media3", name = "media3-ui-compose", version.ref = "media3" }

# Camera
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-video = { group = "androidx.camera", name = "camera-video", version.ref = "camerax" }
camerax-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }

# ML
mediapipe-vision = { group = "com.google.mediapipe", name = "tasks-vision", version.ref = "mediapipe" }

# Health
health-connect = { group = "androidx.health.connect", name = "connect-client", version.ref = "health-connect" }

# Image
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# Security
security-crypto = { group = "androidx.security", name = "security-crypto", version = "1.1.0-alpha06" }

# Testing
junit5 = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
okhttp-mockwebserver = { group = "com.squareup.okhttp3", name = "mockwebserver", version.ref = "okhttp" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.28" }
```

### 14.2 GitHub Actions CI Pipeline

```yaml
# .github/workflows/android-ci.yml
name: Android CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
      - name: Run unit tests
        run: ./gradlew test
      - name: Run lint
        run: ./gradlew lint
      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: '**/build/reports/tests/'

  build:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build debug APK
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk

  release:
    runs-on: ubuntu-latest
    needs: [test, build]
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Set up Ruby (Fastlane)
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.2'
          bundler-cache: true
      - name: Build release AAB
        run: ./gradlew bundleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      - name: Deploy to Play Store Internal Track
        run: bundle exec fastlane deploy_internal
        env:
          PLAY_STORE_JSON_KEY: ${{ secrets.PLAY_STORE_JSON_KEY }}
```

---

## 15. Critical Data Flow Diagrams

### 15.1 Pipeline: CameraX -> MediaPipe -> ViewModel -> Compose Canvas Overlay

```
+------------------+     ImageProxy      +---------------------+
|    CameraX       | -----------------> |  MediaPipe           |
|  ImageAnalysis   |   (YUV_420_888)    |  PoseLandmarker      |
|  UseCase         |                    |  (LIVE_STREAM mode)  |
+------------------+                    +---------------------+
  Dispatcher: MAIN                        Dispatcher: DEFAULT
  (CameraX requires                       (ML inference, CPU/GPU
   Main thread for                         bound, must NOT block
   lifecycle binding)                      Main thread)
        |                                        |
        |                                        | resultListener callback
        |                                        | (returns on DEFAULT)
        |                                        v
        |                              +---------------------+
        |                              |  PoseResultMapper    |
        |                              |  (normalize coords,  |
        |                              |   calc joint angles) |
        |                              +---------------------+
        |                                        |
        |                                   Flow<PoseOverlayData>
        |                                        |
        |                                        v
        |                              +---------------------+
        |                              |  VisionViewModel     |
        |                              |  _state.update {     |
        |                              |    it.copy(          |
        |                              |      currentPose=    |
        |                              |        poseData)     |
        |                              |  }                   |
        |                              +---------------------+
        |                                        |
        |                                   StateFlow<VisionUiState>
        |                                        |
        |                                        v
+------------------------------------------------------------------+
|  LiveCameraScreen (Compose)                                       |
|                                                                   |
|  +----------------------------+  +-----------------------------+  |
|  |  AndroidView(PreviewView)  |  |  Canvas(Modifier.fillMax)   |  |
|  |  (camera preview surface)  |  |  drawPoseLandmarks(pose)    |  |
|  |                            |  |  drawJointAngles(angles)    |  |
|  |                            |  |  drawBarbellPath(trajectory)|  |
|  +----------------------------+  +-----------------------------+  |
|         z-index: 0                     z-index: 1                 |
|                     (transparent overlay)                         |
+------------------------------------------------------------------+
```

**Threading constraints:**
- `CameraX.bindToLifecycle()` MUST run on `Dispatchers.Main` -- CameraX owns the camera thread internally.
- `PoseLandmarker` MUST run on `Dispatchers.Default` -- ML inference is CPU-bound. Running on Main will cause ANR.
- `StateFlow.update{}` is thread-safe and can be called from any dispatcher.
- `Canvas` drawing runs on the Compose render thread (Main). The overlay data is a snapshot; no synchronization needed beyond StateFlow collection.

**Critical detail:** The `ImageProxy` from CameraX ImageAnalysis must be closed after MediaPipe consumes it. Failure to call `imageProxy.close()` will stall the camera pipeline. Use a `try/finally` block in the analyzer.

### 15.2 Pipeline: Video Upload -> FastAPI -> Gemini -> Coaching JSON -> Android UI

```
+------------------+                    +---------------------+
|  Android Client   |                    |  FastAPI Backend     |
+------------------+                    +---------------------+
        |                                        |
        | 1. POST /coaching/analyze              |
        |    (multipart: video + metadata)       |
        |--------------------------------------->|
        |                                        |
        |    202 { analysis_id, poll_url }       |
        |<---------------------------------------|
        |                                        |
        |                                        | 2. Upload video to
        |                                        |    Supabase Storage
        |                                        |    (service_role key)
        |                                        |
        |                                        | 3. Create/retrieve
        |                                        |    Context Cache:
        |                                        |    - Movement standards
        |                                        |    - Biomechanical rules
        |                                        |    - Athlete history
        |                                        |    cache_name =
        |                                        |      "athlete_{id}_ctx"
        |                                        |
        |                                        | 4. Gemini 3.1/1.5 Pro:
        |                                        |    analyze video
        |                                        |    Input: video +
        |                                        |      cached context ref
        |                                        |    Output: structured JSON
        |                                        |    (reps, faults,
        |                                        |     timestamps, cues)
        |                                        |
        |                                        | 5. For each fault:
        |                                        |    Extract frame
        |                                        |    Gemini Flash: generate
        |                                        |    corrected posture image
        |                                        |    Upload to Storage
        |                                        |
        |                                        | 6. Write report +
        |                                        |    faults to Supabase DB
        |                                        |
        | 3. GET /coaching/status/{id}           |
        |    (poll every 3s)                     |
        |--------------------------------------->|
        |    200 { status: "processing", 0.6 }   |
        |<---------------------------------------|
        |                                        |
        |    ... continue polling ...             |
        |                                        |
        |    200 { status: "complete" }           |
        |<---------------------------------------|
        |                                        |
        | 4. GET /coaching/report/{id}            |
        |--------------------------------------->|
        |    200 { full CoachingReport JSON }     |
        |<---------------------------------------|
        |                                        |
        v                                        |
+------------------+                             |
|  CoachingViewModel|                             |
|  state.update {  |                             |
|    analysisStatus |                             |
|      = COMPLETE  |                             |
|    report = ...  |                             |
|  }               |                             |
+------------------+                             |
        |
        v
+----------------------------+
|  CoachingReportScreen      |
|  - Overall assessment      |
|  - Fault list with cues    |
|  - Corrected images (Coil) |
|  - "View in player" button |
+----------------------------+
        |
        | Navigate to VideoPlaybackScreen
        v
+----------------------------+
|  VideoPlaybackScreen       |
|  - Media3 PlayerView       |
|  - Compose Canvas overlay  |
|    (from overlay_data JSON)|
|  - Fault markers on seekbar|
+----------------------------+
```

**Context Cache Invalidation Strategy:**
- Each athlete gets a cache named `athlete_{uuid}_context`.
- Cache TTL: 24 hours (Gemini configurable). FastAPI checks cache existence before each analysis.
- Cache is rebuilt when: (a) TTL expires, (b) athlete logs a new PR (triggers cache refresh via Supabase webhook), (c) explicitly requested by the athlete via "refresh coaching profile" button.
- Cache contents: movement standards for the requested lift type, athlete's last 90 days of results, PR table, injury history notes.
- Wrap all Gemini API calls in a retry that catches `CacheNotFoundError` and triggers rebuild + retry.

### 15.3 Pipeline: Health Connect -> ACWR Calculation -> Readiness Score -> Dashboard

```
+------------------+   HealthConnectClient   +---------------------+
|  Android Device   | --------------------> |  Health Connect      |
|  (Oura, Garmin,  |   readRecords() calls  |  on-device datastore |
|   Whoop, etc.    |                        +---------------------+
|  write to HC)    |                                  |
+------------------+                                  |
                                                      |  HrvReading[]
                                                      |  SleepSession[]
                                                      |  HeartRateReading[]
                                                      v
                                            +---------------------+
                                            |  HealthRepository    |
                                            |  (core-health)      |
                                            |  Dispatcher: IO     |
                                            +---------------------+
                                                      |
                                                      | HealthSnapshot
                                                      v
                                            +---------------------+
                                            |  Supabase REST      |
                                            |  POST /health_      |
                                            |  snapshots           |
                                            +---------------------+
                                                      |
                                                      | (data persisted)
                                                      v
                                            +---------------------+
                                            |  POST /rpc/         |
                                            |  calculate_readiness|
                                            |  (Edge Function)    |
                                            +---------------------+
                                                      |
                                   ReadinessScore JSON response
                                                      |
                                                      v
                                            +---------------------+
                                            |  ReadinessViewModel  |
                                            |  Dispatcher: MAIN   |
                                            |  state.update {     |
                                            |    readinessScore,  |
                                            |    zone, hrv, sleep |
                                            |  }                  |
                                            +---------------------+
                                                      |
                                               StateFlow<ReadinessUiState>
                                                      |
                                                      v
                                            +----------------------------+
                                            |  ReadinessDashboardScreen   |
                                            |  - ACWR gauge (0-2.0)      |
                                            |  - Zone indicator           |
                                            |    (color-coded)            |
                                            |  - HRV trend sparkline     |
                                            |  - Sleep breakdown bars    |
                                            |  - Recommendation card     |
                                            +----------------------------+
```

---

## 16. Dependency Injection Setup (Hilt Modules)

### 16.1 Module Hierarchy

```kotlin
// --- core-network ---
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenRefreshAuthenticator: TokenRefreshAuthenticator
    ): OkHttpClient

    @Provides @Singleton @Named("supabase")
    fun provideSupabaseRetrofit(okHttpClient: OkHttpClient): Retrofit

    @Provides @Singleton @Named("fastapi")
    fun provideFastApiRetrofit(okHttpClient: OkHttpClient): Retrofit

    @Provides @Singleton
    fun provideSupabaseApi(@Named("supabase") retrofit: Retrofit): SupabaseApi

    @Provides @Singleton
    fun provideCoachingApi(@Named("fastapi") retrofit: Retrofit): CoachingApi
}

// --- core-database ---
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ApexDatabase

    @Provides fun provideWorkoutDao(db: ApexDatabase): WorkoutDao
    @Provides fun provideMovementDao(db: ApexDatabase): MovementDao
    @Provides fun provideResultDao(db: ApexDatabase): ResultDao
    @Provides fun providePrDao(db: ApexDatabase): PersonalRecordDao
}

// --- core-data ---
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository
    @Binds abstract fun bindPrRepository(impl: PrRepositoryImpl): PrRepository
    @Binds abstract fun bindReadinessRepository(impl: ReadinessRepositoryImpl): ReadinessRepository
    @Binds abstract fun bindCoachingRepository(impl: CoachingRepositoryImpl): CoachingRepository
}

// --- core-health ---
@Module
@InstallIn(SingletonComponent::class)
object HealthModule {
    @Provides @Singleton
    fun provideHealthConnectClient(
        @ApplicationContext context: Context
    ): HealthConnectClient

    @Provides @Singleton
    fun provideHealthRepository(
        client: HealthConnectClient,
        api: SupabaseApi
    ): HealthRepository
}

// --- core-media ---
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    @Provides @Singleton
    fun providePlayerPool(@ApplicationContext context: Context): PlayerPool

    @Provides @Singleton
    fun providePoseLandmarker(@ApplicationContext context: Context): PoseLandmarker
}

// --- Dispatchers (core-common) ---
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @Dispatcher(ApexDispatchers.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides @Dispatcher(ApexDispatchers.DEFAULT)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides @Dispatcher(ApexDispatchers.MAIN)
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val value: ApexDispatchers)

enum class ApexDispatchers { IO, DEFAULT, MAIN }
```

---

## 17. Threading Model

| Operation | Dispatcher | Rationale |
|---|---|---|
| CameraX lifecycle binding | `Main` | CameraX requires main thread for `bindToLifecycle()` |
| CameraX ImageAnalysis frame delivery | CameraX internal executor | Frames delivered on camera thread; do NOT process on Main |
| MediaPipe PoseLandmarker inference | `Default` | CPU/GPU-bound ML inference; blocks for 10-30ms per frame |
| Joint angle calculation | `Default` | Trigonometric computation, should not block IO |
| Supabase REST calls | `IO` | Network-bound |
| FastAPI calls (upload, poll, fetch) | `IO` | Network-bound, large payloads |
| Room database queries | `IO` | Disk-bound |
| Health Connect reads | `IO` | Disk-bound (on-device data store) |
| DataStore reads/writes | `IO` | Disk-bound |
| StateFlow updates in ViewModels | Any (thread-safe) | `MutableStateFlow.update{}` is atomic |
| Compose UI rendering | `Main` | Compose framework requirement |
| Media3 player operations | `Main` | ExoPlayer requires main thread for most operations |
| Video file compression (pre-upload) | `Default` | CPU-bound transcode |

---

## 18. Critical Pitfalls and Mitigations

### 18.1 ExoPlayer/Media3 Pool Management

**Problem:** Each `ExoPlayer` instance claims a hardware decoder slot. Android devices typically have 4-8 decoder slots. A `LazyColumn` of video tiles will instantiate one player per visible item, exhausting slots and causing black tiles, flickering, or crashes.

**Mitigation:**
```kotlin
@Singleton
class PlayerPool @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val maxPoolSize = 3  // NEVER exceed this
    private val pool = ArrayDeque<ExoPlayer>(maxPoolSize)
    private val inUse = mutableMapOf<String, ExoPlayer>()

    fun acquire(key: String): ExoPlayer {
        // Return existing if this key already has a player
        inUse[key]?.let { return it }

        // Get from pool or evict LRU
        val player = if (pool.isNotEmpty()) {
            pool.removeFirst()
        } else if (inUse.size >= maxPoolSize) {
            val (evictKey, evictPlayer) = inUse.entries.first()
            inUse.remove(evictKey)
            evictPlayer.stop()
            evictPlayer.clearMediaItems()
            evictPlayer
        } else {
            ExoPlayer.Builder(context).build()
        }

        inUse[key] = player
        return player
    }

    fun release(key: String) {
        inUse.remove(key)?.let { player ->
            player.stop()
            player.clearMediaItems()
            if (pool.size < maxPoolSize) pool.addLast(player)
            else player.release()
        }
    }

    fun releaseAll() {
        inUse.values.forEach { it.release() }
        inUse.clear()
        pool.forEach { it.release() }
        pool.clear()
    }
}
```

**Rules for frontend agent:**
- Never create `ExoPlayer()` directly in a Composable.
- Always acquire/release through `PlayerPool`.
- Call `releaseAll()` in `onCleared()` of any ViewModel that holds players.
- In `LazyColumn`, use `DisposableEffect` keyed to the item to acquire on enter and release on leave.

### 18.2 MediaPipe Z-Depth Limitations

**Problem:** BlazePose Z-coordinates represent relative depth from the camera plane. On mobile devices, Z accuracy degrades significantly with distance, lighting variation, and clothing. The Z-axis is labeled as "experimental" by Google.

**Mitigation:**
- All coaching threshold algorithms (e.g., "knee valgus detection", "trunk inclination") MUST use 2D angular calculations from X/Y coordinates.
- The app MUST instruct users to record from a profile (side) view for Olympic lifting analysis. This maximizes the information content of 2D projections.
- Z-depth may be displayed as a "confidence-degraded" auxiliary metric but MUST NOT be the sole basis for any fault detection.
- Joint angle calculation formula (2D):

```kotlin
fun calculateAngle2D(
    a: PoseLandmark,  // first point (e.g., shoulder)
    b: PoseLandmark,  // vertex point (e.g., elbow)
    c: PoseLandmark   // end point (e.g., wrist)
): Float {
    val radians = atan2(
        (c.y - b.y).toDouble(),
        (c.x - b.x).toDouble()
    ) - atan2(
        (a.y - b.y).toDouble(),
        (a.x - b.x).toDouble()
    )
    var degrees = Math.toDegrees(abs(radians)).toFloat()
    if (degrees > 180f) degrees = 360f - degrees
    return degrees
}
```

### 18.3 Gemini Context Cache Invalidation

**Problem:** Context Caches have a TTL (default 1 hour, configurable up to 24 hours). If the cache expires mid-analysis or if the athlete's profile changes (new PR, injury note), the coaching quality degrades or the request fails with a cache-miss error.

**Mitigation (FastAPI backend):**
1. Before every analysis request, check if `cache_name = "athlete_{id}_context"` exists and is valid.
2. If expired or missing, rebuild the cache synchronously before starting Gemini analysis.
3. Set TTL to 24 hours to minimize rebuilds.
4. Implement a Supabase webhook on `personal_records` INSERT/UPDATE that calls a FastAPI endpoint to proactively rebuild the cache for that athlete.
5. Wrap all Gemini API calls in a retry that catches `CacheNotFoundError` and triggers rebuild + retry.
6. Log cache hit/miss rates for cost monitoring.

### 18.4 ImageProxy Leak in CameraX

**Problem:** If `ImageProxy.close()` is not called after every frame, CameraX stops delivering new frames. This is the most common cause of "camera freezes after a few seconds."

**Mitigation:**
```kotlin
class PoseAnalyzer(
    private val poseLandmarker: PoseLandmarker
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        try {
            val mpImage = BitmapImageBuilder(imageProxy.toBitmap()).build()
            poseLandmarker.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
        } finally {
            imageProxy.close()  // ALWAYS close, even on error
        }
    }
}
```

### 18.5 Health Connect Availability

**Problem:** Health Connect is not available on all devices. On Android 13 and below, it requires a separate app install. On Android 14+, it is a system module but may be disabled.

**Mitigation:**
```kotlin
suspend fun isHealthConnectAvailable(context: Context): Boolean {
    val status = HealthConnectClient.getSdkStatus(context)
    return status == HealthConnectClient.SDK_AVAILABLE
}
// If SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> prompt user to update
// If SDK_UNAVAILABLE -> graceful fallback: readiness feature disabled
```

### 18.6 Video Upload Size

**Problem:** Raw workout videos (1080p, 60fps, 2-5 minutes) can be 500MB+. Uploading over mobile networks will fail or timeout.

**Mitigation:**
- Transcode to 720p 30fps H.264 before upload (using `MediaCodec` on `Dispatchers.Default`).
- Target output: approximately 50MB for a 3-minute clip.
- Use chunked upload with resume capability (Supabase Storage supports resumable uploads via TUS protocol).
- Show progress indicator with estimated time remaining.
- Allow background upload via `WorkManager`.

---

## 19. Open Questions and Ambiguities

| # | Question | Assumption Made |
|---|---|---|
| 1 | No wireframes or mockups exist for any screen. | Frontend agent should implement Material 3 design language with a dark/athletic theme. Screen inventory in Section 6 is authoritative. |
| 2 | The PDF mentions "social/leaderboards" at 10% effort allocation, but no feature details are provided. | Deferred to post-MVP. Not included in this architecture plan. Can be added as `feature-social` module later. |
| 3 | Barbell tracking via OpenCV/YOLOv8 is mentioned but no model is specified. | Deferred to Week 3 stretch goal. Initial release uses MediaPipe body landmarks only. Barbell tracking is isolated in `VisionRepository` so a detector can be swapped in without ViewModel changes. |
| 4 | PR detection trigger assumes `score_numeric` is always comparable across workouts of the same movement. | The `WorkoutResultInput` must include a parsed `score_numeric` field. The Android client is responsible for parsing the user's score string into a numeric value before submission. |
| 5 | Supabase project credentials are not yet provisioned. | Debug builds use placeholder values in BuildConfig. Backend agent must provision the Supabase project and provide the URL + anon key. |
| 6 | Gemini API model version ("3.1 Pro") does not exist as of knowledge cutoff. PDF references it alongside "1.5 Pro". | Use Gemini 1.5 Pro as primary. The FastAPI service should accept a model config parameter so switching to a newer model requires only a config change. |
| 7 | OxygenSaturationRecord is mentioned once in the PDF but not in CLAUDE.md permissions list. | Not included in initial Health Connect permissions. Can be added later if readiness algorithm is expanded. |

---

## 20. Implementation Priority Order

Each milestone produces a compilable, navigable, testable app.

### Milestone 1: Foundation (Week 1, Days 1-3)
1. **Gradle project setup** -- multi-module structure, version catalog, convention plugins
2. **core-ui** -- Material 3 theme, color scheme, typography, shared composables
3. **core-model** -- All domain entity data classes
4. **core-common** -- Extensions, dispatcher qualifiers, error types
5. **App shell** -- MainActivity, NavHost, BottomNavigation scaffold with placeholder screens

**Testable outcome:** App compiles, launches, navigates between 5 placeholder tabs.

### Milestone 2: Auth + Supabase (Week 1, Days 4-7)
6. **core-network** -- OkHttp client, Supabase Retrofit interface, auth interceptor
7. **core-data** -- DataStore setup, EncryptedSharedPreferences for tokens
8. **feature-auth** -- Login, Register, Profile screens with ViewModel
9. **AuthRepository** implementation against Supabase Auth

**Testable outcome:** User can register, log in, log out. Auth state persists across app restart.

### Milestone 3: WOD Tracking (Week 1-2 overlap)
10. **core-database** -- Room database, DAOs, entities
11. **feature-wod** -- Browse, Detail, Log, History, Timer screens
12. **WorkoutRepository** -- Supabase + Room cache

**Testable outcome:** User can browse workouts, start a timer, log results. History screen shows past results.

### Milestone 4: PR Tracking (Week 2)
13. **Backend: PostgreSQL trigger** for PR auto-detection (backend agent)
14. **feature-pr** -- Dashboard and Detail screens
15. **PrRepository** -- Supabase query

**Testable outcome:** Logging a WOD result that beats a PR shows the new PR in the dashboard.

### Milestone 5: Readiness (Week 2)
16. **core-health** -- Health Connect client wrapper
17. **Backend: ACWR Edge Function** (backend agent)
18. **feature-readiness** -- Dashboard, permission setup
19. **HealthRepository + ReadinessRepository**

**Testable outcome:** App reads HRV/sleep from Health Connect, syncs to Supabase, displays readiness score with zone indicator.

### Milestone 6: Vision (Week 3)
20. **core-media** -- CameraX pipeline, MediaPipe PoseLandmarker, PlayerPool
21. **feature-vision** -- LiveCameraScreen with Canvas overlay, recording
22. **PoseAnalyzer** + joint angle calculations

**Testable outcome:** Camera opens, skeletal overlay renders in real time over the camera feed. User can record a video.

### Milestone 7: AI Coaching (Week 3)
23. **Backend: FastAPI microservice** with Gemini integration (backend agent)
24. **feature-coaching** -- Report screen, video playback with overlay
25. **CoachingRepository** -- upload, poll, fetch report

**Testable outcome:** User records a lift, uploads it, receives a coaching report with faults and corrective images, can play back the video with kinematic overlay.

### Milestone 8: Polish and Deploy (Week 4)
26. **Performance profiling** -- Compose recomposition audit, memory leak detection
27. **CI/CD pipeline** -- GitHub Actions + Fastlane
28. **Play Store submission** -- Internal testing track

**Testable outcome:** Signed release AAB deployed to Play Store internal track via CI/CD.

---

## Appendix A: FastAPI Backend Structure

```
backend/
+-- app/
|   +-- main.py                    # FastAPI app, CORS, lifespan
|   +-- config.py                  # Environment variables, Supabase/Gemini keys
|   +-- dependencies.py            # Auth dependency (validate Supabase JWT)
+-- routers/
|   +-- coaching.py                # /coaching/analyze, /coaching/status, /coaching/report
|   +-- health.py                  # /health/readiness (optional REST proxy)
+-- services/
|   +-- gemini_service.py          # Gemini Pro analysis + Flash correction
|   +-- context_cache_service.py   # Cache lifecycle management
|   +-- video_service.py           # Supabase Storage upload, frame extraction
|   +-- supabase_service.py        # DB writes for reports, faults
+-- models/
|   +-- coaching.py                # Pydantic models for request/response
|   +-- video.py                   # Video metadata models
+-- requirements.txt
+-- Dockerfile
+-- docker-compose.yml
```

**Key environment variables for FastAPI:**
```
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_SERVICE_ROLE_KEY=eyJ...       # NOT the anon key
GEMINI_API_KEY=AIza...
GEMINI_MODEL=gemini-1.5-pro
GEMINI_FLASH_MODEL=gemini-2.0-flash
CONTEXT_CACHE_TTL_HOURS=24
MAX_VIDEO_SIZE_MB=500
```

---

## Appendix B: Key Landmark Indices for Olympic Lifting

```
MediaPipe BlazePose -- 33 landmarks

Critical for CrossFit coaching:

  Index  |  Name              |  Use in Coaching
  -------+--------------------+-------------------------------------------
  11     |  Left Shoulder     |  Trunk inclination, overhead position
  12     |  Right Shoulder    |  Trunk inclination, overhead position
  13     |  Left Elbow        |  Arm bend detection in pulls
  14     |  Right Elbow       |  Arm bend detection in pulls
  15     |  Left Wrist        |  Catch position, lockout
  16     |  Right Wrist       |  Catch position, lockout
  23     |  Left Hip          |  Hip hinge angle, squat depth
  24     |  Right Hip         |  Hip hinge angle, squat depth
  25     |  Left Knee         |  Knee valgus, squat depth
  26     |  Right Knee        |  Knee valgus, squat depth
  27     |  Left Ankle        |  Dorsiflexion, starting position
  28     |  Right Ankle       |  Dorsiflexion, starting position

Key angle calculations:
  - Knee flexion: hip-knee-ankle (indices 23-25-27 or 24-26-28)
  - Hip hinge: shoulder-hip-knee (indices 11-23-25 or 12-24-26)
  - Elbow flexion: shoulder-elbow-wrist (indices 11-13-15 or 12-14-16)
  - Trunk inclination: shoulder-hip vs vertical (indices 11-23 or 12-24)
```
agentId: a492f9a82b6af5ea4 (for resuming to continue this agent's work if needed)
<usage>total_tokens: 126870
tool_uses: 0
duration_ms: 472341</usage>
