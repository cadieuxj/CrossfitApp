)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

---

### 3.14 CI/CD Pipeline

#### 3.14.1 GitHub Actions — `.github/workflows/android-ci.yml`

```yaml
name: Android CI/CD

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

env:
  JAVA_VERSION: '17'
  JAVA_DISTRIBUTION: 'temurin'

jobs:
  # -------------------------------------------------------
  # JOB 1: Unit Tests + Lint
  # Runs on every push and PR. Blocks build if any test fails.
  # -------------------------------------------------------
  test:
    name: Unit Tests and Lint
    runs-on: ubuntu-latest
    steps:
      - name: Checkout source
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Cache Gradle dependencies
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}
          restore-keys: |
            gradle-${{ runner.os }}-

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Run unit tests
        run: ./gradlew test --continue

      - name: Run lint
        run: ./gradlew lint

      - name: Upload test reports
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-reports-${{ github.run_number }}
          path: |
            **/build/reports/tests/
            **/build/reports/lint-results*.html
          retention-days: 14

  # -------------------------------------------------------
  # JOB 2: Debug Build
  # Assembles debug APK to verify compilation on every PR.
  # -------------------------------------------------------
  build-debug:
    name: Build Debug APK
    runs-on: ubuntu-latest
    needs: test
    steps:
      - name: Checkout source
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Cache Gradle dependencies
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Assemble debug APK
        run: ./gradlew assembleDebug

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk-${{ github.run_number }}
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 7

  # -------------------------------------------------------
  # JOB 3: Release AAB + Play Store Deployment
  # Only runs on pushes to main (not on PRs).
  # Requires all secrets to be configured in GitHub Secrets.
  # -------------------------------------------------------
  release:
    name: Release Build and Deploy
    runs-on: ubuntu-latest
    needs: [ test, build-debug ]
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    steps:
      - name: Checkout source
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Fastlane changelog generation needs full history

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: ${{ env.JAVA_DISTRIBUTION }}

      - name: Set up Ruby for Fastlane
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.2'
          bundler-cache: true

      - name: Cache Gradle dependencies
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ runner.os }}-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties', '**/libs.versions.toml') }}

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Decode keystore from secret
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > keystore.jks

      - name: Build release AAB
        env:
          KEYSTORE_PATH: keystore.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
          SUPABASE_ANON_KEY: ${{ secrets.SUPABASE_ANON_KEY }}
        run: ./gradlew bundleRelease

      - name: Write Play Store service account key
        run: |
          echo '${{ secrets.PLAY_STORE_JSON_KEY }}' > play-store-key.json

      - name: Deploy to Play Store internal track
        run: bundle exec fastlane deploy_internal
        env:
          PLAY_STORE_KEY_PATH: play-store-key.json
          AAB_PATH: app/build/outputs/bundle/release/app-release.aab

      - name: Upload release AAB artifact
        uses: actions/upload-artifact@v4
        with:
          name: release-aab-${{ github.run_number }}
          path: app/build/outputs/bundle/release/app-release.aab
          retention-days: 30

      - name: Clean up secrets from disk
        if: always()
        run: |
          rm -f keystore.jks play-store-key.json
```

**Required GitHub Secrets:**

| Secret Name | Description |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded release keystore `.jks` file |
| `KEYSTORE_PASSWORD` | Password for the keystore |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Password for the specific key |
| `PLAY_STORE_JSON_KEY` | Full JSON content of the Google Play service account key |
| `SUPABASE_URL` | Production Supabase project URL |
| `SUPABASE_ANON_KEY` | Production Supabase anon key |

#### 3.14.2 Fastlane — `fastlane/Fastfile`

```ruby
# fastlane/Fastfile

default_platform(:android)

platform :android do

  # -----------------------------------------------------------
  # LANE: test
  # Run all unit tests and lint locally or in CI
  # Usage: bundle exec fastlane test
  # -----------------------------------------------------------
  lane :test do
    gradle(
      task: "test",
      flags: "--continue"
    )
    gradle(task: "lint")
  end

  # -----------------------------------------------------------
  # LANE: build_debug
  # Build a debug APK for manual distribution or testing
  # Usage: bundle exec fastlane build_debug
  # -----------------------------------------------------------
  lane :build_debug do
    gradle(
      task: "assemble",
      build_type: "Debug"
    )
    UI.success("Debug APK built: #{lane_context[SharedValues::GRADLE_APK_OUTPUT_PATH]}")
  end

  # -----------------------------------------------------------
  # LANE: build_release
  # Build a signed release AAB without deploying
  # Usage: bundle exec fastlane build_release
  # -----------------------------------------------------------
  lane :build_release do
    gradle(
      task: "bundle",
      build_type: "Release",
      properties: {
        "android.injected.signing.store.file"     => ENV["KEYSTORE_PATH"],
        "android.injected.signing.store.password" => ENV["KEYSTORE_PASSWORD"],
        "android.injected.signing.key.alias"      => ENV["KEY_ALIAS"],
        "android.injected.signing.key.password"   => ENV["KEY_PASSWORD"]
      }
    )
    UI.success("Release AAB built: #{lane_context[SharedValues::GRADLE_AAB_OUTPUT_PATH]}")
  end

  # -----------------------------------------------------------
  # LANE: deploy_internal
  # Build signed release AAB and upload to Play Store internal testing track.
  # Triggered by CI on every push to main.
  # Usage: bundle exec fastlane deploy_internal
  # -----------------------------------------------------------
  lane :deploy_internal do
    # Increment version code automatically (uses git commit count)
    # This ensures every CI build has a unique version code.
    version_code = sh("git rev-list --count HEAD").strip.to_i

    gradle(
      task: "bundle",
      build_type: "Release",
      properties: {
        "android.injected.signing.store.file"     => ENV["KEYSTORE_PATH"],
        "android.injected.signing.store.password" => ENV["KEYSTORE_PASSWORD"],
        "android.injected.signing.key.alias"      => ENV["KEY_ALIAS"],
        "android.injected.signing.key.password"   => ENV["KEY_PASSWORD"],
        "versionCode"                             => version_code
      }
    )

    upload_to_play_store(
      track: "internal",
      aab: ENV["AAB_PATH"] || lane_context[SharedValues::GRADLE_AAB_OUTPUT_PATH],
      json_key: ENV["PLAY_STORE_KEY_PATH"],
      skip_upload_apk: true,
      skip_upload_metadata: true,
      skip_upload_changelogs: false,
      # Auto-generate changelog from the latest git commit message
      changelog_from_git_last_commit: true,
      release_status: "draft"
      # Draft status: build is visible in internal track but must be manually promoted
    )

    UI.success("Successfully deployed to Play Store internal track!")
  end

  # -----------------------------------------------------------
  # LANE: promote_to_alpha
  # Promote the latest internal build to alpha track.
  # Run manually after internal QA sign-off.
  # Usage: bundle exec fastlane promote_to_alpha
  # -----------------------------------------------------------
  lane :promote_to_alpha do
    upload_to_play_store(
      track: "internal",
      track_promote_to: "alpha",
      json_key: ENV["PLAY_STORE_KEY_PATH"],
      skip_upload_aab: true
    )
    UI.success("Promoted to alpha track!")
  end

  # -----------------------------------------------------------
  # LANE: promote_to_production
  # Promote alpha build to production with staged rollout (10%).
  # Run manually after sufficient alpha testing.
  # Usage: bundle exec fastlane promote_to_production
  # -----------------------------------------------------------
  lane :promote_to_production do
    upload_to_play_store(
      track: "alpha",
      track_promote_to: "production",
      rollout: "0.1",   # 10% staged rollout
      json_key: ENV["PLAY_STORE_KEY_PATH"],
      skip_upload_aab: true
    )
    UI.success("Promoted to production with 10% rollout!")
  end

  # -----------------------------------------------------------
  # Error handler — called when any lane fails
  # -----------------------------------------------------------
  error do |lane, exception, options|
    UI.error("Lane '#{lane}' failed with: #{exception.message}")
  end

end
```

`fastlane/Appfile`:
```ruby
json_key_file(ENV["PLAY_STORE_KEY_PATH"])
package_name("com.apexai.crossfit")
```

`Gemfile`:
```ruby
source "https://rubygems.org"
gem "fastlane", "~> 2.225"
```

---

## 4. Frontend Agent Specifications

### 4.1 Design System

#### 4.1.1 Color Palette (Material 3 Dynamic Color + Custom Athletic Theme)

The app uses a dark theme as default. Light theme is optional and lower priority.

```kotlin
// com.apexai.crossfit.core.ui.theme.Color.kt

// Brand colors
val ApexOrange = Color(0xFFFF6B35)       // Primary action, FAB, key CTAs
val ApexOrangeDim = Color(0xFFBF4F26)    // Primary container
val ApexGold = Color(0xFFFFD700)         // PR achievements, personal bests, star ratings

// Semantic / Readiness zone colors
val ReadinessOptimal = Color(0xFF4CAF50)   // Green — ACWR 0.8–1.3
val ReadinessCaution = Color(0xFFFF9800)   // Amber — ACWR 1.3–1.5
val ReadinessHighRisk = Color(0xFFF44336)  // Red — ACWR > 1.5
val ReadinessUndertrained = Color(0xFF2196F3) // Blue — ACWR < 0.8

// Fault severity colors (used in coaching report)
val FaultCritical = Color(0xFFF44336)     // Red
val FaultModerate = Color(0xFFFF9800)     // Amber
val FaultMinor = Color(0xFFFFEB3B)        // Yellow

// Surface hierarchy (dark theme)
val Surface           = Color(0xFF121212)  // Base background
val SurfaceElevated1  = Color(0xFF1E1E1E)  // Cards, bottom nav
val SurfaceElevated2  = Color(0xFF2C2C2C)  // Dialogs, sheets
val SurfaceElevated3  = Color(0xFF383838)  // Chip backgrounds, input fields

// Text
val OnSurface         = Color(0xFFE8E8E8)  // Primary text
val OnSurfaceVariant  = Color(0xFF9E9E9E)  // Secondary text, captions

// Kinematic overlay colors (Canvas)
val LandmarkDot       = Color(0xFFFF6B35)  // Joint position dots
val LandmarkLine      = Color(0x99FF6B35)  // Skeletal connection lines (60% opacity)
val AngleArc          = Color(0xFFFFFFFF)  // Joint angle arc
val AngleText         = Color(0xFFFFFFFF)  // Angle degree label
val BarbellPath       = Color(0xFF00E5FF)  // Barbell trajectory trail (cyan)
val FaultHighlight    = Color(0xFFF44336)  // Fault landmark highlight (red)
```

#### 4.1.2 Typography

```kotlin
// com.apexai.crossfit.core.ui.theme.Typography.kt

val ApexTypography = Typography(
    // Hero numbers — readiness score, PR value, timer display
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    // Screen titles
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // Card titles, section headers
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Body text — WOD descriptions, coaching cues
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Captions, timestamps, metadata
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

#### 4.1.3 Spacing System

```
4dp   — minimum padding, icon-to-label gaps
8dp   — compact element spacing
12dp  — medium element spacing
16dp  — standard screen horizontal padding, card padding
24dp  — section spacing
32dp  — large section gaps
48dp  — screen-level vertical rhythm
```

#### 4.1.4 Component Shapes

```kotlin
val ApexShapes = Shapes(
    small  = RoundedCornerShape(8.dp),   // Chips, badges, small buttons
    medium = RoundedCornerShape(12.dp),  // Cards, input fields, dialogs
    large  = RoundedCornerShape(16.dp),  // Bottom sheets, large cards
    extraLarge = RoundedCornerShape(28.dp) // FAB, full-screen overlays
)
```

#### 4.1.5 Iconography

Use `androidx.compose.material:material-icons-extended`. Key icons:

| Element | Icon |
|---|---|
| Home tab | `Icons.Outlined.Home` |
| WOD tab | `Icons.Outlined.FitnessCenter` |
| Camera FAB | `Icons.Filled.Videocam` |
| Readiness tab | `Icons.Outlined.MonitorHeart` |
| Profile tab | `Icons.Outlined.Person` |
| PR achievement | `Icons.Filled.EmojiEvents` (gold tint) |
| Timer | `Icons.Outlined.Timer` |
| Start/Play | `Icons.Filled.PlayArrow` |
| Pause | `Icons.Filled.Pause` |
| Upload | `Icons.Outlined.CloudUpload` |
| Warning / Fault | `Icons.Filled.Warning` |
| Checkmark | `Icons.Filled.CheckCircle` |
| Back | `Icons.AutoMirrored.Filled.ArrowBack` |

---

### 4.2 Screen Specifications

#### S01 — SplashScreen

**Purpose:** Display app logo, check auth state, route to correct destination.

**Layout:** `Box(Modifier.fillMaxSize())` centered content. Dark background (`Surface`).

**Components:**
- `Image` — ApexAI Athletics logo, centered, `128.dp × 128.dp`
- `Text` — "ApexAI Athletics" in `headlineMedium`, below logo, `ApexOrange` color
- `CircularProgressIndicator` — small, below text, visible only during auth check (typically < 500ms)

**States:**

| State | UI |
|---|---|
| `Loading` | Logo + spinner visible |
| `Authenticated` | No UI change; immediate navigation to `home` |
| `Unauthenticated` | No UI change; immediate navigation to `auth/login` |

**Interactions:** None. The screen navigates automatically based on auth state from `AppStateManager`. If auth check takes longer than 2 seconds, show the spinner; otherwise it appears instantaneously.

**Animation:** Logo fades in over 300ms using `AnimatedVisibility` with `fadeIn(animationSpec = tween(300))`.

---

#### S02 — LoginScreen

**Purpose:** Email/password authentication.

**Layout:** `Scaffold` → `Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = SpaceBetween)`

**Components (top to bottom):**
1. `Spacer(Modifier.height(48.dp))`
2. `Text` — "Welcome back" — `headlineLarge`, `OnSurface`
3. `Text` — "Sign in to your athlete profile" — `bodyMedium`, `OnSurfaceVariant`
4. `Spacer(Modifier.height(32.dp))`
5. `OutlinedTextField` — Email — `keyboardType = Email`, `imeAction = Next`; error state shows inline message below field
6. `Spacer(Modifier.height(12.dp))`
7. `OutlinedTextField` — Password — `visualTransformation = PasswordVisualTransformation()`, `imeAction = Done`, trailing icon toggles password visibility
8. `Spacer(Modifier.height(8.dp))`
9. `AnimatedVisibility` — Error banner — `Card` with `FaultCritical` tint, `Text` with error message; visible when `state.error != null`
10. `Spacer(Modifier.weight(1f))`
11. `Button(Modifier.fillMaxWidth())` — "Sign In" — `ApexOrange` background; shows `CircularProgressIndicator` when `state.isLoading`
12. `TextButton` — "Don't have an account? Create one" — navigates to RegisterScreen

**States:**

| State | UI Behavior |
|---|---|
| `isLoading = true` | Button replaced with progress indicator, all fields disabled |
| `error != null` | Error banner visible with message |
| `email` or `password` invalid format | Field-level error text below the specific `OutlinedTextField` |

**Interactions:**
- Keyboard `Done` action on password field triggers login
- Sign In button calls `viewModel.onEvent(AuthEvent.LoginClicked)`

---

#### S03 — RegisterScreen

**Purpose:** Create a new athlete account.

**Layout:** `Scaffold` → `Column` with `verticalScroll(rememberScrollState())`

**Components (top to bottom):**
1. `TopAppBar` with back arrow (navigate to LoginScreen)
2. `Text` — "Create your profile" — `headlineLarge`
3. `Text` — "Start tracking your performance" — `bodyMedium`, `OnSurfaceVariant`
4. `Spacer(24.dp)`
5. `OutlinedTextField` — Display Name — `imeAction = Next`
6. `Spacer(12.dp)`
7. `OutlinedTextField` — Email — `keyboardType = Email`, `imeAction = Next`
8. `Spacer(12.dp)`
9. `OutlinedTextField` — Password — `PasswordVisualTransformation`, `imeAction = Next`; hint: minimum 8 characters
10. `Spacer(12.dp)`
11. `OutlinedTextField` — Confirm Password — `PasswordVisualTransformation`, `imeAction = Done`; inline error "Passwords do not match" if mismatch
12. `Spacer(8.dp)`
13. Error banner (same as LoginScreen)
14. `Spacer(24.dp)`
15. `Button(Modifier.fillMaxWidth())` — "Create Account" — disabled until all fields pass validation

---

#### S04 — HomeScreen (Dashboard)

**Purpose:** Central hub showing today's readiness, recent WODs, and quick actions.

**Layout:** `Scaffold(topBar = { LargeTopAppBar("Good morning, {displayName}") })` → `LazyColumn`

**Components (LazyColumn items in order):**

1. **Readiness Score Card** — `ElevatedCard(Modifier.fillMaxWidth().padding(horizontal=16.dp))`
   - `Row` containing:
     - Left: `Column` with `Text("Readiness", labelSmall)`, large score number `Text(acwr.formatted, displayLarge, color=zone.color)`, `Text(zone.label, bodyMedium, color=zone.color)`
     - Right: `ACWRGaugeComposable` — custom arc gauge (see Reusable Components 4.3)
   - Bottom: `Text(recommendation, bodySmall, OnSurfaceVariant)` — truncated to 2 lines with "see more"
   - Tap navigates to ReadinessDashboardScreen
   - Empty state: "Connect Health Data" button if `healthConnectPermissionsGranted = false`

2. **Section Header** — `Text("Recent Workouts", titleMedium, modifier = Modifier.padding(16.dp))`

3. **Recent WOD Cards** — `LazyRow` of `WodSummaryCard` components (see Reusable Components), showing last 3 results

4. **Section Header** — `Text("Quick Actions", titleMedium, Modifier.padding(16.dp))`

5. **Quick Actions Grid** — `LazyVerticalGrid(columns = Fixed(2))` with 2 items:
   - "Log a Workout" card → navigates to WodBrowseScreen
   - "Record Your Lift" card → navigates to vision/live

6. **Section Header** — `Text("Personal Records", titleMedium, Modifier.padding(16.dp))`

7. **Recent PRs Row** — `LazyRow` of `PrChipComponent` for PRs set in the last 7 days; empty state: "No recent PRs. Log a workout to start tracking."

**States:**

| State | UI |
|---|---|
| `isLoading = true` | Skeleton placeholders for each section using `ShimmerBox` |
| `error != null` | Error card with retry button at top of list |
| All data loaded | Normal content as above |

---

#### S05 — WodBrowseScreen

**Purpose:** Browse and search the WOD catalog.

**Layout:** `Scaffold(topBar = {...})` → `Column` → `LazyColumn`

**Top Bar Components:**
- `SearchBar` (Material 3) — full width, placeholder "Search workouts...", value = `state.searchQuery`, `onValueChange` dispatches `WodBrowseEvent.SearchChanged`
- `LazyRow` of `FilterChip` items for each `TimeDomain`: "All", "AMRAP", "EMOM", "RFT", "Tabata"

**LazyColumn Items:**
- Each item: `WodSummaryCard` (see Reusable Components)
- Empty state item: `EmptyStateComposable` with barbell icon and "No workouts match your search"
- Loading state: 6 `ShimmerBox` items of `WodSummaryCard` height

**Interactions:**
- Tapping a WOD card navigates to `wod/{wodId}`
- Search debounced 300ms before dispatching event

---

#### S06 — WodDetailScreen

**Purpose:** Display full workout details and entry point to timer and logging.

**Layout:** `Scaffold` with `CollapsingTopAppBar` showing workout name

**Components:**
1. `TopAppBar` with back arrow and workout name
2. **Time Domain Badge** — `AssistChip` with `TimeDomain` label and appropriate icon
3. **WOD Description** — `Text(workout.description, bodyLarge)` in a `Card`
4. **Movements Section** — `Text("Movements", titleMedium)` header + `LazyColumn` of `MovementRowItem`:
   - Each row: `Row` with movement name (`bodyLarge`), prescribed reps/weight (`bodyMedium`, `OnSurfaceVariant`), equipment chip
5. **Action Row** — `Row` at bottom with:
   - `OutlinedButton` — "Log Result" — navigates to `wod/{id}/log`
   - `Button` (primary) — "Start Timer" — navigates to `wod/{id}/timer`

**States:**

| State | UI |
|---|---|
| `isLoading = true` | Skeleton for description and movement rows |
| `error != null` | Error message with retry button |

---

#### S07 — WodLogScreen

**Purpose:** Enter workout score and submit the result.

**Layout:** `Scaffold` → `Column` with `verticalScroll`

**Components (top to bottom):**
1. `TopAppBar` — "Log {workout.name}" with back arrow
2. **Score Field** — `OutlinedTextField(Modifier.fillMaxWidth())`:
   - Label adapts to `scoringMetric`: "Total Reps" / "Time (mm:ss)" / "Load (kg)" / "Rounds + Reps"
   - `keyboardType = Number` for REPS/LOAD; `Text` for TIME
   - Helper text explains format for TIME: "Format: 12:34"
3. **RXd Toggle** — `Row` with `Text("As Prescribed (RXd)")` and `Switch(checked = state.rxd)`
4. **RPE Slider** — `Text("Rate of Perceived Exertion: ${state.rpe ?: "--"}")` + `Slider(range = 1f..10f, steps = 8)` — optional
5. **Notes Field** — `OutlinedTextField(minLines = 3)` — optional
6. **PR Banner** — `AnimatedVisibility(state.newPrs.isNotEmpty())` → `Card` with gold border, trophy icon, "New PRs! {pr list}"
7. `Spacer(Modifier.weight(1f))`
8. `Button(Modifier.fillMaxWidth())` — "Save Result" — `isLoading` shows spinner

**Business Rule:** Score field validation:
- TIME format: must match `MM:SS` regex pattern; real-time validation with inline error
- REPS/LOAD: must be a positive number; "0" is not valid
- Submit button is disabled until score field is non-empty and passes format validation

---

#### S09 — WodTimerScreen

**Purpose:** Active workout timer. Fullscreen to minimize distractions.

**Layout:** `Scaffold` with no top bar → `Box(Modifier.fillMaxSize(), contentAlignment = Center)`

**Components:**
1. **Round Indicator** (EMOM/RFT only) — `Text("Round ${state.currentRound}", headlineMedium, OnSurfaceVariant)` at top
2. **Timer Display** — `Text(state.elapsedMillis.formatTimer(), displayLarge, ApexOrange)` — formatted as `MM:SS.d` (decisecond precision)
3. **Interval Countdown** (EMOM only) — `Text("${state.currentIntervalSecondsRemaining}s remaining in interval", bodyLarge, ReadinessCaution)` below timer
4. **Control Buttons** — `Row` with:
   - `IconButton(Icons.Filled.Restart)` — Reset
   - Large `FloatingActionButton(Icons.Filled.PlayArrow / Pause)` — Start/Pause, `apexOrange` container
5. **Finish Workout Button** — `OutlinedButton("Finish and Log")` at bottom, triggers `WodTimerEvent.Complete` and navigates to log screen
6. **Movements reminder** — `LazyColumn` at bottom (collapsed) showing the WOD movements for reference

**AMRAP behavior:** Timer counts up from 0 to `timeCap`. At `timeCap`, timer pulses red and a `Snackbar` appears: "Time's up! Log your total reps."

**EMOM behavior:** Timer shows total elapsed time. Interval countdown shows seconds remaining in the current minute. At each minute boundary, the interval resets and a haptic feedback pulse triggers.

---

#### S10 — PrDashboardScreen

**Purpose:** All personal records grouped by movement category.

**Layout:** `Scaffold` → `LazyColumn`

**Components:**
- Each category section: `Text(categoryName, titleMedium)` + `LazyRow` of `PrCard` items
- Categories: Olympic Lifting, Powerlifting, Gymnastics, Monostructural, Accessory

**PrCard** (each item in the row):
- `ElevatedCard(96.dp × 112.dp)` — compact card
- `Text(movement.name, bodyMedium, maxLines = 2)`
- `Text("${pr.value} ${pr.unit.label}", titleLarge, ApexGold)` — the PR value in gold
- `Text(pr.achievedAt.formatRelative(), labelSmall, OnSurfaceVariant)` — "3 days ago"
- Tap navigates to `pr/{movementId}`

**Empty State:** Trophy icon illustration + "Log workouts to start tracking your PRs."

---

#### S11 — PrDetailScreen

**Purpose:** PR history trend chart for a single movement.

**Layout:** `Scaffold` → `Column`

**Components:**
1. `TopAppBar` — movement name, back arrow
2. **Current PR Display** — `Card(Modifier.fillMaxWidth())`: large PR value in `displayLarge`, `ApexGold`, unit label, date achieved
3. **Trend Chart** — `PrTrendChart` custom Compose Canvas chart:
   - X-axis: dates of PR achievements (sparse, labeled at key points)
   - Y-axis: PR value
   - Data points: `ApexOrange` filled circles
   - Line: `ApexOrange` connecting points
   - Y-axis auto-scaled with 10% padding above current PR
4. **History List** — `LazyColumn` of rows: date | value with unit | `ElevatedCard` styling

---

#### S12 — ReadinessDashboardScreen

**Purpose:** Composite readiness assessment with ACWR, HRV, and sleep metrics.

**Layout:** `Scaffold` → `LazyColumn`

**Components:**

1. **Hero Score Card** — `ElevatedCard(Modifier.fillMaxWidth())`:
   - Large `ACWRGaugeComposable` (200.dp diameter)
   - ACWR value: `Text(state.readinessScore?.format(2), displayLarge, zoneColor)`
   - Zone label: `Text(state.readinessZone?.label, headlineMedium, zoneColor)`
   - Last synced: `Text("Synced ${state.lastSyncedAt?.formatRelative()}", labelSmall, OnSurfaceVariant)`
   - `Button("Sync Health Data")` — dispatches `ReadinessEvent.SyncHealthData`

2. **Biometrics Row** — `Row(Modifier.fillMaxWidth(), horizontalArrangement = SpaceEvenly)`:
   - HRV Card: label "HRV", value `${state.latestHrv}ms` or "--"
   - Sleep Card: label "Sleep", value `${state.sleepDuration?.hours}h ${state.sleepDuration?.minutes}m` or "--"

3. **Sleep Quality Breakdown** — `Card`:
   - `LinearProgressIndicator` for Deep sleep (purple), REM (indigo), Light (blue), each labeled with duration
   - All three bars in a `Column` with label and value

4. **Recommendation Card** — `Card` with left border accent in `zoneColor`:
   - `Text("Today's Recommendation", titleMedium)`
   - `Text(state.readinessScore?.recommendation, bodyLarge)`

5. **ACWR History Chart** — `LazyRow` of last 14 days as vertical bar chart, bars colored by zone

**Health Connect Not Connected State:**
Full-width `OutlinedCard` with `MonitorHeart` icon, "Connect your wearable" headline, body text explaining Health Connect, `Button("Set Up Health Data")` → navigates to HealthConnectSetupScreen.

---

#### S13 — HealthConnectSetupScreen

**Purpose:** Walk the user through granting Health Connect permissions.

**Layout:** `Scaffold` → `Column(verticalArrangement = SpaceEvenly, Modifier.fillMaxSize().padding(24.dp))`

**Components (onboarding-style single-page):**
1. `Icon(Icons.Outlined.MonitorHeart, 96.dp)` centered, `ApexOrange` tint
2. `Text("Connect your health data", headlineLarge)` centered
3. `Text(explanatory paragraph, bodyLarge, OnSurfaceVariant)` — explains what data is read and why
4. **Permissions List** — `Column` of 3 `Row` items:
   - Each row: checkmark icon + `Text(permission description)`
   - "Sleep duration and quality"
   - "Heart Rate Variability (HRV)"
   - "Resting Heart Rate"
5. `Spacer(Modifier.weight(1f))`
6. `Button(Modifier.fillMaxWidth())` — "Grant Permissions" — launches `healthConnectPermissionLauncher`
7. `TextButton` — "Skip for now" — navigates back

**Permission result handling:**
```kotlin
val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
    contract = healthConnectManager.createPermissionRequestContract()
) { granted ->
    viewModel.onEvent(ReadinessEvent.PermissionsResult(granted))
}
```

After permissions granted: navigate back to ReadinessDashboardScreen and trigger sync.

---

#### S14 — LiveCameraScreen

**Purpose:** Real-time camera with MediaPipe skeletal overlay. The visually most complex screen.

**Layout:** Fullscreen `Box` — no system UI, no top bar, no bottom nav.

**Component layers (z-order bottom to top):**
1. `AndroidView(factory = { PreviewView(context) })` — CameraX preview surface, fills screen
2. `Canvas(Modifier.fillMaxSize())` — transparent overlay for pose landmarks (z-index above preview)
3. Control overlay `Column(Modifier.align(Alignment.TopStart).padding(16.dp))`:
   - Back button `IconButton(Icons.AutoMirrored.Filled.ArrowBack)`
   - Movement selector `ExposedDropdownMenuBox` — select lift type for AI analysis context
   - FPS counter `Text("${state.fps} fps", labelSmall, White)` in debug builds only
4. Recording controls `Column(Modifier.align(Alignment.BottomCenter).padding(24.dp))`:
   - Recording duration `Text(state.recordingDuration.formatTimer(), titleLarge, White)` — visible only when recording
   - Record/Stop button `FloatingActionButton(96.dp)` — red fill when recording, white outline when not
   - Flip camera `IconButton(Icons.Outlined.FlipCameraAndroid)` — hidden when recording

**Canvas Overlay Drawing Logic:**

```kotlin
// Drawn inside Canvas { } block on each recompose triggered by pose data
val pose = state.currentPoseResult ?: return@Canvas

// 1. Draw skeletal connections (lines)
val connections = listOf(
    11 to 12, 11 to 13, 13 to 15,  // Left arm
    12 to 14, 14 to 16,              // Right arm
    11 to 23, 12 to 24,              // Torso
    23 to 24,                         // Hip line
    23 to 25, 25 to 27,              // Left leg
    24 to 26, 26 to 28               // Right leg
)
connections.forEach { (startIdx, endIdx) ->
    val start = pose.landmarks.getOrNull(startIdx)
    val end = pose.landmarks.getOrNull(endIdx)
    if (start != null && end != null &&
        start.visibility > 0.5f && end.visibility > 0.5f) {
        drawLine(
            color = LandmarkLine,
            start = Offset(start.x * size.width, start.y * size.height),
            end = Offset(end.x * size.width, end.y * size.height),
            strokeWidth = 3.dp.toPx()
        )
    }
}

// 2. Draw joint dots
pose.landmarks.forEach { lm ->
    if (lm.visibility > 0.5f) {
        drawCircle(
            color = LandmarkDot,
            radius = 6.dp.toPx(),
            center = Offset(lm.x * size.width, lm.y * size.height)
        )
    }
}

// 3. Draw joint angle labels at key joints
pose.jointAngles.forEach { (joint, angle) ->
    val landmarkIndex = joint.toLandmarkIndex()
    val lm = pose.landmarks.getOrNull(landmarkIndex) ?: return@forEach
    drawContext.canvas.nativeCanvas.drawText(
        "${angle.toInt()}°",
        lm.x * size.width + 10.dp.toPx(),
        lm.y * size.height - 10.dp.toPx(),
        anglePaint  // white text, 14sp
    )
}

// 4. Draw barbell trajectory
if (pose.barbellTrajectory.size > 1) {
    val path = Path()
    path.moveTo(
        pose.barbellTrajectory.first().x * size.width,
        pose.barbellTrajectory.first().y * size.height
    )
    pose.barbellTrajectory.drop(1).forEach { pt ->
        path.lineTo(pt.x * size.width, pt.y * size.height)
    }
    drawPath(path, color = BarbellPath, style = Stroke(width = 3.dp.toPx()))
}
```

**Camera permission handling:** If `CAMERA` permission not granted, show `PermissionRationaleDialog` before launching `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())`. If denied permanently, show instructions to open Settings.

---

#### S15 — RecordingReviewScreen

**Purpose:** Preview the recorded video before uploading for AI analysis.

**Layout:** `Scaffold` → `Column`

**Components:**
1. `TopAppBar("Review Recording")` with close button (re-records)
2. **Video Player** — `AndroidView` wrapping `PlayerView` from Media3, fills upper 60% of screen. Acquire player from `PlayerPool`. Plays recorded local file on loop.
3. **Movement Confirmation** — `Card` showing the movement type selected on LiveCameraScreen with option to change via `ExposedDropdownMenuBox`
4. **Video Metadata** — `Row`: duration chip, file size chip
5. `Spacer(Modifier.weight(1f))`
6. `Row(horizontalArrangement = SpaceBetween)`:
   - `OutlinedButton("Re-record")` — pops back to LiveCameraScreen
   - `Button("Analyze with AI")` — uploads video, navigates to CoachingReportScreen

**DisposableEffect** on this screen:
```kotlin
DisposableEffect(Unit) {
    val player = playerPool.acquire("review_player")
    // ... attach to PlayerView
    onDispose { playerPool.release("review_player") }
}
```

---

#### S16 — CoachingReportScreen

**Purpose:** Display the AI-generated coaching analysis. Can be reached from `RecordingReviewScreen` (polling) or directly via deep link.

**Layout:** `Scaffold` → `LazyColumn`

**States with distinct UIs:**

| AnalysisStatus | UI |
|---|---|
| `UPLOADING` | `LinearProgressIndicator(state.uploadProgress)` + "Uploading video..." text |
| `ANALYZING` | `CircularProgressIndicator` + `Text(state.stage.label)` — updates every 3 seconds poll |
| `COMPLETE` | Full report UI (described below) |
| `ERROR` | Error card with `state.error`, `Button("Try Again")` |

**COMPLETE state — LazyColumn items:**

1. **Overall Assessment Card** — `ElevatedCard`:
   - "AI Coaching Report" label, movement type badge
   - `Text(report.overallAssessment, bodyLarge)`
   - Metadata row: `Text("${report.repCount} reps")`, estimated weight

2. **Faults Section Header** — `Text("Movement Faults (${faults.count()})", titleMedium)`

3. **Fault Cards** — one `ElevatedCard` per fault, sorted by `severity` (CRITICAL first):
   - Left accent border colored by `fault.severity.color`
   - `Row`: severity badge chip + `Text(fault.description, titleMedium)`
   - `Text(fault.cue, bodyMedium)` — the coaching instruction
   - `AsyncImage(fault.correctedImageUrl)` via Coil — shows corrected posture image, `240.dp` height, `contentScale = Crop`, `RoundedCornerShape(8.dp)` clip
   - `TextButton("See in video at ${fault.timestampMs.formatTimestamp()}")` → dispatches `CoachingEvent.FaultSelected(fault)` which triggers `CoachingEffect.NavigateToPlayback`

4. **Global Coaching Cues Card** — `ElevatedCard`:
   - `Text("What You're Doing Well", titleMedium)`
   - Bulleted list of `report.globalCues`

5. **Action Row**:
   - `Button("Watch Full Playback")` → navigates to `coaching/playback/{videoId}`
   - `OutlinedButton("Done")` → navigates to `home`

---

#### S17 — VideoPlaybackScreen

**Purpose:** Media3 video player with timed kinematic overlay and fault markers on the seekbar.

**Layout:** Fullscreen `Box`. No bottom nav.

**Component layers:**
1. `AndroidView(factory = { PlayerView(context) })` — fills screen; `PlayerView` configured with `app:resize_mode="fit"`
2. `Canvas(Modifier.fillMaxSize())` — timed overlay; draws pose for the `TimedPoseOverlay` closest to `state.currentPositionMs`
3. Fault markers layer: `Box` overlay with `FaultMarkerDot` composables positioned along the seekbar at their relative `timestampMs` positions
4. Controls overlay (fades out after 3s of inactivity, returns on tap):
   - `TopAppBar` with back arrow and video title
   - Bottom `Column`: `Slider` for scrubbing with fault marker annotations, playback controls row

**Fault marker on seekbar:**
```kotlin
@Composable
fun FaultSeekbarMarker(
    fault: FaultMarker,
    videoDurationMs: Long,
    seekbarWidthPx: Float
) {
    val xFraction = fault.timestampMs.toFloat() / videoDurationMs.toFloat()
    val xOffset = xFraction * seekbarWidthPx
    Box(
        Modifier
            .offset { IntOffset(xOffset.toInt() - 6.dp.roundToPx(), 0) }
            .size(12.dp)
            .background(fault.severity.color, CircleShape)
    )
}
```

**Overlay sync logic:** On each Media3 `Player.Listener.onPositionDiscontinuity` and on a 16ms `LaunchedEffect` ticker:
```kotlin
val currentPositionMs by remember { derivedStateOf { player.currentPosition } }
val overlayFrame = state.overlayData.minByOrNull {
    abs(it.timestampMs - currentPositionMs)
}
// Render overlayFrame on Canvas
```

**Player pool usage:**
```kotlin
DisposableEffect(videoId) {
    val player = playerPool.acquire("playback_$videoId")
    player.setMediaItem(MediaItem.fromUri(videoUrl))
    player.prepare()
    player.seekTo(initialTimestampMs)
    // Attach to PlayerView via AndroidView factory
    onDispose { playerPool.release("playback_$videoId") }
}
```

---

#### S18 — ProfileScreen

**Purpose:** User profile display, settings, and logout.

**Layout:** `Scaffold` → `LazyColumn`

**Components:**
1. **Profile Header** — `Column(horizontalAlignment = CenterHorizontally)`:
   - `AsyncImage` for avatar (or initials placeholder `Box` with `ApexOrange` background), `72.dp` circle
   - `Text(state.displayName, headlineMedium)`
   - `Text(state.email, bodyMedium, OnSurfaceVariant)`
2. **Stats Summary Row** — `Row(SpaceEvenly)`:
   - Total workouts logged count
   - Total PRs count
   - Member since date
3. **Settings Section** — `Text("Settings", titleMedium, Modifier.padding(16.dp))`:
   - `ListItem` — Unit system toggle: "Metric (kg)" / "Imperial (lbs)" → `Switch`
   - `ListItem` — Dark mode (placeholder for future)
4. **Data Section**:
   - `ListItem` — "Health Connect" → shows sync status and link to HealthConnectSetupScreen
   - `ListItem` — "Export Data" (placeholder, deferred)
5. **Danger Zone**:
   - `TextButton("Sign Out", color = FaultCritical)` → dispatches `AuthEvent.LogoutClicked`, navigates to LoginScreen

---

### 4.3 Reusable Components

#### ACWRGaugeComposable

```kotlin
// com.apexai.crossfit.core.ui.component.ACWRGauge

@Composable
fun ACWRGauge(
    acwr: Float?,
    zone: ReadinessZone?,
    modifier: Modifier = Modifier,
    diameter: Dp = 160.dp
) {
    // Draws an arc from 210° to -30° (240° sweep)
    // Arc color transitions: Blue (< 0.8) → Green (0.8-1.3) → Amber (1.3-1.5) → Red (> 1.5)
    // Needle pointer at ACWR position
    // Center text: ACWR value or "--" if null
    Canvas(modifier.size(diameter)) {
        // Background arc (unfilled track)
        // Filled arc up to ACWR position, color = zone.color
        // Needle line from center to arc edge
        // Zone boundary tick marks at 0.8, 1.3, 1.5
    }
}
```

**Parameters:**
- `acwr: Float?` — null shows "--" in center
- `zone: ReadinessZone?` — determines arc color
- `diameter: Dp` — default 160.dp for cards, 200.dp for hero display

#### WodSummaryCard

```kotlin
@Composable
fun WodSummaryCard(
    workout: WorkoutSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

Renders: time domain badge (`AssistChip`), workout name (`titleMedium`), movement count (`labelSmall`), tap target is full card.

#### ShimmerBox

```kotlin
@Composable
fun ShimmerBox(modifier: Modifier = Modifier)
```

Animated shimmer placeholder using `InfiniteTransition` with `LinearGradient` sweep. Used for all loading states.

#### MovementRowItem

```kotlin
@Composable
fun MovementRowItem(
    movement: WorkoutMovement,
    modifier: Modifier = Modifier
)
```

Renders: movement name, prescribed reps/weight/distance/calories, equipment chip. Used in WodDetailScreen and WodTimerScreen reference list.

#### PrChipComponent

```kotlin
@Composable
fun PrChip(
    pr: PersonalRecord,
    onClick: () -> Unit
)
```

Compact chip with trophy icon, movement name (truncated), PR value in `ApexGold`. Used in HomeScreen recent PRs row.

#### PrTrendChart

```kotlin
@Composable
fun PrTrendChart(
    history: List<PrHistoryEntry>,
    unit: PrUnit,
    modifier: Modifier = Modifier
)
```

Custom Compose Canvas line chart. X-axis = date, Y-axis = value. Dots at each data point. Tap on dot shows tooltip with exact value and date.

#### ErrorStateCard

```kotlin
@Composable
fun ErrorStateCard(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
)
```

`ElevatedCard` with warning icon, error message, optional retry button. Used uniformly across all error states.

#### EmptyStateComposable

```kotlin
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
)
```

Centered column: large icon (48.dp, tinted `OnSurfaceVariant`), title in `titleMedium`, optional subtitle, optional action composable (usually a Button).

---

### 4.4 Navigation and Transitions

#### Transition Animations

| Transition | Enter | Exit |
|---|---|---|
| Bottom nav tab switch | `fadeIn(tween(200))` | `fadeOut(tween(200))` |
| Push navigation (WOD detail, PR detail) | `slideInHorizontally { it }` | `slideOutHorizontally { -it/3 }` |
| Back navigation | `slideInHorizontally { -it/3 }` | `slideOutHorizontally { it }` |
| Full-screen (LiveCamera, CoachingReport) | `slideInVertically { it }` | `slideOutVertically { it }` |
| Dialog / Bottom sheet | Material default spring animation | Material default |

All transition animations are registered on each `composable()` entry in the NavHost using `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition` parameters.

#### Bottom Navigation Back Stack Behavior

- Each bottom nav tab maintains its own back stack. Switching tabs preserves the back stack of the previous tab (`saveState = true`, `restoreState = true` in `NavOptions`).
- Pressing back from a bottom nav root destination exits the app (standard Android behavior).
- The camera FAB (`vision/live`) opens as a new full-screen destination outside the bottom nav graph. Back press from LiveCameraScreen returns to the previously focused bottom nav destination.

#### Deep Link Handling

Deep links are processed in `MainActivity.onCreate()` and `onNewIntent()`. The NavController handles the link routing automatically. Deep link URLs:
- `apexai://wod/{wodId}` — navigates to WodDetailScreen
- `apexai://coaching/report/{analysisId}` — navigates to CoachingReportScreen

These are used for push notification taps (future feature) and share links.

---

### 4.5 Forms and Validation

#### Login Form

| Field | Validation Rule | Error Message |
|---|---|---|
| Email | Must match `Patterns.EMAIL_ADDRESS` | "Please enter a valid email address" |
| Password | Minimum 1 character (server validates strength) | "Password is required" |
| Both | Server returns 400 on wrong credentials | "Invalid email or password" |

#### Register Form

| Field | Validation Rule | Error Message |
|---|---|---|
| Display Name | 2–50 characters, non-empty after trim | "Name must be between 2 and 50 characters" |
| Email | `Patterns.EMAIL_ADDRESS` match | "Please enter a valid email address" |
| Password | Minimum 8 characters | "Password must be at least 8 characters" |
| Confirm Password | Must equal password field | "Passwords do not match" |

Validation triggers on field focus loss (`onFocusChanged`), not on every keystroke. The submit button becomes enabled only when all fields have been touched and pass validation.

#### WOD Log Form

| Field | Validation Rule | Error Message |
|---|---|---|
| Score (REPS) | Positive integer, > 0 | "Score must be a positive number" |
| Score (TIME) | Matches `[0-9]{1,2}:[0-5][0-9]` | "Format must be MM:SS (e.g. 12:34)" |
| Score (LOAD) | Positive decimal, > 0 | "Weight must be a positive number" |
| RPE | Integer 1–10 (optional) | "RPE must be between 1 and 10" (if entered) |

#### General Form Behavior

- Validation error messages appear as `supportingText` in `OutlinedTextField` using Material 3's built-in error state (`isError = true`, `supportingText = { Text(errorMsg) }`).
- No field shows an error until the user has interacted with it (dirty state tracking via a `Set<String>` of field keys).
- Form-level errors (from server responses) appear in the `ErrorBanner` composable above the submit button.

---

### 4.6 Accessibility

#### Content Descriptions

Every non-text interactive element requires a `contentDescription`:

```kotlin
IconButton(
    onClick = { navController.popBackStack() },
    modifier = Modifier.semantics { contentDescription = "Go back" }
) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
// contentDescription on the IconButton via semantics; null on the Icon itself to avoid duplication
```

Key content descriptions:

| Component | Content Description |
|---|---|
| Camera FAB | "Open video coaching camera" |
| Record button (not recording) | "Start recording" |
| Record button (recording) | "Stop recording" |
| ACWRGauge | "Readiness score: {acwr}, zone: {zone.label}" |
| Fault severity chip | "{severity} severity fault" |
| PR chip | "{movementName} personal record: {value} {unit}" |
| Corrected posture image | "Corrected technique illustration for {fault.description}" |

#### Minimum Touch Targets

All interactive elements: minimum `48.dp × 48.dp`. Apply `Modifier.minimumInteractiveComponentSize()` to any icon-only button that might fall below this threshold.

#### Contrast Requirements

All text must meet WCAG 2.1 AA minimum contrast:
- Body text on dark surfaces: `OnSurface` (`#E8E8E8`) on `Surface` (`#121212`) — contrast ratio ~15:1 (passes)
- Secondary text: `OnSurfaceVariant` (`#9E9E9E`) on `Surface` (`#121212`) — contrast ratio ~6:1 (passes AA)
- `ApexOrange` (`#FF6B35`) on `Surface` (`#121212`) — verify with contrast checker before finalizing; use as accent only, not for large body text blocks

#### Screen Reader Navigation

- Use `semantics { heading() }` on all section header `Text` composables.
- Ensure `LazyColumn` items have unique accessibility labels when content is repeated (e.g., multiple WOD cards with the same name should include their time domain in the description).
- The Canvas overlay on LiveCameraScreen is purely visual. Add a `semantics { contentDescription = "Live camera with pose detection overlay" }` on the root Box so screen readers announce the screen purpose without attempting to read Canvas drawings.

---

## 5. Implementation Roadmap

### Milestone 1 — Foundation (Week 1, Days 1–3)

**Deliverables:**
1. Gradle multi-module project scaffolding with version catalog and convention plugins
2. `core-ui` — Material 3 dark theme, color tokens, typography, shape system
3. `core-model` — All domain entity data classes (zero Android imports)
4. `core-common` — AppError sealed class, dispatcher qualifiers, extension functions
5. App shell — MainActivity, NavHost, BottomNavigation scaffold with 5 placeholder tab screens

**Testable outcome:** App compiles, launches on emulator, navigates between 5 placeholder tabs with correct bottom navigation behavior.

---

### Milestone 2 — Auth and Supabase (Week 1, Days 4–7)

**Deliverables:**
6. `core-network` — OkHttp client, AuthInterceptor, TokenRefreshAuthenticator, Supabase Retrofit interface
7. `core-data` — DataStore setup, EncryptedSharedPreferences wrapper, `AppStateManager`
8. `feature-auth` — LoginScreen, RegisterScreen, ProfileScreen with ViewModels
9. Supabase project provisioned; `profiles`, `workouts`, `movements`, `workout_movements`, `results`, `personal_records` tables created

**Testable outcome:** User can register, log in, persist session across app restart, and log out. Auth state drives correct navigation.

---

### Milestone 3 — WOD Tracking (Week 1–2 overlap)

**Deliverables:**
10. `core-database` — Room database with `CachedWorkout`, `CachedMovement`, `CachedResult`, `CachedPersonalRecord` entities and DAOs
11. `feature-wod` — All 5 WOD screens with ViewModels
12. `WorkoutRepository` implementation — Supabase REST + Room offline cache

**Testable outcome:** User can browse the WOD catalog, search by name and filter by time domain, open a WOD detail, run the timer, and log a result. History screen shows past results.

---

### Milestone 4 — PR Tracking (Week 2)

**Deliverables:**
13. PostgreSQL PR trigger (`check_and_update_pr`) applied in Supabase
14. `feature-pr` — PrDashboardScreen and PrDetailScreen with ViewModels and trend chart
15. `PrRepository` implementation

**Testable outcome:** After logging a WOD result that beats a prior result for a movement, the PR dashboard reflects the new PR automatically. No client-side computation.

---

### Milestone 5 — Readiness Score (Week 2)

**Deliverables:**
16. `core-health` — `HealthConnectManager` with HRV, sleep, heart rate read methods
17. `calculate_readiness` Edge Function applied in Supabase
18. `health_snapshots` table created with RLS
19. `feature-readiness` — ReadinessDashboardScreen and HealthConnectSetupScreen with ViewModels

**Testable outcome:** On a physical device with Oura/Garmin/Whoop data in Health Connect, the readiness dashboard shows a calculated ACWR score with zone classification and recommendation.

---

### Milestone 6 — Live Vision (Week 3, Days 1–3)

**Deliverables:**
20. `core-media` — `CameraManager`, `PoseAnalyzer`, `PoseLandmarkerFactory`, `PlayerPool`
21. `feature-vision` — LiveCameraScreen with Canvas overlay, RecordingReviewScreen
22. `CalculateJointAnglesUseCase` in domain layer

**Testable outcome:** On a physical device, the camera opens with skeletal overlay rendering joint angles in real time at ≥ 25 FPS. User can record a clip and review it.

---

### Milestone 7 — AI Coaching (Week 3, Days 4–7)

**Deliverables:**
23. FastAPI microservice deployed (Docker container on Cloud Run or Railway)
24. `video_uploads`, `coaching_reports`, `movement_faults` tables created with RLS
25. `feature-coaching` — CoachingReportScreen and VideoPlaybackScreen with ViewModels
26. `CoachingRepository` — upload via WorkManager, poll, fetch report

**Testable outcome:** User records a lift, approves the clip, uploads it, and within 60 seconds receives a coaching report with textual fault descriptions and corrected posture images. Video playback screen shows kinematic overlay synchronized to the video.

---

### Milestone 8 — Polish and Deploy (Week 4)

**Deliverables:**
27. Compose recomposition audit using Layout Inspector — fix any unnecessary recompositions on LiveCameraScreen
28. LeakCanary integration — confirm no memory leaks in PlayerPool or camera pipeline
29. ProGuard rules verified for release build
30. GitHub Actions workflow and Fastlane configured; first signed AAB deployed to Play Store internal testing track
31. Certificate pins populated with actual SHA-256 hashes

**Testable outcome:** Signed release AAB deployed to Play Store internal track via CI/CD. All milestone test outcomes verified on release build.

---

### Feature Phasing Summary

| Phase | Feature | Notes |
|---|---|---|
| MVP (Week 1–4) | Auth, WOD logging, PR tracking, Readiness score, Live camera + pose, AI coaching | All required for initial launch |
| V1 (post-launch) | Social / leaderboards | 10% effort allocation noted in PDF; deferred; requires `feature-social` module |
| V1 | Barbell trajectory (OpenCV/YOLOv8) | Stretch goal; VisionRepository already has interface seam for drop-in addition |
| V1 | Oxygen saturation (SpO2) from Health Connect | `OxygenSaturationRecord` type; one additional permission |
| V2 | Semantic AI search ("show me workouts with heavy barbell cycling") | Requires pgvector extension in Supabase; embeddings on movement catalog |
| V2 | Custom WOD builder | Write permissions on `workouts` table; additional `created_by` logic |

---

## 6. Open Questions and Assumptions

### Open Questions

| # | Question | Impact |
|---|---|---|
| OQ-1 | No wireframes or design mockups exist for any screen. | Frontend agent must make all visual hierarchy decisions. This spec provides component inventory and layout guidance; specific pixel-level decisions (icon sizes, spacing variations, illustration style) are at the agent's discretion within the Material 3 constraints defined in Section 4.1. |
| OQ-2 | Supabase project credentials have not been provisioned. | Debug builds use placeholder values. A Supabase project must be created and the URL + anon key must be populated in GitHub Secrets before the auth milestone can be tested. |
| OQ-3 | The Google Cloud project with Gemini API access has not been confirmed. | The FastAPI microservice cannot be functionally tested without a valid `GEMINI_API_KEY` with billing enabled. Context Caching is a paid feature. |
| OQ-4 | Certificate pin SHA-256 hashes are placeholder values in this spec. | Release builds will fail certificate pinning until the correct hashes are obtained from `openssl s_client -connect xxxx.supabase.co:443 \| openssl x509 -pubkey -noout \| openssl pkey -pubin -outform der \| openssl dgst -sha256 -binary \| base64`. |
| OQ-5 | Play Store developer account status is unconfirmed. | Play Store deployment (Milestone 8) requires an active Google Play Console account ($25 one-time fee) and the app must pass the initial review process. |
| OQ-6 | Knee valgus detection requires front-facing camera view but the app defaults to rear camera in profile view. | The spec includes a UI note flagging this limitation. A formal decision is needed: either add a front-camera mode for squat analysis or document valgus as out of scope for V1. |
| OQ-7 | The FastAPI microservice's hosting environment (Cloud Run, Railway, Fly.io, or self-hosted) is not specified. | This affects the production `FASTAPI_BASE_URL` in `BuildConfig` and the `CertificatePinner` configuration. Decision needed before Milestone 8. |

### Assumptions Made

| # | Assumption | Rationale |
|---|---|---|
| A-1 | Gemini model is `gemini-1.5-pro` for video analysis and `gemini-2.0-flash` for image generation. | The PDF references "Gemini 3.1 Pro" which does not exist at knowledge cutoff (August 2025). The architecture uses a config parameter so switching models requires only an environment variable change. |
| A-2 | `OxygenSaturationRecord` from Health Connect is excluded from MVP. | Mentioned once in the PDF but absent from the CLAUDE.md permissions list. The ACWR algorithm does not require it. Added to V1 roadmap. |
| A-3 | Barbell tracking (OpenCV/YOLOv8) is deferred to V1. | The PDF marks it as a stretch goal. The `VisionRepository` interface contains the seam for adding a barbell detector without ViewModel changes. |
| A-4 | Social/leaderboard features are deferred to post-MVP. | The PDF allocates 10% effort but provides no feature details. No architecture decisions block this addition. It will require a new `feature-social` module. |
| A-5 | Video storage bucket in Supabase is configured as private with signed URL generation. | Required for user data privacy (RLS on database rows does not cover storage objects by default). Bucket policy must be set explicitly in the Supabase dashboard. |
| A-6 | The `score_numeric` field on `WorkoutResultInput` is provided by the Android client. | The PostgreSQL PR trigger depends on this field. The client is responsible for parsing the user's text score entry into a numeric value. If parsing fails, `score_numeric` is submitted as null and the PR trigger returns without action. |
| A-7 | `WorkManager` is used for video uploads rather than a foreground service. | WorkManager's `CoroutineWorker` is appropriate for a network upload task that can tolerate deferral if the app is backgrounded. A foreground service would be needed only if instant upload is required even when the phone is locked; this is not required for the coaching use case. |
| A-8 | Health Connect sync is triggered manually by the user (tap "Sync Health Data") and automatically on app foreground via `LaunchedEffect`. | Background sync (via WorkManager periodic task) is out of scope for MVP but can be added in V1. |
| A-9 | Unit system (kg vs lbs) is a display-only setting. | All values stored in the database are in SI units (kg, meters). The Android client converts for display based on the `SELECTED_UNIT_SYSTEM` DataStore preference. |

---

*End of ApexAI Athletics Technical Specification — Version 1.0*
agentId: a1adc1e3799311976 (for resuming to continue this agent's work if needed)
<usage>total_tokens: 130119
tool_uses: 16
duration_ms: 915130</usage>
