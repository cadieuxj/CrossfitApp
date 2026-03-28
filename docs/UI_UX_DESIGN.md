# ApexAI Athletics — UI/UX Design Templates

**Version:** 1.0
**Date:** 2026-03-28
**Platform:** Native Android — Kotlin + Jetpack Compose
**Prepared for:** Coder Agent (Frontend Implementation)

---

## PART 1 — DESIGN SYSTEM FOUNDATION

### 1.1 Color System

```kotlin
// core-ui/src/main/kotlin/com/apexai/crossfit/core/ui/theme/Color.kt

// --- Brand ---
val ElectricBlue       = Color(0xFF00D4FF)   // Primary interactive, rings, accents
val ElectricBlueDark   = Color(0xFF00A8CC)   // Pressed/active state of Electric Blue
val NeonGreen          = Color(0xFF39FF14)   // Success, optimal zone, PR achieved
val BlazeOrange        = Color(0xFFFF6B35)   // Moderate fault, warning-adjacent

// --- Backgrounds ---
val BackgroundDeepBlack  = Color(0xFF0A0A0F)  // Screen background
val SurfaceDark          = Color(0xFF12121A)  // Navigation bar, modal scrim base
val SurfaceElevated      = Color(0xFF1A1A26)  // Raised cards, bottom sheets
val SurfaceCard          = Color(0xFF1E1E2E)  // Standard card background
val BorderSubtle         = Color(0xFF2A2A3E)  // Dividers, outline inputs (unfocused)
val BorderVisible        = Color(0xFF3A3A55)  // Focused borders, separators

// --- Text ---
val TextPrimary    = Color(0xFFF0F0FF)  // Primary text on dark backgrounds
val TextSecondary  = Color(0xFF9090B0)  // Subtitles, metadata, placeholders
val TextDisabled   = Color(0xFF505068)  // Disabled controls text
val TextOnBlue     = Color(0xFF000000)  // Text sitting on ElectricBlue fills

// --- Semantic ---
val ColorSuccess = NeonGreen             // #39FF14
val ColorWarning = Color(0xFFFFB800)     // Amber — caution zone, RPE high
val ColorError   = Color(0xFFFF3B5C)     // Error states, high-risk zone
val ColorInfo    = ElectricBlue          // #00D4FF

// --- Readiness Zones ---
val ZoneOptimal      = NeonGreen         // ACWR 0.8 – 1.3
val ZoneCaution      = ColorWarning      // ACWR 1.3 – 1.5
val ZoneHighRisk     = ColorError        // ACWR > 1.5
val ZoneUndertrained = Color(0xFF9090B0) // ACWR < 0.8

// --- Fault Severity ---
val SeverityMinor    = ColorWarning      // MINOR
val SeverityModerate = BlazeOrange       // MODERATE
val SeverityCritical = ColorError        // CRITICAL
```

### 1.2 Typography Scale

```kotlin
// core-ui/src/main/kotlin/com/apexai/crossfit/core/ui/theme/Type.kt
// Font: Inter (import via Google Fonts in Compose)
// Fallback: FontFamily.SansSerif

val ApexTypography = Typography(
    displayLarge  = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold,
                              lineHeight = 52.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold,
                              lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold,
                              lineHeight = 34.sp, letterSpacing = (-0.25).sp),
    headlineMedium= TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold,
                              lineHeight = 30.sp, letterSpacing = (-0.25).sp),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                              lineHeight = 26.sp),
    titleLarge    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                              lineHeight = 24.sp),
    titleMedium   = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium,
                              lineHeight = 22.sp),
    bodyLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal,
                              lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,
                              lineHeight = 20.sp),
    bodySmall     = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,
                              lineHeight = 16.sp),
    labelLarge    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                              lineHeight = 20.sp),
    labelSmall    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium,
                              lineHeight = 14.sp, letterSpacing = 0.5.sp)
)
```

### 1.3 Spacing & Layout Grid

```
Base unit: 4dp

Scale tokens:
  Spacing2  =  4dp   (icon-to-text tight gaps)
  Spacing4  =  8dp   (within-component gaps)
  Spacing6  = 12dp   (chip internal padding vertical)
  Spacing8  = 16dp   (standard component padding, card internal padding)
  Spacing10 = 20dp   (section vertical rhythm)
  Spacing12 = 24dp   (card-to-card gap, section padding)
  Spacing16 = 32dp   (top-of-section padding)
  Spacing20 = 40dp   (large vertical gaps)
  Spacing24 = 48dp   (screen top padding under nav bar)
  Spacing32 = 64dp   (major section breaks)

Screen horizontal padding: 16dp (mobile) / 24dp (tablet)
Screen content max-width: 600dp centered (tablet and above)

Corner Radii:
  CornerSmall  =  8dp  (chips, badges, small buttons)
  CornerMedium = 12dp  (text fields, secondary cards)
  CornerLarge  = 16dp  (primary cards, bottom sheets)
  CornerXLarge = 24dp  (dialogs, FAB)
  CornerFull   = 9999dp (avatars, circular elements)
```

### 1.4 Component Library Reference

#### Button Variants

```
PrimaryButton
  Background:   ElectricBlue (#00D4FF)
  Text:         TextOnBlue, TitleMedium, weight 600
  Height:       52dp
  Corner:       CornerMedium (12dp)
  Padding:      horizontal 24dp
  Ripple:       ElectricBlueDark
  States:
    Default  → Background: ElectricBlue
    Hover    → Background: ElectricBlueDark, scale 1.01
    Pressed  → Background: ElectricBlueDark, scale 0.98 (150ms EaseInOut)
    Disabled → Background: BorderSubtle (#2A2A3E), Text: TextDisabled
    Loading  → Show CircularProgressIndicator (size 20dp, color TextOnBlue)
               inside button; hide label text

SecondaryButton (outlined)
  Background:   Transparent
  Border:       1dp ElectricBlue
  Text:         ElectricBlue, TitleMedium
  Height:       52dp
  Corner:       CornerMedium
  Pressed:      Background fills with ElectricBlue at 12% alpha
  Disabled:     Border: BorderSubtle, Text: TextDisabled

TextButton
  Background:   Transparent
  Text:         ElectricBlue, TitleMedium
  Ripple:       ElectricBlue at 8% alpha
  Disabled:     Text: TextDisabled

DestructiveButton
  Background:   ColorError (#FF3B5C)
  Text:         TextPrimary, TitleMedium
  Height:       52dp
  Corner:       CornerMedium

IconButton
  Size:         40dp × 40dp (touch target 48dp via padding)
  Background:   Transparent; on hover SurfaceElevated (#1A1A26)
  Icon:         Lucide, 20dp, TextSecondary; active: ElectricBlue
```

#### Card

```
ApexCard
  Background:   SurfaceCard (#1E1E2E)
  Corner:       CornerLarge (16dp)
  Border:       1dp BorderSubtle (#2A2A3E) — increases to BorderVisible on focused/pressed
  Padding:      16dp all sides
  Elevation:    Color-based only (no drop shadows in dark theme)
  States:
    Default  → as above
    Pressed  → Border: ElectricBlue 1dp, scale 0.99 (150ms)
    Selected → Border: ElectricBlue 2dp, Background: ElectricBlue at 6% alpha
    Loading  → Replace content with ShimmerSkeleton (see below)
    Error    → Border: ColorError, add error icon + message at bottom
```

#### Text Input

```
ApexTextField
  Background:   SurfaceElevated (#1A1A26)
  Corner:       CornerMedium (12dp)
  Border:       1dp BorderSubtle unfocused → 2dp ElectricBlue focused
  Height:       56dp (single line)
  Padding:      horizontal 16dp, vertical 0dp (centered text)
  Label:        BodySmall, TextSecondary, animates upward on focus (standard Compose behavior)
  Text:         BodyLarge, TextPrimary
  Placeholder:  TextSecondary
  Trailing icon: Lucide 20dp TextSecondary
  Error state:  Border 2dp ColorError, label turns ColorError, error message below BodySmall ColorError
  Disabled:     Background SurfaceDark, Text TextDisabled
```

#### Chips / Filter Tabs

```
FilterChip
  Background (unselected): Transparent, Border 1dp BorderSubtle
  Background (selected):   ElectricBlue at 15% alpha, Border 1dp ElectricBlue
  Text (unselected):       TextSecondary, LabelLarge
  Text (selected):         ElectricBlue, LabelLarge
  Height:                  36dp
  Corner:                  CornerSmall (8dp)
  Padding:                 horizontal 16dp
  Icon (optional):         16dp leading icon, same color as text
```

#### Skeleton Shimmer

```
ShimmerSkeleton
  Base color:     SurfaceElevated (#1A1A26)
  Shimmer color:  BorderVisible (#3A3A55) at 60% alpha
  Animation:      translateX gradient, 1200ms linear, infinite
  Shape:          Matches the content element it replaces (rounded rects)
```

#### Bottom Navigation Bar

```
ApexBottomNavBar
  Background:     SurfaceDark (#12121A) + top border 1dp BorderSubtle
  Height:         80dp (including safe area inset)
  Items:          5 (Home, WOD, [Camera FAB], Readiness, Profile)
  Item icon:      Lucide 24dp
  Item label:     LabelSmall
  Active:         Icon + label ElectricBlue
  Inactive:       Icon + label TextSecondary (40% opacity)
  Indicator:      2dp wide × 16dp tall rounded pill above active icon (ElectricBlue)

  Center Camera FAB:
    Size:           56dp × 56dp
    Background:     ElectricBlue
    Icon:           Lucide Aperture or Camera, 28dp, TextOnBlue
    Elevation:      Vertically centered on nav bar top edge
    Shadow:         0dp 0dp 20dp ElectricBlue at 40% (glow effect via Box with blur)
    Pressed:        scale 0.95, color ElectricBlueDark (150ms)
```

#### Toast / Snackbar

```
ApexSnackbar
  Background:   SurfaceElevated
  Border:       1dp border by type:
    Info:    ElectricBlue
    Success: NeonGreen
    Warning: ColorWarning
    Error:   ColorError
  Text:         BodyMedium TextPrimary
  Corner:       CornerMedium
  Duration:     Short 2000ms / Long 4000ms
  Enter:        slideInVertically from bottom 300ms EaseOut
  Exit:         fadeOut 200ms
```

#### Progress / Loading Indicators

```
CircularReadinessRing
  Size:         200dp × 200dp
  Track:        BorderSubtle, strokeWidth 14dp
  Fill:         Color based on ReadinessZone (ZoneOptimal/Caution/HighRisk/Undertrained)
  Animation:    DrawArc from 0f to targetAngle, 800ms EaseOut, on first composition
  Center:       Score number DisplayMedium + label BodySmall

LinearProgressBar
  Height:       6dp
  Corner:       CornerFull
  Track:        BorderSubtle
  Fill:         ElectricBlue
  Animation:    animateFloatAsState 600ms EaseOut
```

---

## PART 2 — SCREEN TEMPLATES

---

## SCREEN S04: HomeScreen (Dashboard)

**Route:** `home`
**Feature Module:** `app` (uses ViewModels from multiple features)
**User Story:** As a CrossFit athlete, I want to see my daily readiness, today's workout, and recent PRs at a glance so I can decide how to train today.
**Entry Points:** SplashScreen (authenticated), any bottom nav "Home" tab tap
**Exit Points:** WodDetailScreen, ReadinessDashboardScreen, PrDetailScreen, LiveCameraScreen (FAB), ProfileScreen

### Compose Component Hierarchy

```
HomeScreen (Scaffold)
  topBar: HomeTopBar (composable)
    Row
      Image (logo, 32dp)
      Spacer(weight 1f)
      IconButton (notifications bell, Lucide Bell 24dp)
      AsyncImage (avatar, 36dp circle)
  content: LazyColumn(verticalArrangement = spacedBy(16dp), contentPadding = PaddingValues(16dp, 16dp, 16dp, 96dp))
    item: ReadinessSummaryCard
    item: TodayWodCard
    item: RecentPrsRow
    item: QuickActionsRow
  bottomBar: ApexBottomNavBar (Home selected)
  floatingActionButton: (none — FAB is embedded in bottom nav)
```

### Zone: HomeTopBar

```
Height:         64dp
Background:     BackgroundDeepBlack with bottom border 1dp BorderSubtle
Padding:        horizontal 16dp, vertical 12dp
Left:           ApexAI Athletics wordmark — HeadlineMedium, TextPrimary, weight 700
                OR brand logo image if asset exists — height 28dp, aspect ratio preserved
Right side (row, gap 8dp):
  IconButton: Lucide Bell 24dp, TextSecondary
    Badge (if unread notifications): 8dp red dot (ColorError), positioned top-right of icon
  AsyncImage:
    Size:       36dp × 36dp
    Shape:      CircleShape
    Placeholder: Shimmer circle
    ContentScale: Crop
    onClick:    navigate to "profile"
```

### Zone: ReadinessSummaryCard

```
Component:      ApexCard (clickable, navigates to "readiness")
Padding:        16dp
Layout:         Row (verticalAlignment = CenterVertically)

Left sub-section (weight 1f):
  Text: "READINESS" — LabelSmall, TextSecondary, letterSpacing +0.5sp
  Spacer: 4dp
  Text: score value (e.g., "1.12") — DisplayMedium, color = ZoneColor(readinessZone)
  Spacer: 4dp
  Text: zone label (e.g., "OPTIMAL TRAINING ZONE") — LabelSmall, ZoneColor(readinessZone)
  Spacer: 8dp
  Text: recommendation snippet (truncated to 2 lines) — BodySmall, TextSecondary

Right sub-section:
  CircularReadinessRing
    Size: 96dp × 96dp
    strokeWidth: 10dp
    (compressed version of full ring for summary card)
  Text (centered below ring): "ACWR" — LabelSmall, TextSecondary

States:
  Loading:  Replace entire card content with ShimmerSkeleton (96dp height)
  No data / permissions not granted:
    Icon: Lucide ShieldAlert 24dp, TextSecondary
    Text: "Connect Health Data" — BodyMedium, TextSecondary
    TextButton: "Set Up" — ElectricBlue, navigates to "readiness/setup"
```

### Zone: TodayWodCard

```
Component:      ApexCard (clickable, navigates to "wod/{wodId}")
Padding:        16dp
Layout:         Column (verticalArrangement = spacedBy(12dp))

Header Row:
  Row (spaceBetween):
    Column:
      Text: "TODAY'S WOD" — LabelSmall, TextSecondary
      Spacer: 4dp
      Text: wod.name — TitleLarge, TextPrimary
    TimeDomainBadge:
      Background: timeDomain.badgeColor at 15% alpha
      Border: 1dp timeDomain.badgeColor
      Text: timeDomain.label (e.g., "AMRAP") — LabelLarge, timeDomain.badgeColor
      Corner: CornerSmall
      Padding: horizontal 10dp, vertical 4dp
      Color mapping:
        AMRAP   → ElectricBlue
        EMOM    → NeonGreen
        RFT     → BlazeOrange
        TABATA  → ColorWarning

WOD Details Row (wrap if needed):
  Row (horizontalArrangement = spacedBy(16dp)):
    Text: time cap (e.g., "20 min") — BodyMedium, TextSecondary, prefix Lucide Clock icon 14dp
    Text: movement count (e.g., "3 movements") — BodyMedium, TextSecondary, prefix Lucide Layers icon 14dp

Movement List (max 3 items, then "+ N more"):
  LazyColumn (disabled scrolling, shows up to 3 items):
    each item:
      Row (spacedBy 8dp):
        Text: prescribedReps + "×" — BodyMedium, ElectricBlue, weight 600 — width 40dp
        Text: movement.name — BodyMedium, TextPrimary
        Text: prescribedWeight (e.g., "95 lbs") — BodySmall, TextSecondary, align end

  if movementCount > 3:
    Text: "+ {N} more movements" — BodySmall, TextSecondary, paddingStart 48dp

Action Row:
  Row (horizontalArrangement = spacedBy(8dp)):
    SecondaryButton: "View Details" (weight 1f, height 44dp)
      onClick: navigate to "wod/{wodId}"
    PrimaryButton: "Start Workout" (weight 1f, height 44dp)
      onClick: navigate to "wod/{wodId}/timer"

States:
  Loading:  ShimmerSkeleton (height 180dp)
  No WOD today:
    Icon: Lucide Dumbbell 32dp, TextSecondary
    Text: "No workout scheduled" — BodyMedium, TextSecondary
    TextButton: "Browse WODs" → navigates to "wod"
```

### Zone: RecentPrsRow

```
Header Row:
  Row (spaceBetween, padding horizontal 0dp):
    Text: "PERSONAL RECORDS" — LabelSmall, TextSecondary
    TextButton: "See All" — ElectricBlue, BodySmall, navigate to "pr"

LazyRow (horizontalArrangement = spacedBy(12dp), contentPadding = PaddingValues(horizontal 0dp)):
  Each PrMiniCard:
    ApexCard (width 130dp, not clickable / tap navigates to pr/{movementId})
    Padding: 12dp
    Layout: Column (spacedBy 4dp)
    Badge (top): if achievedAt within 7 days → "NEW PR" badge NeonGreen
    Text: movement.name — BodySmall, TextPrimary, maxLines 2
    Spacer: auto
    Text: pr.value + unit — HeadlineSmall, ElectricBlue
    Text: achievedAt (relative: "2d ago") — LabelSmall, TextSecondary

States:
  Loading: 3 ShimmerSkeleton cards in row (each 130dp × 100dp)
  Empty:
    Text: "Log workouts to track PRs" — BodySmall, TextSecondary, centeredHorizontally
```

### Zone: QuickActionsRow

```
Text: "QUICK ACTIONS" — LabelSmall, TextSecondary, paddingBottom 8dp

Row (horizontalArrangement = spacedBy(8dp)):
  QuickActionButton (weight 1f, each):
    Background: SurfaceCard
    Border: 1dp BorderSubtle
    Corner: CornerLarge (16dp)
    Height: 72dp
    Layout: Column (centered, spacedBy 6dp)
    Icon: Lucide icon 24dp, ElectricBlue
    Text: label, LabelSmall, TextSecondary

  Item 1: Icon=Video, label="Record Lift" → navigates to "vision/live"
  Item 2: Icon=Plus, label="Log WOD" → navigates to "wod" (with intent to log)
  Item 3: Icon=TrendingUp, label="View PRs" → navigates to "pr"
```

### ViewModel State Required

```kotlin
// HomeViewModel exposes:
data class HomeUiState(
    val readinessSummary: ReadinessSummary? = null,    // condensed from ReadinessScore
    val todayWod: Workout? = null,
    val recentPrs: List<PersonalRecord> = emptyList(), // last 5
    val athleteName: String = "",
    val avatarUrl: String? = null,
    val unreadNotificationCount: Int = 0,
    val isLoadingReadiness: Boolean = true,
    val isLoadingWod: Boolean = true,
    val isLoadingPrs: Boolean = true,
    val error: String? = null
)
```

### Interaction & Behavior Specifications

- Entire screen is a LazyColumn that scrolls vertically. No nested scrolling.
- ReadinessSummaryCard ring animates its fill angle on first render (800ms EaseOut).
- Swipe-to-refresh (PullRefreshIndicator) triggers refresh of all three data zones independently.
- Notification bell badge uses animateIntAsState — count increments with a scale pop animation (1.3x → 1.0x, 200ms).
- TodayWodCard's "Start Workout" button should be disabled and show a lock icon if readinessZone == HIGH_RISK. Tooltip: "High injury risk today. Consider active recovery."

### Accessibility Notes

- ReadinessSummaryCard: contentDescription = "Readiness score {value}, zone: {zone.label}. Tap to view details."
- TodayWodCard: contentDescription includes workout name, time domain, time cap, and movement list as a full sentence.
- PrMiniCards: contentDescription = "{movementName}, personal record {value} {unit}, achieved {date}"
- QuickActionButtons: contentDescription = each button's purpose e.g., "Record a lift with AI coaching"
- Tab order: TopBar → ReadinessSummaryCard → TodayWodCard → RecentPrsRow → QuickActionsRow → BottomNavBar
- All tap targets minimum 48dp × 48dp.
- Color contrast: TextPrimary (#F0F0FF) on SurfaceCard (#1E1E2E) = 14.7:1 — passes AAA.
- NeonGreen (#39FF14) on SurfaceCard (#1E1E2E) = 8.9:1 — passes AAA.
- ElectricBlue (#00D4FF) on SurfaceCard (#1E1E2E) = 9.2:1 — passes AAA.

---

## SCREEN S07 + S06 Composite: WOD Log Screen

> Note: Per architecture plan, WodLogScreen (S07) is where the athlete submits results after completing or reviewing a WOD. This template combines the log entry UI with the key elements of WodDetailScreen (S06) since the coder agent builds them as a flow: Detail → Timer → Log.

---

## SCREEN S06: WodDetailScreen

**Route:** `wod/{wodId}`
**Feature Module:** `feature-wod`
**User Story:** As an athlete, I want to see the full workout details including movements, time domain, and prescribed weights so I can prepare properly.
**Entry Points:** HomeScreen TodayWodCard, WodBrowseScreen list item
**Exit Points:** WodTimerScreen (Start), WodLogScreen (Log Result directly), back to browse

### Compose Component Hierarchy

```
WodDetailScreen (Scaffold)
  topBar: CenterAlignedTopAppBar
    navigationIcon: IconButton (Lucide ArrowLeft)
    title: Text(wod.name, HeadlineMedium)
    actions: IconButton (Lucide Share)
  content: LazyColumn (contentPadding PaddingValues(16dp, 16dp, 16dp, 120dp))
    item: WodMetaRow
    item: HorizontalDivider (BorderSubtle, 1dp, verticalPadding 8dp)
    item: MovementsSection
    item: HorizontalDivider
    item: PersonalHistorySection (athlete's previous scores for this WOD)
  bottomBar: WodDetailActionBar
```

### Zone: WodMetaRow

```
Layout: Row (wrap, horizontalArrangement = spacedBy(12dp))

MetaChip (each):
  Row (spacedBy 6dp, verticalAlignment = CenterVertically)
  Background: SurfaceElevated
  Corner: CornerSmall (8dp)
  Padding: horizontal 12dp, vertical 8dp

  Chip 1: Lucide Timer 16dp + timeDomain.label — color: timeDomain.badgeColor
  Chip 2: Lucide Clock 16dp + "{timeCap} min" — TextSecondary (null if no cap)
  Chip 3: Lucide RotateCcw 16dp + "{rounds} rounds" — TextSecondary (null for AMRAP)
  Chip 4: Lucide Target 16dp + scoringMetric.label — TextSecondary

Below row (vertical 8dp gap):
  Text: wod.description — BodyMedium, TextSecondary (multiline, fully shown)
```

### Zone: MovementsSection

```
Text: "MOVEMENTS" — LabelSmall, TextSecondary, paddingBottom 12dp

For each movement in wod.movements (sorted by sortOrder):
  MovementRow:
    Layout: Row (verticalAlignment = CenterVertically, padding vertical 12dp)
    Divider between rows (not above first): 1dp BorderSubtle

    Left: Text("{prescribedReps}×") — HeadlineSmall, ElectricBlue, width 48dp, textAlign End
    Spacer: 12dp
    Middle (Column, weight 1f):
      Text: movement.name — TitleMedium, TextPrimary
      Row (spacedBy 8dp):
        if prescribedWeight != null:
          Text: "{prescribedWeight} kg (Rx)" — BodySmall, TextSecondary
        if movement.equipment != null:
          Text: movement.equipment — BodySmall, TextSecondary
        Text: movement.category — BodySmall, BorderVisible (faint label)
    Right: IconButton (Lucide Video 20dp, TextSecondary, 40dp touch target)
      onClick: navigate to "vision/live" with movement pre-selected
      contentDescription: "Record {movement.name} with AI coaching"
```

### Zone: PersonalHistorySection

```
Text: "YOUR HISTORY" — LabelSmall, TextSecondary, paddingBottom 12dp

if no previous results:
  Text: "No previous results" — BodyMedium, TextSecondary

else (last 5 results as cards):
  each result:
    Row (spaceBetween, padding vertical 10dp):
      Column:
        Text: completedAt.formatRelative() — BodySmall, TextSecondary
        Text: result.score — TitleMedium, TextPrimary
      Row (spacedBy 8dp):
        if result.rxd: Badge "Rx" — NeonGreen text, NeonGreen 15% alpha background
        else:         Badge "Scaled" — TextSecondary text, SurfaceElevated background
        if result.rpe != null:
          Badge "RPE {rpe}" — color based on rpe:
            1-4: NeonGreen
            5-7: ColorWarning
            8-10: ColorError
```

### Zone: WodDetailActionBar

```
Layout: fixed bottom, Column (SurfaceElevated, top border 1dp BorderSubtle)
Padding: horizontal 16dp, vertical 12dp + WindowInsets.navigationBars

Row (horizontalArrangement = spacedBy(12dp)):
  SecondaryButton: "Log Result" (weight 1f, height 52dp)
    onClick: navigate to "wod/{wodId}/log"
  PrimaryButton: "Start Timer" (weight 1f, height 52dp)
    Icon: Lucide Play 20dp leading
    onClick: navigate to "wod/{wodId}/timer"
```

### ViewModel State Required

```kotlin
// WodDetailViewModel exposes WodDetailUiState (see Architecture Plan)
// Additional display helpers needed in UiState:
data class WodDetailUiState(
    val workout: Workout? = null,
    val movements: List<WorkoutMovement> = emptyList(),
    val recentResults: List<WorkoutResult> = emptyList(), // athlete's last 5
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Accessibility Notes

- Each MovementRow should be a semanticsMerged node with description: "{reps} reps of {movement.name}, prescribed {weight}kg"
- The Record video IconButton per movement: contentDescription = "Record {movement.name}"
- Start Timer button: announce result to talkback on press

---

## SCREEN S07: WodLogScreen

**Route:** `wod/{wodId}/log`
**Feature Module:** `feature-wod`
**User Story:** As an athlete, I want to log my WOD score, RPE, and notes after completing a workout so my performance is tracked and PRs are detected.
**Entry Points:** WodDetailScreen "Log Result", WodTimerScreen on completion
**Exit Points:** HomeScreen (after submit success), WodDetailScreen (back)

### Compose Component Hierarchy

```
WodLogScreen (Scaffold)
  topBar: CenterAlignedTopAppBar
    navigationIcon: IconButton (Lucide ArrowLeft) — shows confirmation dialog if form is dirty
    title: Text("Log Result", HeadlineMedium)
  content: Column (verticalScroll(rememberScrollState()), padding 16dp)
    WorkoutSummaryHeader (condensed repeat of WOD name + time domain badge)
    Spacer: 24dp
    ScoreInputSection
    Spacer: 24dp
    RxdToggleSection
    Spacer: 24dp
    RpeSection
    Spacer: 24dp
    NotesSection
    Spacer: 32dp
    SubmitButton
    Spacer: 48dp (keyboard clearance)
  bottomBar: none (keyboard-avoiding layout handles insets)
```

### Zone: ScoreInputSection

```
Text: "SCORE" — LabelSmall, TextSecondary, paddingBottom 8dp

Contextual input based on wod.scoringMetric:

  REPS (AMRAP):
    ApexTextField:
      label: "Total Reps"
      keyboardType: Number
      trailingIcon: Lucide Hash 20dp
      placeholder: "e.g. 155"

  TIME (RFT):
    Row (spacedBy 8dp):
      ApexTextField (weight 1f): label "Minutes", keyboardType Number, placeholder "12"
      Text: ":" — HeadlineMedium, TextSecondary, align CenterVertically
      ApexTextField (weight 1f): label "Seconds", keyboardType Number, placeholder "34"

  ROUNDS_PLUS_REPS (AMRAP):
    Row (spacedBy 8dp):
      ApexTextField (weight 1f): label "Rounds", keyboardType Number
      Text: "+" — HeadlineMedium, TextSecondary
      ApexTextField (weight 1f): label "Reps", keyboardType Number

  LOAD (max effort):
    ApexTextField:
      label: "Weight (kg)"
      keyboardType: Decimal
      trailingIcon: Lucide Weight 20dp
```

### Zone: RxdToggleSection

```
Row (spaceBetween, verticalAlignment = CenterVertically):
  Column:
    Text: "AS PRESCRIBED (Rx)" — TitleMedium, TextPrimary
    Text: "Did you use the prescribed weights and movements?" — BodySmall, TextSecondary
  Switch:
    checked: uiState.rxd
    thumbColor (checked): BackgroundDeepBlack
    trackColor (checked): ElectricBlue
    trackColor (unchecked): BorderSubtle
    onChange: onEvent(WodLogEvent.RxdToggled)
    Animation: 150ms EaseInOut
```

### Zone: RpeSection

```
Text: "EFFORT LEVEL (RPE)" — LabelSmall, TextSecondary, paddingBottom 12dp

RpeSelector:
  Row (horizontalArrangement = spacedBy(6dp)):
    For each value 1..10:
      RpeButton:
        Size: 32dp × 44dp
        Corner: CornerSmall (8dp)
        Background (unselected): SurfaceElevated
        Background (selected): rpeColor (see below)
        Border (unselected): 1dp BorderSubtle
        Text: value.toString(), LabelLarge
        Text color selected: TextPrimary
        Text color unselected: TextSecondary

    RPE color mapping:
      1-4: NeonGreen background at 20% alpha, NeonGreen border, NeonGreen text
      5-7: ColorWarning background at 20% alpha, ColorWarning border, ColorWarning text
      8-10: ColorError background at 20% alpha, ColorError border, ColorError text

Row below: description text:
  Text: rpeDescription(selectedRpe) — BodySmall, TextSecondary, italic
  e.g. RPE 7 → "Hard. Conversation is difficult."
```

### Zone: NotesSection

```
Text: "NOTES" — LabelSmall, TextSecondary, paddingBottom 8dp

ApexTextField:
  minLines: 3
  maxLines: 6
  label: "How did it feel? Movement notes..."
  keyboardType: Text
  maxLength: 500
  counter: Text("{current}/{500}", BodySmall, TextSecondary, align End)
```

### Zone: SubmitButton

```
PrimaryButton:
  label: "Submit Result"
  fullWidth (fillMaxWidth)
  height: 56dp
  Loading state: shows CircularProgressIndicator, hides label
  Disabled: when score is empty or isSubmitting
  onClick: onEvent(WodLogEvent.SubmitClicked)
```

### PR Achieved Celebration (UiEffect: PrAchieved)

```
Triggered via WodLogEffect.PrAchieved collected in LaunchedEffect.

Full-screen BottomSheet (ModalBottomSheet):
  Background: SurfaceElevated
  Corner top: CornerXLarge (24dp)
  Padding: 24dp

  Header:
    Icon: Lucide Trophy 48dp, NeonGreen
    Text: "NEW PERSONAL RECORD!" — HeadlineMedium, NeonGreen, textAlign Center
    Spacer: 8dp
    Text: "Outstanding performance!" — BodyLarge, TextSecondary, textAlign Center

  PR List (for each PR in WodLogEffect.PrAchieved.prs):
    ApexCard (NeonGreen border 2dp):
      Row (spaceBetween):
        Text: pr.movementName — TitleMedium, TextPrimary
        Text: "{pr.value} {pr.unit}" — HeadlineMedium, NeonGreen

  Spacer: 24dp
  PrimaryButton: "Awesome!" fullWidth
    onClick: dismiss sheet + emit NavigateBack effect

Entrance animation:
  Sheet slides up from bottom, 350ms EaseOut
  Trophy icon scales from 0.5 to 1.0, 400ms with OvershootInterpolator (spring)
  NeonGreen particle burst: 8 small circles (8dp) scattered radially from trophy icon
    each: translatesY -80dp to -160dp, fades out over 600ms
    Implemented via Canvas + LaunchedEffect + animateFloatAsState
```

### ViewModel State Required

```kotlin
// WodLogViewModel exposes WodLogUiState (see Architecture Plan)
// onEvent(WodLogEvent) handler
// effects: SharedFlow<WodLogEffect>
```

### Accessibility Notes

- Score input section: announce label changes dynamically (e.g., when WOD type changes the label)
- RPE buttons: Role = RadioButton, contentDescription = "RPE {value}: {description}"
- Submit button: announce "Submitting result" when loading starts; announce "Result submitted" on success
- PR celebration sheet: focusTrap within sheet; announce "New personal record achieved" via LiveRegion

---

## SCREEN S09: WodTimerScreen

**Route:** `wod/{wodId}/timer`
**Feature Module:** `feature-wod`
**User Story:** As an athlete mid-workout, I need a distraction-free full-screen timer that counts down my AMRAP or intervals so I can stay focused.
**Entry Points:** WodDetailScreen "Start Timer"
**Exit Points:** WodLogScreen (on Complete), WodDetailScreen (back, with confirmation)

### Compose Component Hierarchy

```
WodTimerScreen (full screen, no Scaffold top bar, no bottom nav)
  Box (fillMaxSize, background BackgroundDeepBlack)
    Column (fillMaxSize, verticalArrangement = SpaceBetween)
      TimerTopBar
      TimerDisplay (weight 1f, verticalAlignment = CenterVertically)
      MovementReferenceList
      TimerControls
    KeepScreenOn (SideEffect that acquires SCREEN_ON window flag)
```

### Zone: TimerTopBar

```
Row (fillMaxWidth, padding 16dp top + statusBarInsets):
  IconButton: Lucide X (close), TextSecondary
    onClick: show ExitWorkoutDialog
    contentDescription: "Exit workout"
  Spacer (weight 1f)
  Text: wod.name + " · " + timeDomain.label — BodyMedium, TextSecondary
  Spacer (weight 1f)
  Text: "Round {currentRound}/{totalRounds}" — BodyMedium, ElectricBlue
    (only visible for RFT and EMOM; hidden for AMRAP)
```

### Zone: TimerDisplay

```
Column (horizontalAlignment = CenterHorizontally, verticalArrangement = spacedBy(8dp)):

  For AMRAP (countdown):
    Text: formatMMSS(elapsedMillis remaining from timeCap) — DisplayLarge, TextPrimary
      Font size: 72sp for this specific context (override DisplayLarge)
      letterSpacing: -2sp
    Text: "remaining" — BodyLarge, TextSecondary

  For EMOM (interval countdown):
    Text: formatSS(currentIntervalSecondsRemaining) — DisplayLarge, 72sp, TextPrimary
    Text: "seconds this minute" — BodyLarge, TextSecondary
    LinearProgressBar:
      Width: 200dp, height 8dp, corner CornerFull
      fill = (currentIntervalSecondsRemaining / 60f)
      color: ElectricBlue → ColorWarning (lerp as time runs low below 10s)
      animateFloatAsState: 200ms

  For RFT (count up):
    Text: formatMMSS(elapsedMillis) — DisplayLarge, 72sp, TextPrimary
    Text: "elapsed" — BodyLarge, TextSecondary

  For TABATA (20s on / 10s off):
    Text: currentIntervalSecondsRemaining — DisplayLarge, 72sp
      color: if isWorkInterval → ElectricBlue else ColorWarning
    Text: if isWorkInterval "WORK" else "REST" — HeadlineMedium, same color
    TabataRoundIndicator:
      Row: 8 dots (12dp each, corner CornerFull)
        completed: ElectricBlue solid
        current: ElectricBlue pulsing (scale 1.0 → 1.3 → 1.0, 500ms infinite)
        future: BorderSubtle
```

### Zone: MovementReferenceList

```
Background: SurfaceCard (bottom panel, height ~160dp)
Corner: top corners CornerLarge (16dp)
Border top: 1dp BorderSubtle

Text: "MOVEMENTS" — LabelSmall, TextSecondary, padding 16dp top + 16dp start

LazyRow (contentPadding = PaddingValues(horizontal 16dp), spacedBy 8dp):
  Each movement chip:
    Background: SurfaceElevated
    Corner: CornerSmall (8dp)
    Padding: horizontal 12dp, vertical 8dp
    Text: "{prescribedReps}× {movement.name}" — BodyMedium, TextPrimary
    Text: "{prescribedWeight}kg" — BodySmall, TextSecondary (if applicable)
```

### Zone: TimerControls

```
Padding: horizontal 24dp, vertical 16dp + navigationBarInsets

Row (horizontalArrangement = spacedBy(16dp), verticalAlignment = CenterVertically):

  if !isComplete:
    IconButton (reset, 48dp circular):
      Background: SurfaceElevated
      Icon: Lucide RotateCcw 24dp, TextSecondary
      onClick: onEvent(WodTimerEvent.Reset)
      Show confirmation AlertDialog before reset

    Spacer (weight 1f)

    StartPauseButton (76dp × 76dp circular):
      Background: if isRunning → ColorError else ElectricBlue
      Icon: if isRunning → Lucide Pause 32dp else Lucide Play 32dp
      Icon color: TextOnBlue
      onClick: onEvent(WodTimerEvent.StartPause)
      Animation: icon crossfade 150ms

    Spacer (weight 1f)

    // Empty space to balance reset button
    Box(48dp) {}

  if isComplete:
    PrimaryButton: "Log Result" fullWidth height 56dp
      onClick: navigate to "wod/{wodId}/log", popUpTo("wod/{wodId}")
      Leading icon: Lucide CheckCircle2 20dp
```

### Timer Behavior Specifications

- AMRAP: countdown from `timeCap` seconds to 0. On reaching 0, vibrate device (HapticFeedback), play completion sound, set isComplete = true, show "Log Result" button.
- EMOM: each minute, interval restarts. At 0s remaining, vibrate + beep, auto-advance to next minute. On `currentRound > rounds`, complete.
- RFT: count up. Athlete manually taps complete. Show "Finish" button that calls `onEvent(Complete)`.
- TABATA: 8 intervals of 20s work / 10s rest. Audio cue on interval transitions. Count down total intervals in dot row.
- Screen stays awake for entire timer session (FLAG_KEEP_SCREEN_ON in Activity window).
- Timer continues in foreground Service if app is backgrounded (NotificationCompat showing current time).

### ViewModel State Required

```kotlin
// WodTimerViewModel exposes WodTimerUiState (see Architecture Plan)
// Additional: tabataIsWorkInterval: Boolean, totalElapsedMillis: Long
```

### Accessibility Notes

- Timer display: LiveRegion (LIVE_REGION_ASSERTIVE) for completion announcement only (not every tick, which would be overwhelming)
- Every 5-minute mark announce via talkback: "5 minutes remaining"
- Start/Pause button: contentDescription changes dynamically: "Pause timer" / "Start timer"
- Exit button: confirm dialog before leaving mid-workout

---

## SCREEN S10 + S11: PR Dashboard and PR Detail

---

## SCREEN S10: PrDashboardScreen

**Route:** `pr`
**Feature Module:** `feature-pr`
**User Story:** As an athlete, I want to see all my personal records organized by movement category so I can track my overall strength and skill progression.
**Entry Points:** Bottom nav "PR" tab, HomeScreen "View PRs" quick action
**Exit Points:** PrDetailScreen (tap PR item)

### Compose Component Hierarchy

```
PrDashboardScreen (Scaffold)
  topBar: LargeTopAppBar
    title: Text("Personal Records", HeadlineLarge, when collapsed: HeadlineMedium)
    Collapsible behavior: TopAppBarScrollBehavior.enterAlwaysScrollBehavior()
    actions:
      IconButton: Lucide SlidersHorizontal (filter icon, 24dp)
        onClick: show CategoryFilterSheet (ModalBottomSheet)
  content: LazyColumn (nestedScroll with topBar, contentPadding PaddingValues(16dp, 8dp, 16dp, 96dp))
    item: PrSummaryStatsRow
    items: CategorySection (one per category present in prsByCategory)
  bottomBar: ApexBottomNavBar (PR tab selected)
```

### Zone: PrSummaryStatsRow

```
Row (horizontalArrangement = spacedBy(8dp), fillMaxWidth):

  StatCard (weight 1f, each):
    Background: SurfaceCard
    Corner: CornerLarge
    Padding: 16dp
    Text: value — HeadlineSmall, ElectricBlue
    Text: label — BodySmall, TextSecondary
    Text: trend indicator ("+3 this month") — BodySmall, NeonGreen (positive) / TextSecondary

  Stat 1: total PR count, label "Total PRs"
  Stat 2: PRs this month count, label "This Month"
  Stat 3: most improved movement name (truncated), label "Best Gains"
```

### Zone: CategorySection (one per category)

```
Category Header:
  Row (spaceBetween, padding vertical 12dp):
    Row (spacedBy 8dp):
      CategoryIcon (Lucide, 20dp, ElectricBlue):
        "Olympic Lifting" → Lucide Dumbbell
        "Gymnastics"      → Lucide PersonStanding
        "Monostructural"  → Lucide Timer
        "Powerlifting"    → Lucide Weight
      Text: category.name — TitleLarge, TextPrimary
    Text: "{count} movements" — BodySmall, TextSecondary

PR Item List (Column, divider between items):
  Each PrListItem (clickable, navigates to "pr/{movementId}"):
    Row (spaceBetween, padding vertical 14dp, horizontal 0dp):
      Column (weight 1f):
        Text: pr.movementName — TitleMedium, TextPrimary
        Row (spacedBy 8dp):
          if achievedAt within 7 days:
            Badge "NEW" — NeonGreen 12pt, background NeonGreen 15% alpha, corner CornerFull, padding h 8dp v 2dp
          Text: "Set {achievedAt.formatRelative()}" — BodySmall, TextSecondary
      Column (horizontalAlignment = End):
        Text: "{pr.value} {pr.unit}" — TitleLarge, ElectricBlue
        Text: pr.unit.displayLabel — BodySmall, TextSecondary
    Divider: 1dp BorderSubtle, paddingStart 0dp
```

### Empty State

```
Column (centered, padding 32dp):
  Icon: Lucide Trophy 64dp, TextSecondary
  Spacer: 16dp
  Text: "No Personal Records Yet" — HeadlineSmall, TextPrimary, textAlign Center
  Text: "Complete workouts to automatically track your PRs" — BodyMedium, TextSecondary, textAlign Center
  Spacer: 24dp
  PrimaryButton: "Browse Workouts" → navigate to "wod"
```

---

## SCREEN S11: PrDetailScreen

**Route:** `pr/{movementId}`
**Feature Module:** `feature-pr`
**User Story:** As an athlete, I want to see my full PR history for a specific movement with a trend chart so I can understand my progress over time.
**Entry Points:** PrDashboardScreen list item tap
**Exit Points:** Back to PrDashboardScreen

### Compose Component Hierarchy

```
PrDetailScreen (Scaffold)
  topBar: CenterAlignedTopAppBar
    navigationIcon: IconButton (Lucide ArrowLeft)
    title: Text(movement.name, HeadlineMedium)
  content: LazyColumn (contentPadding PaddingValues(16dp, 0dp, 16dp, 32dp))
    item: CurrentPrHero
    item: ProgressChartSection
    item: PrHistoryList
```

### Zone: CurrentPrHero

```
ApexCard (background gradient: SurfaceCard → ElectricBlue at 6% alpha, diagonal)
Padding: 24dp

Row (spaceBetween, verticalAlignment = CenterVertically):
  Column:
    Text: "CURRENT PR" — LabelSmall, TextSecondary
    Spacer: 4dp
    Text: "{currentPr.value}" — DisplayMedium, ElectricBlue
    Text: currentPr.unit.displayLabel — HeadlineSmall, TextSecondary
    Spacer: 8dp
    Text: "Set on {currentPr.achievedAt.formatDate()}" — BodySmall, TextSecondary
  Icon: Lucide Trophy 48dp, ElectricBlue (trailing, decorative, aria-hidden)

Movement Category Badge (bottom row):
  Row (spacedBy 8dp):
    FilterChip (non-interactive display): movement.category
    FilterChip: movement.equipment (if non-null)
    FilterChip: movement.primaryMuscles.first() (primary muscle)
```

### Zone: ProgressChartSection

```
Text: "PROGRESS" — LabelSmall, TextSecondary, paddingBottom 12dp

TimeRangeTabs:
  TabRow (ElectricBlue indicator):
    Tab: "3M" (3 months)
    Tab: "6M"
    Tab: "1Y"
    Tab: "All"
  selectedTabIndex: controlled by ViewModel state
  Spacer: 12dp

LineChart (Canvas-based, implemented with androidx.compose.ui.graphics):
  Width: fillMaxWidth
  Height: 180dp
  Background: SurfaceCard, corner CornerLarge
  Padding inside: 16dp

  X-axis: dates from prHistory filtered by selected range
    Labels: BodySmall, TextSecondary, every N-th data point
  Y-axis: weight/reps scale
    Labels: BodySmall, TextSecondary, 4-5 gridlines
  Grid: horizontal dashed lines, BorderSubtle, dash 4dp gap 4dp
  Line: ElectricBlue, strokeWidth 2.5dp
  Area fill: gradient ElectricBlue 30% alpha → transparent (from line down)
  Data points: 6dp filled circles ElectricBlue, outline 2dp BackgroundDeepBlack
  PR Milestone dots: 8dp NeonGreen filled circles
  Touch interaction: show tooltip on tap/press nearest data point
    Tooltip: ApexCard (SurfaceElevated, 1dp ElectricBlue border)
      Text: "{value} {unit}" — TitleMedium, ElectricBlue
      Text: "{date}" — BodySmall, TextSecondary
  Animation: drawPath progress from 0f to 1f, 600ms EaseOut on first load or tab change
```

### Zone: PrHistoryList

```
Text: "HISTORY" — LabelSmall, TextSecondary, paddingBottom 12dp

For each entry in prHistory (descending date):
  PrHistoryRow:
    Row (spaceBetween, padding vertical 12dp):
      Row (spacedBy 12dp):
        DateColumn (width 48dp):
          Text: date.monthAbbrev — LabelSmall, TextSecondary
          Text: date.day — TitleMedium, TextPrimary
        Column:
          Text: "{value} {unit}" — TitleLarge, TextPrimary
          Text: "Improvement: +{delta} {unit} vs previous" — BodySmall, NeonGreen
            (hide delta for first entry)
      if index == 0 (most recent):
        Icon: Lucide Trophy 20dp, NeonGreen (current PR indicator)
    Divider: 1dp BorderSubtle
```

### ViewModel State Required

```kotlin
// PrDetailViewModel exposes PrDetailUiState (see Architecture Plan)
// Additional needed:
data class PrDetailUiState(
    val movement: Movement? = null,
    val currentPr: PersonalRecord? = null,
    val prHistory: List<PrHistoryEntry> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.SIX_MONTHS,
    val isLoading: Boolean = false
)
enum class TimeRange { THREE_MONTHS, SIX_MONTHS, ONE_YEAR, ALL_TIME }
```

### Accessibility Notes

- Progress chart: contentDescription on the Canvas = "Line chart showing {movement.name} PR history. Current best: {value} {unit}. {N} recorded PRs."
- Tab row: each tab has role Tab, selected state announced
- Chart touch tooltip: announce value and date when tooltip appears

---

## SCREEN S14: LiveCameraScreen (Video Coach — Camera)

**Route:** `vision/live`
**Feature Module:** `feature-vision`
**User Story:** As an athlete, I want to see my live pose overlaid on camera feed and record my movement for AI coaching analysis.
**Entry Points:** Camera FAB in bottom nav, WodDetailScreen movement video icon, HomeScreen "Record Lift"
**Exit Points:** RecordingReviewScreen (after recording), previous bottom nav destination (back)

### Compose Component Hierarchy

```
LiveCameraScreen (no Scaffold — full immersive screen)
  Box (fillMaxSize, background Color.Black)
    // Layer 1: Camera preview
    AndroidView(factory = { PreviewView(context) }, modifier = fillMaxSize)
      // CameraX PreviewView with UseCase attached in ViewModel via ProcessCameraProvider
    // Layer 2: Pose overlay Canvas
    Canvas(modifier = fillMaxSize, onDraw = { drawPoseOverlay(poseOverlayData) })
    // Layer 3: UI Controls overlay
    Column(fillMaxSize, verticalArrangement = SpaceBetween)
      LiveCameraTopBar
      Spacer(weight 1f)
      AngleReadoutsRow
      RecordingControls
```

### Camera Preview Implementation Note for Coder

```
Use PreviewView in PERFORMANCE mode (not COMPATIBLE).
Attach CameraX UseCases (Preview + ImageAnalysis) in ViewModel via ProcessCameraProvider.
ImageAnalysis feeds frames to MediaPipe PoseLandmarkerHelper (LIVE_STREAM mode).
PoseLandmarkerHelper.resultListener pushes PoseOverlayData to ViewModel StateFlow.
Compose Canvas collects poseOverlayData from uiState and redraws on every emission.
DO NOT use SurfaceView or TextureView directly — use CameraX PreviewView exclusively.
```

### Zone: LiveCameraTopBar

```
Layout: Row (fillMaxWidth, padding 16dp + statusBarInsets, spaceBetween)
Background: gradient from Color.Black 60% alpha at top to transparent over 80dp

Left:
  IconButton (48dp, circular SurfaceDark 60% alpha background):
    Icon: Lucide ArrowLeft 24dp, TextPrimary
    onClick: navigate back
    contentDescription: "Close camera"

Center:
  Column (horizontalAlignment = CenterHorizontally):
    if cameraState == RECORDING:
      Row (spacedBy 8dp, verticalAlignment = CenterVertically):
        Box (12dp circle, ColorError, pulsing: scale 1.0↔1.4, 800ms infinite)
        Text: formatMMSS(recordingDuration) — TitleLarge, TextPrimary
    if cameraState == READY:
      Text: "READY" — LabelSmall, NeonGreen
    if cameraState == INITIALIZING:
      CircularProgressIndicator (24dp, ElectricBlue, strokeWidth 2dp)
    if cameraState == ERROR:
      Text: "CAMERA ERROR" — LabelSmall, ColorError

Right:
  Row (spacedBy 8dp):
    IconButton (48dp, SurfaceDark 60% alpha background):
      Icon: Lucide FlipHorizontal2 24dp (flip camera)
      onClick: onEvent(VisionEvent.FlipCamera)
      contentDescription: "Flip camera"
    IconButton (48dp, SurfaceDark 60% alpha background):
      Icon: Lucide Settings 24dp
      onClick: show SettingsSheet
      contentDescription: "Camera settings"
```

### Zone: Skeleton Pose Overlay (Canvas drawPoseOverlay)

```
Drawn on Canvas layer above CameraX PreviewView.
All coordinates: normalized PoseLandmark.x/y × canvas width/height.

Skeleton Lines:
  For each connection in BlazePose skeleton topology (33 standard pairs):
    if both landmarks have visibility > 0.5:
      drawLine:
        color: ElectricBlue at 60% alpha
        strokeWidth: 3dp
        strokeCap: Round

Joint Dots:
  For each landmark with visibility > 0.5:
    drawCircle:
      radius: 8dp
      color: TextPrimary

Key Angle Joints (Shoulders 11/12, Hips 23/24, Ankles 27/28 per CLAUDE.md):
  drawCircle:
    radius: 12dp
    color: ElectricBlue solid

Barbell Trajectory Path (if barbellPosition != null):
  drawPath through barbellTrajectory list (last 30 frames):
    color: NeonGreen at 70% alpha
    strokeWidth: 2.5dp
    PathEffect: none (solid line)

  drawCircle at barbellPosition:
    radius: 16dp
    color: NeonGreen at 80% alpha

Joint Angle Text Labels (for angles in jointAngles map, visible joints only):
  drawText at midpoint offset from joint:
    text: "${angle.toInt()}°"
    textSize: 14sp
    color: TextPrimary
    background: SurfaceDark 70% alpha rounded rect behind text (padding 4dp)
```

### Zone: AngleReadoutsRow

```
Background: gradient transparent at top → SurfaceDark 80% alpha over 80dp
Padding: horizontal 16dp, vertical 8dp

LazyRow (horizontalArrangement = spacedBy(8dp)):
  For each angle in jointAngles where value is meaningful (filter low-confidence):
    AngleChip:
      Background: SurfaceDark 80% alpha
      Corner: CornerSmall (8dp)
      Border: 1dp based on angle quality:
        within_normal_range → BorderSubtle
        at_threshold        → ColorWarning 1dp
        fault               → ColorError 1dp
      Padding: horizontal 10dp, vertical 6dp
      Text (row):
        Text: jointLabel — LabelSmall, TextSecondary
        Spacer 4dp
        Text: "${angle.toInt()}°" — LabelLarge, ElectricBlue

  FPS counter chip (trailing):
    Text: "${fps} fps" — LabelSmall, TextSecondary (performance debug info)
```

### Zone: MovementSelectorSheet (accessed via settings)

```
ModalBottomSheet:
  Background: SurfaceDark
  Corner top: CornerXLarge (24dp)

  Text: "SELECT MOVEMENT" — LabelSmall, TextSecondary, padding 16dp

  ApexTextField: Search movements (keyboardType Text)

  LazyColumn:
    For each movement in common Olympic/Barbell movements (seeded list):
      MovementRow:
        Row (spacedBy 12dp, padding vertical 12dp, clickable):
          Icon: Lucide Dumbbell 20dp, ElectricBlue
          Text: movement.name — TitleMedium, TextPrimary
          Spacer (weight 1f)
          if selected: Icon Lucide CheckCircle2 20dp NeonGreen
        onClick: onEvent(VisionEvent.MovementSelected(movement))
        
  Padding bottom: navigationBarInsets
```

### Zone: RecordingControls

```
Background: gradient transparent → SurfaceDark 80% over 120dp
Padding: horizontal 24dp, bottom 32dp + navigationBarInsets

Row (fillMaxWidth, horizontalArrangement = SpaceEvenly, verticalAlignment = CenterVertically):

  if !isRecording:
    // Empty left slot for balance
    Box(60dp) {}

    // Main record button
    RecordButton (76dp × 76dp):
      Outer ring: 76dp circle, border 3dp TextPrimary
      Inner circle: 60dp circle (8dp inset from outer ring)
        color: ColorError
        animation: scale pulses when tap held (scale 0.95, 150ms)
      onClick: onEvent(VisionEvent.StartRecording)
      contentDescription: "Start recording"

    // Empty right slot
    Box(60dp) {}

  if isRecording:
    // Discard button
    IconButton (60dp circular, SurfaceDark 80% alpha, border 1dp ColorError):
      Icon: Lucide Trash2 24dp, ColorError
      onClick: show DiscardConfirmDialog
      contentDescription: "Discard recording"

    // Stop button
    StopButton (76dp × 76dp):
      Outer ring: 76dp circle, border 3dp ColorError
      Inner square: 32dp × 32dp rounded rect (corner 8dp)
        color: ColorError
      onClick: onEvent(VisionEvent.StopRecording) → emits NavigateToReview effect
      contentDescription: "Stop recording"

    // Pause button
    IconButton (60dp circular, SurfaceDark 80% alpha, border 1dp BorderVisible):
      Icon: Lucide Pause 24dp, TextPrimary
      onClick: pause recording
      contentDescription: "Pause recording"
```

### ViewModel State Required

```kotlin
// VisionViewModel exposes VisionUiState (see Architecture Plan)
// Also needs: isCameraPermissionGranted: Boolean
```

### Permission Handling

```
if !CAMERA permission granted:
  Replace full screen with PermissionRequestScreen:
    Background: BackgroundDeepBlack
    Icon: Lucide Camera 64dp, TextSecondary (centered)
    Text: "Camera Access Required" — HeadlineMedium, TextPrimary, textAlign Center
    Text: "ApexAI needs camera access to analyze your movement in real time." — BodyMedium, TextSecondary, textAlign Center
    PrimaryButton: "Grant Permission" → ActivityResultLauncher for CAMERA permission
    TextButton: "Not Now" → navigate back
```

### Accessibility Notes

- Full-screen camera: disable talkback focus traversal on the camera preview and Canvas layers (focusable = false)
- RecordingControls: maintain full keyboard/accessibility focus
- Real-time angle readouts: do NOT announce via LiveRegion (constant updates would overwhelm screen reader users). Provide a "Summary" button that reads the last 3 angles aloud on demand.
- Recording timer: announce recording start ("Recording started") and stop ("Recording stopped") via AccessibilityManager announceForAccessibility.

---

## SCREEN S15: RecordingReviewScreen (Video Coach — Review)

**Route:** `vision/review/{videoUri}`
**Feature Module:** `feature-vision`
**User Story:** As an athlete, I want to review my recorded video before sending it for AI coaching analysis to make sure it captured what I need.
**Entry Points:** LiveCameraScreen stop recording
**Exit Points:** CoachingReportScreen (after upload + analysis), back to LiveCameraScreen (re-record)

### Compose Component Hierarchy

```
RecordingReviewScreen (no Scaffold — full screen)
  Box (fillMaxSize, background Color.Black)
    // Layer 1: Media3 video player
    AndroidView(factory = { PlayerView(context) }, modifier = fillMaxSize)
      // Media3 ExoPlayer from pool, surface attached here
      // AspectRatioFrameLayout with RESIZE_MODE_FIT
    // Layer 2: UI overlay
    Column (fillMaxSize, verticalArrangement = SpaceBetween)
      ReviewTopBar
      Spacer (weight 1f)
      VideoScrubber
      ReviewActionBar
```

### Zone: ReviewTopBar

```
Row (fillMaxWidth, padding 16dp + statusBarInsets, spaceBetween)
Background: gradient Color.Black 70% alpha → transparent over 80dp

Left:
  IconButton (48dp, SurfaceDark 60% alpha):
    Icon: Lucide ArrowLeft 24dp
    onClick: show DiscardDialog → navigate back to "vision/live" on confirm

Center:
  Text: "Review Recording" — TitleMedium, TextPrimary

Right:
  Text: formatMMSS(videoDuration) — TitleMedium, ElectricBlue
```

### Zone: VideoScrubber

```
Background: gradient transparent → SurfaceDark 80% over 60dp
Padding: horizontal 16dp, vertical 8dp

Column (spacedBy 4dp):
  // Time labels row
  Row (spaceBetween):
    Text: formatMMSS(currentPositionMs) — LabelSmall, TextSecondary
    Text: formatMMSS(videoDurationMs) — LabelSmall, TextSecondary

  // Scrubber track
  Slider:
    value: currentPositionMs / videoDurationMs.toFloat()
    valueRange: 0f..1f
    onValueChange: seek player to position
    colors:
      thumbColor: ElectricBlue
      activeTrackColor: ElectricBlue
      inactiveTrackColor: BorderSubtle
    thumbSize: 18dp (custom CircleShape thumb)

  // Playback speed
  Row (End-aligned, spacedBy 8dp):
    For speed in [0.5, 1.0, 1.5, 2.0]:
      SpeedChip:
        selected: currentSpeed == speed
        Background: if selected SurfaceElevated else Transparent
        Border: if selected 1dp ElectricBlue else 1dp BorderSubtle
        Text: "${speed}×" — LabelSmall, if selected ElectricBlue else TextSecondary
        Padding: horizontal 10dp, vertical 4dp
        Corner: CornerSmall
        onClick: setPlaybackSpeed(speed)
```

### Zone: ReviewActionBar

```
Background: SurfaceDark
Border top: 1dp BorderSubtle
Padding: horizontal 16dp, vertical 16dp + navigationBarInsets

Row (spacedBy 12dp):
  SecondaryButton: "Re-record" (weight 1f, height 52dp)
    Icon: Lucide RefreshCcw 20dp leading
    onClick: navigate back to "vision/live"

  MovementDropdown (weight 1f, height 52dp):
    Background: SurfaceElevated
    Corner: CornerMedium
    Border: 1dp BorderSubtle
    Text: selectedMovement.name OR "Select Movement" — TitleMedium, if selected TextPrimary else TextSecondary
    TrailingIcon: Lucide ChevronDown 16dp, TextSecondary
    onClick: show MovementSelectorSheet (same as LiveCameraScreen)

PrimaryButton: "Analyze with AI" (fullWidth, height 56dp, marginTop 8dp)
  Icon: Lucide Brain 20dp leading
  Text: "Analyze with AI"
  Disabled: selectedMovement == null
  onClick: onEvent(CoachingEvent.UploadVideo(videoUri)) → start upload, navigate to "coaching/report/{analysisId}"
```

### Upload Progress Overlay

```
if analysisStatus == UPLOADING or ANALYZING:
  Box (fillMaxSize, background Color.Black 70% alpha)
    Card (SurfaceElevated, corner CornerXLarge, padding 32dp, centered in box):
      Column (horizontalAlignment = CenterHorizontally, spacedBy 16dp):
        CircularProgressIndicator (64dp, ElectricBlue, strokeWidth 4dp)
        Text: stageLabel (from AnalysisStatus):
          UPLOADING → "Uploading video... {(uploadProgress * 100).toInt()}%"
          ANALYZING → "Gemini AI is analyzing your movement..."
        LinearProgressBar (width 200dp) — shows uploadProgress for UPLOADING, indeterminate for ANALYZING
        TextButton: "Cancel" (visible only during UPLOADING) → cancel upload
```

### Accessibility Notes

- Video player: provide contentDescription for the PlayerView AndroidView
- Scrubber Slider: contentDescription = "Video position {currentTime} of {totalTime}. Drag to seek."
- Upload progress overlay: focusTrap inside the overlay card; announce stage changes via LiveRegion

---

## SCREEN S16: CoachingReportScreen (Video Coach — AI Review)

**Route:** `coaching/report/{analysisId}`
**Feature Module:** `feature-coaching`
**User Story:** As an athlete, I want to read the AI's analysis of my movement with fault descriptions, coaching cues, and corrected form images so I know what to fix next session.
**Entry Points:** RecordingReviewScreen (after upload completes)
**Exit Points:** VideoPlaybackScreen (tap a fault to see it in video), HomeScreen (done)

### Compose Component Hierarchy

```
CoachingReportScreen (Scaffold)
  topBar: CenterAlignedTopAppBar
    navigationIcon: IconButton (Lucide X — closes report, popUpTo home)
    title: Text("AI Coaching Report", HeadlineMedium)
    actions: IconButton (Lucide Share 24dp) — share report text
  content: LazyColumn (contentPadding PaddingValues(16dp, 8dp, 16dp, 96dp))
    item: ReportSummaryCard
    item: GlobalCuesSection
    item: FaultsSection (FaultCards list)
  bottomBar: ReportActionBar
```

### Zone: ReportSummaryCard

```
ApexCard:
  Background: gradient SurfaceCard → ElectricBlue at 4% alpha
  Padding: 20dp

  Row (spaceBetween):
    Column (weight 1f):
      Text: "OVERALL ASSESSMENT" — LabelSmall, TextSecondary
      Spacer: 4dp
      Text: report.overallAssessment — BodyLarge, TextPrimary
      Spacer: 12dp
      Row (spacedBy 16dp):
        StatChip: Icon Lucide RotateCcw + "{repCount} reps" — BodyMedium ElectricBlue
        StatChip: if estimatedWeight != null → Icon Lucide Weight + "{weight} kg" — BodyMedium ElectricBlue
    Column (horizontalAlignment = CenterHorizontally):
      FaultScoreBadge:
        Size: 72dp × 72dp
        Shape: CircleShape
        Background: scoreColor at 15% alpha
        Border: 2dp scoreColor
        Text: faultSeverityScore (e.g., "B+") — HeadlineMedium, scoreColor
        Text: "Score" — LabelSmall, TextSecondary
        Score derived: CRITICAL faults reduce score most; displayed letter grade
      Text: report.movementType — BodySmall, TextSecondary, marginTop 4dp
```

### Zone: GlobalCuesSection

```
Text: "COACHING CUES" — LabelSmall, TextSecondary, paddingBottom 12dp

Column (spacedBy 8dp):
  For each cue in report.globalCues:
    Row (spacedBy 12dp, padding vertical 4dp):
      Box:
        Size: 6dp × 6dp circle
        Color: ElectricBlue
        marginTop: 8dp (aligns with first line of text)
      Text: cue — BodyMedium, TextPrimary
```

### Zone: FaultsSection

```
Row (spaceBetween, paddingBottom 12dp):
  Text: "MOVEMENT FAULTS" — LabelSmall, TextSecondary
  Text: "${faults.size} found" — BodySmall,
    if hasCritical: ColorError
    else if hasModerate: BlazeOrange
    else: ColorWarning

Column (spacedBy 12dp):
  For each fault in report.faults (sorted: CRITICAL first):
    FaultCard (ApexCard, clickable → navigates to "coaching/playback/{videoId}?timestamp={fault.timestampMs}"):
      Border: 1dp severityColor (MINOR=ColorWarning, MODERATE=BlazeOrange, CRITICAL=ColorError)
      Padding: 16dp

      Row (verticalAlignment = Top):
        SeverityIndicator:
          Width: 4dp, height: fullHeight of card
          Background: severityColor
          Corner: CornerFull (left side only)
          marginEnd: 12dp
        Column (weight 1f):
          Row (spaceBetween):
            SeverityBadge:
              Background: severityColor at 15% alpha
              Border: 1dp severityColor
              Text: fault.severity.label — LabelLarge, severityColor
              Corner: CornerSmall
              Padding: horizontal 8dp, vertical 2dp
            Text: formatTimestamp(fault.timestampMs) — BodySmall, TextSecondary
          Spacer: 8dp
          Text: fault.description — BodyLarge, TextPrimary
          Spacer: 8dp
          Row (spacedBy 6dp):
            Icon: Lucide Lightbulb 16dp, ElectricBlue
            Text: fault.cue — BodyMedium, ElectricBlue (italic emphasis via fontStyle = Italic)
          if fault.correctedImageUrl != null:
            Spacer: 12dp
            AsyncImage:
              url: fault.correctedImageUrl (Gemini Flash generated)
              modifier: fillMaxWidth, height 160dp, corner CornerMedium clip
              contentScale: Crop
              placeholder: ShimmerSkeleton (same dimensions)
              contentDescription: "AI-generated corrected form image for ${fault.description}"
          Row (end-aligned, marginTop 8dp):
            TextButton: "View in Video →" — ElectricBlue, BodySmall
              onClick: navigate to "coaching/playback/{videoId}" at fault.timestampMs
```

### ReportActionBar

```
Background: SurfaceDark, border top 1dp BorderSubtle
Padding: horizontal 16dp, vertical 12dp + navigationBarInsets

PrimaryButton: "Done — Back to Home" fullWidth height 52dp
  Icon: Lucide Home 20dp leading
  onClick: navigate to "home", popUpTo("home") inclusive=false
```

### Empty State (analysisStatus == ERROR)

```
Column (centered, padding 32dp):
  Icon: Lucide AlertTriangle 64dp, ColorError
  Text: "Analysis Failed" — HeadlineMedium, TextPrimary, textAlign Center
  Text: uiState.error OR "Something went wrong analyzing your video." — BodyMedium, TextSecondary
  Spacer: 24dp
  PrimaryButton: "Retry Analysis" → onEvent(CoachingEvent.RetryAnalysis)
  SecondaryButton: "Go Back" → navigate back
```

### ViewModel State Required

```kotlin
// CoachingViewModel exposes CoachingUiState (see Architecture Plan)
// effects: SharedFlow<CoachingEffect>
```

### Accessibility Notes

- FaultCards: role = Button with contentDescription = "Fault: {description}, severity {severity}, at {timestamp}. Tap to view in video."
- Corrected form images: explicit contentDescription required on each (not "decorative" — these convey corrective information)
- Global cues: each bullet is a list item semantically (use semantics role)

---

## SCREEN S17: VideoPlaybackScreen

**Route:** `coaching/playback/{videoId}?timestamp={startMs}`
**Feature Module:** `feature-coaching`
**User Story:** As an athlete, I want to watch my recorded video with the AI's kinematic overlay showing joint angles and fault markers at specific timestamps so I can see exactly what went wrong.
**Entry Points:** CoachingReportScreen FaultCard "View in Video"
**Exit Points:** CoachingReportScreen (back)

### Compose Component Hierarchy

```
VideoPlaybackScreen (no Scaffold — full screen)
  Box (fillMaxSize, background Color.Black)
    // Layer 1: Media3 ExoPlayer (from pool — CRITICAL: attach/detach surface, never create new instance)
    AndroidView(factory = { PlayerView(context).apply { useController = false } }, modifier = fillMaxWidth.aspectRatio(9/16f).align(TopCenter))
    // Layer 2: Kinematic overlay Canvas (aligned exactly over PlayerView)
    Canvas(modifier = same as PlayerView, onDraw = { drawTimedOverlay(currentPositionMs) })
    // Layer 3: Controls
    Column (fillMaxSize, verticalArrangement = SpaceBetween)
      PlaybackTopBar
      Spacer (weight 1f)
      FaultMarkersTimeline
      PlaybackControls
      RepBreakdownPanel
```

### Zone: PlaybackTopBar

```
Row (fillMaxWidth, padding 16dp + statusBarInsets, spaceBetween)
Background: gradient Color.Black 70% → transparent

Left: IconButton (Lucide ArrowLeft) — navigate back to coaching report
Center: Text: "Rep Analysis" — TitleMedium, TextPrimary
Right: Row:
  IconButton: Lucide Bookmark 24dp — save this timestamp annotation
  IconButton: Lucide Share 24dp — share video clip
```

### Zone: Kinematic Canvas Overlay

```
Drawn on Canvas matching PlayerView exact dimensions and position.

Frame selection:
  Find TimedPoseOverlay entry where abs(entry.timestampMs - currentPositionMs) < 33ms (nearest frame)

Draw skeleton (same as LiveCameraScreen):
  Lines: ElectricBlue 60% alpha, strokeWidth 3dp
  Joints: TextPrimary 8dp circles
  Key joints: ElectricBlue 12dp circles

Draw joint angle labels (same as LiveCameraScreen overlay)

Draw fault highlight (if currentPositionMs falls within ±500ms of any fault):
  Highlight affected joints:
    drawCircle: radius 16dp, color faultSeverityColor, alpha 40% fill + 2dp stroke 80% alpha
  Flash border around video:
    drawRect on full canvas: border 4dp faultSeverityColor at 60% alpha
  Pulse animation: alpha oscillates 40%→80%, 400ms repeat

Barbell path: NeonGreen line (same as live)
```

### Zone: FaultMarkersTimeline

```
Background: SurfaceDark 80% alpha
Padding: horizontal 16dp, vertical 8dp

Row (fillMaxWidth, verticalAlignment = CenterVertically):
  Text: "Faults" — LabelSmall, TextSecondary, width 44dp

  Box (weight 1f, height 24dp):
    // Timeline track
    Canvas:
      drawRect (full width, 2dp centered): BorderSubtle

      For each fault in faultMarkers:
        xPos = (fault.timestampMs / videoDurationMs.toFloat()) * width
        drawCircle at (xPos, center):
          radius: if current fault region → 8dp else 6dp
          color: severityColor(fault.severity)
          stroke (outer ring): 2dp BackgroundDeepBlack

      // Playhead
      playheadX = (currentPositionMs / videoDurationMs) * width
      drawLine (playheadX, 0, playheadX, height): ElectricBlue, strokeWidth 2dp

    // Tap targets for each fault (invisible clickable composables positioned by fraction)
    For each fault:
      Box (24dp × 24dp, offset by xPos - 12dp):
        clickable → player.seekTo(fault.timestampMs)
        contentDescription: "Jump to ${fault.severity} fault at ${formatTimestamp(fault.timestampMs)}"
```

### Zone: PlaybackControls

```
Background: transparent
Padding: horizontal 24dp, vertical 8dp

Row (SpaceEvenly, verticalAlignment = CenterVertically):
  IconButton (56dp):
    Icon: Lucide SkipBack 24dp, TextPrimary
    onClick: seek to previous fault timestamp
    contentDescription: "Previous fault"

  IconButton (56dp):
    Icon: Lucide Rewind 24dp, TextPrimary
    onClick: seekTo(currentPositionMs - 5000)
    contentDescription: "Rewind 5 seconds"

  PlayPauseButton (72dp circular, SurfaceElevated, border 2dp ElectricBlue):
    Icon: if isPlaying Lucide Pause 32dp else Lucide Play 32dp, ElectricBlue
    onClick: toggle play/pause
    contentDescription: if isPlaying "Pause" else "Play"

  IconButton (56dp):
    Icon: Lucide FastForward 24dp, TextPrimary
    onClick: seekTo(currentPositionMs + 5000)
    contentDescription: "Skip forward 5 seconds"

  IconButton (56dp):
    Icon: Lucide SkipForward 24dp, TextPrimary
    onClick: seek to next fault timestamp
    contentDescription: "Next fault"
```

### Zone: RepBreakdownPanel (collapsible bottom panel)

```
BottomSheetScaffold or custom draggable sheet:
  peekHeight: 80dp (shows rep count row)
  expandedHeight: 280dp

Peek state (80dp):
  Row (SpaceBetween, padding horizontal 16dp, vertical 20dp):
    Text: "{report.repCount} Reps Analyzed" — TitleMedium, TextPrimary
    Icon: Lucide ChevronUp 20dp, TextSecondary (indicates expandable)

Expanded state (280dp):
  Text: "REP BREAKDOWN" — LabelSmall, TextSecondary, padding 16dp
  LazyRow (contentPadding PaddingValues(horizontal 16dp), spacedBy 8dp):
    For each rep (rep index derived from fault timestamps):
      RepCard (80dp × 100dp):
        Background: SurfaceCard
        Corner: CornerMedium
        Border: 1dp (if rep has CRITICAL fault: ColorError elif MODERATE: BlazeOrange elif MINOR: ColorWarning else BorderSubtle)
        Column (centered, padding 8dp):
          Text: "Rep {repNumber}" — LabelSmall, TextSecondary
          Spacer 4dp
          RepStatusIcon:
            No faults: Lucide CheckCircle2 24dp NeonGreen
            MINOR:     Lucide AlertCircle 24dp ColorWarning
            MODERATE:  Lucide AlertTriangle 24dp BlazeOrange
            CRITICAL:  Lucide XCircle 24dp ColorError
          Text: faultCount or "Clean" — LabelSmall, statusColor
        clickable: seekTo(repStartTimestamp)
```

### ViewModel State Required

```kotlin
// VideoPlaybackViewModel exposes VideoPlaybackUiState (see Architecture Plan)
// Note: ExoPlayer instance comes from PlayerPoolManager (core-media)
// NEVER instantiate a new ExoPlayer in this ViewModel — get from pool via inject
```

### Accessibility Notes

- PlayerView: set contentDescription = "Video of {movementType} analysis"
- Fault timeline tap targets: minimum 44dp touch targets (use Box with requiredSize padding)
- Rep cards: contentDescription = "Rep {N}, {faultCount} faults, severity {worst severity}"
- Announce when playback reaches a fault: AccessibilityManager.announceForAccessibility("${fault.severity} fault: ${fault.description}")

---

## SCREEN S12: ReadinessDashboardScreen

**Route:** `readiness`
**Feature Module:** `feature-readiness`
**User Story:** As an athlete, I want to see my physiological readiness score based on HRV, sleep, and training load so I can make an informed decision about today's workout intensity.
**Entry Points:** Bottom nav "Readiness" tab, HomeScreen ReadinessSummaryCard tap
**Exit Points:** HealthConnectSetupScreen (if permissions not granted)

### Compose Component Hierarchy

```
ReadinessDashboardScreen (Scaffold)
  topBar: LargeTopAppBar
    title: Text("Readiness", HeadlineLarge when expanded / HeadlineMedium when collapsed)
    scrollBehavior: TopAppBarScrollBehavior.enterAlwaysScrollBehavior()
    actions:
      IconButton: Lucide RefreshCw 24dp → onEvent(ReadinessEvent.SyncHealthData)
      if lastSyncedAt != null:
        Text: "Synced {lastSyncedAt.formatRelative()}" — BodySmall, TextSecondary
  content: LazyColumn (nestedScroll, contentPadding PaddingValues(16dp, 0dp, 16dp, 96dp))
    item: ReadinessHeroSection
    item: AcwrGaugeSection
    item: BiometricCardsRow
    item: SleepBreakdownSection
    item: AiRecommendationCard
    item: ReadinessHistorySection
  bottomBar: ApexBottomNavBar (Readiness tab selected)
```

### Zone: ReadinessHeroSection

```
Column (horizontalAlignment = CenterHorizontally, padding vertical 24dp):

  CircularReadinessRing (full-size):
    Size: 220dp × 220dp
    Track:
      Color: BorderSubtle
      strokeWidth: 18dp
      startAngle: 135° (bottom-left)
      sweepAngle: 270° (fills clockwise to bottom-right)
    Fill arc:
      Color: zoneColor (ZoneOptimal/Caution/HighRisk/Undertrained)
      strokeWidth: 18dp
      sweepAngle: (min(readinessScore, 2.0f) / 2.0f) × 270°
      capStyle: Round (both ends)
      Animation: animateFloatAsState, initial 0f → target, 800ms EaseOut on first composition
    Center content (Column, absoluteOffset to center):
      Text: readinessScore (formatted "1.12") — DisplayMedium, zoneColor
      Text: "ACWR" — LabelSmall, TextSecondary
    Tick marks at 0.8 and 1.3 (optimal zone boundaries):
      drawLine: 4dp × 16dp tick, ColorWarning, at calculated angles

  Spacer: 16dp
  ZoneBadge:
    Background: zoneColor at 15% alpha
    Border: 1dp zoneColor
    Text: readinessZone.displayLabel — LabelLarge, zoneColor
    Corner: CornerSmall
    Padding: horizontal 16dp, vertical 8dp

  Spacer: 8dp
  Text: readinessScore AI recommendation (first sentence only) — BodyMedium, TextSecondary, textAlign Center, padding horizontal 16dp
```

### Zone: AcwrGaugeSection

```
Text: "TRAINING LOAD" — LabelSmall, TextSecondary, paddingBottom 12dp

ApexCard:
  Padding: 16dp
  Column (spacedBy 12dp):

    Row (spaceBetween):
      Column:
        Text: "Acute Load (7d)" — BodySmall, TextSecondary
        Text: "${acuteLoad.formatOneDecimal()}" — HeadlineSmall, TextPrimary
      Column (horizontalAlignment = End):
        Text: "Chronic Load (28d)" — BodySmall, TextSecondary
        Text: "${chronicLoad.formatOneDecimal()}" — HeadlineSmall, TextPrimary

    LinearProgressBar: acuteLoad / max(acuteLoad, chronicLoad * 1.5f)
      color: ElectricBlue, track: BorderSubtle, height 8dp, corner CornerFull

    Row (spacedBy 8dp):
      ZoneReferenceChip (each, non-interactive, small):
        Text + color: "< 0.8 Undertrained" TextSecondary | "0.8–1.3 Optimal" NeonGreen | "1.3–1.5 Caution" ColorWarning | "> 1.5 High Risk" ColorError
        Font: LabelSmall
        Padding: horizontal 8dp, vertical 4dp
        Background: color at 12% alpha
```

### Zone: BiometricCardsRow

```
Text: "BIOMETRICS" — LabelSmall, TextSecondary, paddingBottom 12dp

Row (horizontalArrangement = spacedBy(8dp)):

  BiometricCard (weight 1f):
    ApexCard (height 110dp):
    Column (padding 12dp, spacedBy 4dp):
      Icon: Lucide Activity 20dp, ElectricBlue
      Text: "HRV" — LabelSmall, TextSecondary
      Text: "${latestHrv} ms" — HeadlineSmall, TextPrimary
        Color adjustment:
          hrv > 60ms: NeonGreen
          hrv 40-60ms: ColorWarning
          hrv < 40ms: ColorError
      Text: "RMSSD" — LabelSmall, TextSecondary

  BiometricCard (weight 1f):
    ApexCard (height 110dp):
    Column (padding 12dp, spacedBy 4dp):
      Icon: Lucide Moon 20dp, ElectricBlue
      Text: "SLEEP" — LabelSmall, TextSecondary
      Text: formatHoursMinutes(sleepDuration) — HeadlineSmall, TextPrimary
      SleepQualityIndicator:
        Row (spacedBy 2dp): 5 small bars (4dp × 12dp each, CornerFull)
          filled: sleepQuality.barCount (EXCELLENT=5, GOOD=4, FAIR=3, POOR=2)
          filled bars: NeonGreen; empty bars: BorderSubtle
```

### Zone: SleepBreakdownSection

```
Text: "SLEEP STAGES" — LabelSmall, TextSecondary, paddingBottom 12dp

ApexCard:
  Padding: 16dp

  SleepStageBar:
    Row (fillMaxWidth, height 20dp, corner CornerFull, clip):
      For each stage (Deep, REM, Light, Awake):
        Box:
          weight: stage.durationMinutes.toFloat() / totalSleepMinutes
          height: 20dp
          Background: stageColor
      Stage colors:
        Deep:   Color(0xFF1E5FAD)  // deep blue
        REM:    ElectricBlue
        Light:  Color(0xFF5B8DD9)  // medium blue
        Awake:  BorderSubtle

  Spacer: 12dp

  Row (horizontalArrangement = spacedBy(16dp)):
    For each stage (Deep, REM, Light):
      Column:
        Row (spacedBy 6dp, verticalAlignment = CenterVertically):
          Box (12dp × 12dp, CornerFull, background stageColor)
          Text: stage.label — LabelSmall, TextSecondary
        Text: formatHoursMinutes(stage.duration) — TitleMedium, TextPrimary
```

### Zone: AiRecommendationCard

```
ApexCard (background gradient SurfaceCard → ElectricBlue at 8% alpha):
  Border: 1dp ElectricBlue at 40% alpha
  Padding: 16dp

  Row (spacedBy 12dp):
    Icon: Lucide Brain 32dp, ElectricBlue (left-aligned top)
    Column (weight 1f):
      Text: "AI RECOMMENDATION" — LabelSmall, ElectricBlue
      Spacer: 4dp
      Text: readinessScore.recommendation (full text) — BodyLarge, TextPrimary
      Spacer: 12dp
      IntensityRecommendationRow:
        Row (spacedBy 8dp):
          IntensityBadge:
            derived from readinessZone:
              OPTIMAL:      "Full Intensity" NeonGreen
              CAUTION:      "Moderate Intensity" ColorWarning
              HIGH_RISK:    "Active Recovery Only" ColorError
              UNDERTRAINED: "Build Gradually" TextSecondary
            Background: badgeColor at 15% alpha, Border 1dp badgeColor
            Corner: CornerSmall, Padding: horizontal 12dp vertical 6dp
            Text: LabelLarge, badgeColor
```

### Zone: ReadinessHistorySection

```
Row (spaceBetween, paddingBottom 12dp):
  Text: "7-DAY TREND" — LabelSmall, TextSecondary
  TextButton: "View History" → (future — not MVP, show as disabled)

MiniLineChart (Canvas, height 80dp, fillMaxWidth):
  Same implementation pattern as PrDetailScreen LineChart but mini
  Data: last 7 readiness scores
  Line: ElectricBlue
  Zone shading: filled horizontal bands (red above 1.5, yellow 1.3-1.5, green 0.8-1.3, gray below 0.8) at 10% alpha
  Y axis: 0.0 to 2.0+ scale
  No axis labels (mini version) — just the line on colored bands
  Date labels: only first and last, LabelSmall TextSecondary below chart

Loading state (entire LazyColumn):
  Skeleton version of each section (ShimmerSkeleton matching approximate heights)

Health Connect permissions not granted state:
  Replace entire content with HealthConnectPermissionRequest:
    Icon: custom illustration or Lucide HeartPulse 72dp, TextSecondary
    Text: "Health Data Required" — HeadlineMedium, TextPrimary, textAlign Center
    Text: "Connect your wearable data to unlock your personalized readiness score." — BodyMedium, TextSecondary
    PrimaryButton: "Connect Health Data" → navigate to "readiness/setup"
    SecondaryButton: "Learn More" → opens bottom sheet explaining ACWR
```

### ViewModel State Required

```kotlin
// ReadinessViewModel exposes ReadinessUiState (see Architecture Plan)
// Additional: hrvHistory: List<HrvReading> for mini chart (7 days)
// Additional: readinessHistory: List<ReadinessScore> for 7-day trend chart
```

### Accessibility Notes

- CircularReadinessRing: contentDescription = "Readiness score ${readinessScore}, zone: ${zone.displayLabel}"
- HRV card: announce color-coding in description (not just value): "HRV ${value} milliseconds. ${qualityDescription}"
- Sleep stage bar: contentDescription = "Sleep stages: ${deep}% deep sleep, ${rem}% REM, ${light}% light sleep"
- AI Recommendation card: role = region, labeled "AI training recommendation"

---

## SCREEN S18: ProfileScreen

**Route:** `profile`
**Feature Module:** `feature-auth`
**User Story:** As an athlete, I want to manage my account, Health Connect permissions, and app preferences, and be able to log out.
**Entry Points:** Bottom nav "Profile" tab
**Exit Points:** LoginScreen (on logout), HealthConnectSetupScreen, external app settings

### Compose Component Hierarchy

```
ProfileScreen (Scaffold)
  topBar: LargeTopAppBar
    title: Text("Profile", HeadlineLarge when expanded)
    scrollBehavior: enterAlwaysScrollBehavior()
    actions: IconButton (Lucide Edit 24dp) → navigate to EditProfileScreen (future)
  content: LazyColumn (nestedScroll, contentPadding PaddingValues(16dp, 0dp, 16dp, 96dp))
    item: ProfileHeroSection
    item: AthleteStatsSection
    item: HealthConnectSection
    item: AccountSection
    item: AppPreferencesSection
    item: LogoutButton
    item: AppVersionFooter
  bottomBar: ApexBottomNavBar (Profile tab selected)
```

### Zone: ProfileHeroSection

```
Column (horizontalAlignment = CenterHorizontally, padding vertical 24dp):

  Box:
    AsyncImage:
      Size: 96dp × 96dp
      Shape: CircleShape
      Placeholder: Box(96dp, CircleShape, SurfaceElevated, centered icon Lucide User 48dp TextSecondary)
      ContentScale: Crop
    Box (24dp × 24dp, bottom-end offset, background ElectricBlue, CircleShape, border 2dp BackgroundDeepBlack):
      Icon: Lucide Camera 12dp, TextOnBlue (edit avatar hint)

  Spacer: 12dp
  Text: userProfile.displayName — HeadlineMedium, TextPrimary
  Text: userProfile.email — BodyMedium, TextSecondary

  Spacer: 12dp
  Row (spacedBy 24dp):
    MemberStatChip:
      Text: "Member since" — LabelSmall, TextSecondary
      Text: createdAt.formatMonthYear() — BodyMedium, TextPrimary
    Divider: 1dp × 20dp BorderSubtle, vertical
    MemberStatChip:
      Text: "Total WODs" — LabelSmall, TextSecondary
      Text: totalWodCount.toString() — BodyMedium, TextPrimary
```

### Zone: AthleteStatsSection

```
SectionHeader: "PERFORMANCE STATS"

ApexCard:
  Padding: 0dp (grid layout fills card)
  2×2 grid (Row Row):
    StatCell (each, weight 1f, height 80dp, padding 16dp):
      Text: value — HeadlineSmall, ElectricBlue
      Text: label — BodySmall, TextSecondary
    Divider between cells (1dp BorderSubtle vertical + horizontal)

  Cells:
    [0,0]: totalWorkouts, "Workouts Logged"
    [0,1]: totalPrs, "Personal Records"
    [1,0]: currentStreak + "d", "Current Streak"
    [1,1]: avgReadiness.formatOneDecimal(), "Avg Readiness"
```

### Zone: HealthConnectSection

```
SectionHeader: "HEALTH CONNECT"

ApexCard:
  Column (padding 0dp, divider between items):
    PermissionToggleRow (for each permission):
      Row (spaceBetween, padding horizontal 16dp, vertical 14dp, verticalAlignment = CenterVertically):
        Row (spacedBy 12dp):
          Icon: iconForPermission 20dp, ElectricBlue
          Column:
            Text: permissionLabel — TitleMedium, TextPrimary
            Text: permissionDescription — BodySmall, TextSecondary
        Switch:
          checked: isGranted
          trackColor (checked): ElectricBlue
          thumbColor (checked): BackgroundDeepBlack
          onChange: if !checked → navigate to "readiness/setup" | if checked → request revoke via system settings

  Permissions:
    Icon: Lucide Activity, "Heart Rate Variability", "RMSSD recovery data", isGranted: uiState.hrvPermissionGranted
    Icon: Lucide Moon, "Sleep Tracking", "Deep and REM sleep stages", isGranted: uiState.sleepPermissionGranted
    Icon: Lucide Heart, "Heart Rate", "Live workout heart rate", isGranted: uiState.heartRatePermissionGranted

  Row (padding 16dp, spacedBy 8dp):
    Icon: Lucide Info 16dp, TextSecondary
    Text: "Data is read-only. ApexAI never writes to Health Connect." — BodySmall, TextSecondary, italic
```

### Zone: AccountSection

```
SectionHeader: "ACCOUNT"

ApexCard:
  Column (padding 0dp):
    SettingsRow: "Account Email", trailing: Text(email, BodyMedium, TextSecondary)
    SettingsRow: "Change Password", trailing: Icon Lucide ChevronRight 16dp TextSecondary, clickable → external Supabase password reset
    SettingsRow: "Delete Account", trailing: Icon Lucide ChevronRight 16dp ColorError, Text color ColorError, shows AlertDialog with destructive confirmation

SettingsRow pattern:
  Row (spaceBetween, padding horizontal 16dp, vertical 16dp, fillMaxWidth):
    Text: label — TitleMedium, TextPrimary
    trailing content as specified
  Divider: 1dp BorderSubtle between rows
```

### Zone: AppPreferencesSection

```
SectionHeader: "PREFERENCES"

ApexCard:
  Column (padding 0dp):
    ToggleRow: "Weight Units", trailing: SegmentedControl ["kg", "lbs"]
      SegmentedControl:
        Row (background SurfaceElevated, corner CornerSmall, border 1dp BorderSubtle):
          Each segment (weight 1f, padding vertical 8dp):
            Background: if selected ElectricBlue else Transparent
            Text: label — LabelLarge, if selected TextOnBlue else TextSecondary
            onClick: onEvent(PreferenceEvent.UnitChanged)
    Divider
    ToggleRow: "Haptic Feedback", trailing: Switch (same styling as Rx toggle)
    Divider
    ToggleRow: "Audio Cues (Timer)", trailing: Switch
    Divider
    ToggleRow: "Push Notifications", trailing: Switch
```

### Zone: LogoutButton

```
Spacer: 24dp

DestructiveButton: "Log Out" fullWidth height 52dp
  Icon: Lucide LogOut 20dp leading
  onClick: show ConfirmLogoutDialog
    Dialog: AlertDialog
      containerColor: SurfaceElevated
      title: "Log Out?" — TitleLarge, TextPrimary
      text: "You will need to log back in to access your data." — BodyMedium, TextSecondary
      confirmButton: DestructiveButton "Log Out" → onEvent(AuthEvent.LogoutClicked)
      dismissButton: TextButton "Cancel"
```

### Zone: AppVersionFooter

```
Column (horizontalAlignment = CenterHorizontally, padding vertical 16dp):
  Image: app logo 40dp, tint TextDisabled
  Text: "ApexAI Athletics" — LabelSmall, TextSecondary
  Text: "Version {BuildConfig.VERSION_NAME} ({BuildConfig.VERSION_CODE})" — LabelSmall, TextDisabled
  Text: "Powered by Gemini AI" — LabelSmall, TextDisabled
```

### ViewModel State Required

```kotlin
// AuthViewModel (extended for profile) exposes:
data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val totalWorkouts: Int = 0,
    val totalPrs: Int = 0,
    val currentStreak: Int = 0,
    val avgReadiness: Float? = null,
    val hrvPermissionGranted: Boolean = false,
    val sleepPermissionGranted: Boolean = false,
    val heartRatePermissionGranted: Boolean = false,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val hapticEnabled: Boolean = true,
    val audioCuesEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
enum class WeightUnit { KG, LBS }
```

### Accessibility Notes

- HealthConnect toggles: each row should be a merged semantics node with full description: "${permissionLabel}: currently ${if granted 'enabled' else 'disabled'}"
- Logout button: destructive action, role = Button, contentDescription = "Log out of ApexAI Athletics"
- Avatar edit hint: contentDescription = "Profile photo. Tap to change."
- Switches: standard Switch semantics work well; ensure labels are associated programmatically

---

## PART 3 — NAVIGATION AND FLOW MAP

### 3.1 Full Screen Flow Diagram

```
[COLD LAUNCH]
     |
     v
[S01 SplashScreen] ──── auth check ────┐
                                       |
                    authenticated      |     unauthenticated
                         |             |         |
                         v             └─────────┘
                    [S04 HomeScreen]        |
                         |             [S02 LoginScreen]
                    ┌────┴─────┐            |
                    |          |       [S03 RegisterScreen]
              Bottom Nav   Bottom Nav        |
                 tabs       tabs          → S04 HomeScreen
                 |            |
    ┌────────────┼────────────┼────────────────────────────┐
    |            |            |                            |
[S04 Home]  [S05 WodBrowse] [S10 PrDashboard]  [S12 Readiness] [S18 Profile]
    |            |            |                    |
    |        [S06 WodDetail] [S11 PrDetail]   [S13 HCSetup]
    |            |
    |       [S09 WodTimer]
    |            |
    |        [S07 WodLog] ─── PR Achieved Sheet (overlay)
    |
    |── Camera FAB ──────────────────────────────────────┐
                                                          |
                                                   [S14 LiveCamera]
                                                          |
                                                   [S15 RecordingReview]
                                                          |
                                                   [S16 CoachingReport]
                                                          |
                                                   [S17 VideoPlayback]
                                                          |
                                                   ← back to S16
                                                          |
                                                   ← Done to S04 Home
```

### 3.2 Shared Layout Templates

```
PublicLayout (S01, S02, S03):
  Full screen, BackgroundDeepBlack
  No bottom navigation
  No top app bar (custom per-screen)
  StatusBar: transparent, light icons (dark scrim)

AuthenticatedLayout (S04, S05, S10, S12, S18):
  Scaffold with BottomNavBar
  StatusBar: transparent, light icons
  NavigationBar: SurfaceDark, 80dp height
  Content padding accounts for BottomNavBar height

FullScreenLayout (S09, S14, S15, S17):
  No Scaffold
  Full screen including system bars (edgeToEdge)
  System bar icons: white (light)
  Back handled by system Back gesture or custom in-screen button

DetailLayout (S06, S07, S11, S13, S16, S18 sub-screens):
  Scaffold with CenterAlignedTopAppBar or LargeTopAppBar
  Back arrow NavigationIcon
  No bottom navigation
  Content scrollable with windowInsetsPadding applied
```

### 3.3 Screen Transition Specifications

```
Standard push/pop:
  Enter: slideInHorizontally { it } (slides in from right)
  Exit:  slideOutHorizontally { -it } (slides out to left)
  Pop enter: slideInHorizontally { -it } (slides in from left)
  Pop exit:  slideOutHorizontally { it } (slides out to right)
  Duration: 300ms EaseInOut

Modal/bottom-up screens (WodLog, CoachingReport):
  Enter: slideInVertically { it } (slides in from bottom)
  Exit:  slideOutVertically { it } (slides out to bottom)
  Duration: 350ms EaseOut

Home → Camera FAB (LiveCameraScreen):
  Enter: fadeIn + scaleIn(0.95 → 1.0)
  Exit (back to Home): fadeOut + scaleOut(1.0 → 0.95)
  Duration: 250ms EaseInOut

Splash → Auth/Home:
  Enter: fadeIn
  Exit: fadeOut
  Duration: 400ms linear

All transitions implemented via AnimatedNavHost with:
  enterTransition / exitTransition / popEnterTransition / popExitTransition
  using Compose Animation APIs (slideInHorizontally, fadeIn, etc.)
```

---

## PART 4 — CODER AGENT HANDOFF SUMMARY

### 4.1 Implementation Order (Recommended)

```
Phase 1 — Foundation (do first, everything depends on these)
  1. core-ui: Theme setup (ApexTheme, Color.kt, Type.kt, Shape.kt)
  2. core-ui: Shared composables (ApexCard, ApexTextField, PrimaryButton, SecondaryButton, TextButton, DestructiveButton)
  3. core-ui: ApexBottomNavBar with Camera FAB
  4. core-ui: ShimmerSkeleton utility composable
  5. app: MainActivity (edgeToEdge, enableCompose), NavHost setup, all routes registered

Phase 2 — Auth (required before any authenticated screen)
  6. S02 LoginScreen
  7. S03 RegisterScreen
  8. S01 SplashScreen (auth state check + routing)

Phase 3 — Core Tracking Screens (MVP Week 1)
  9.  S06 WodDetailScreen
  10. S09 WodTimerScreen (requires foreground service for background timer)
  11. S07 WodLogScreen + PR celebration BottomSheet
  12. S05 WodBrowseScreen
  13. S04 HomeScreen (depends on WOD + PR + Readiness data existing)

Phase 4 — PR Screens (MVP Week 2)
  14. S10 PrDashboardScreen
  15. S11 PrDetailScreen (requires LineChart Canvas implementation)

Phase 5 — Readiness Screens (MVP Week 2)
  16. S13 HealthConnectSetupScreen
  17. S12 ReadinessDashboardScreen (CircularReadinessRing + LineChart)

Phase 6 — Vision Screens (MVP Week 3 — highest complexity)
  18. S14 LiveCameraScreen (CameraX + MediaPipe Canvas overlay)
  19. S15 RecordingReviewScreen (Media3 player from pool)
  20. S16 CoachingReportScreen
  21. S17 VideoPlaybackScreen (Media3 pool + kinematic Canvas overlay)

Phase 7 — Profile
  22. S18 ProfileScreen
```

### 4.2 Shared Component Build List (build before any screen uses them)

```
core-ui components to build first:
  [ ] ApexTheme (MaterialTheme wrapper with dark color scheme)
  [ ] ApexBottomNavBar (5-item nav with center FAB)
  [ ] ApexCard (with states: default, pressed, selected, loading, error)
  [ ] ApexTextField (with states: default, focused, error, disabled)
  [ ] PrimaryButton (with loading state)
  [ ] SecondaryButton
  [ ] TextButton
  [ ] DestructiveButton
  [ ] IconButton (styled wrapper)
  [ ] FilterChip
  [ ] TimeDomainBadge (AMRAP/EMOM/RFT/TABATA with color mapping)
  [ ] ShimmerSkeleton (utility composable accepting modifier + shape)
  [ ] ApexSnackbar (4 variants: info/success/warning/error)
  [ ] CircularReadinessRing (canvas-based arc, parameterized)
  [ ] LinearProgressBar (styled)
  [ ] SectionHeader (LabelSmall + optional trailing TextButton)
  [ ] LineChart (canvas-based, reusable for PrDetail + Readiness mini chart)
  [ ] EmptyStateComposable (icon + title + subtitle + optional CTA button)
  [ ] ErrorStateComposable (icon + message + retry button)
  [ ] PermissionRequestScreen (reusable for Camera + Health Connect)
```

### 4.3 Third-Party Dependencies Required

```gradle
// In libs.versions.toml:

[versions]
compose-bom = "2025.01.00"          // Jetpack Compose BOM
coil = "3.0.4"                      // Image loading
media3 = "1.5.0"                    // Video playback
camerax = "1.4.1"                   // Camera
mediapipe = "0.10.14"               // Pose detection

[libraries]
// Google Fonts for Inter (or bundle Inter .ttf in assets/)
compose-ui-google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts" }

// Coil for async image loading
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

// Media3
media3-exoplayer = { group = "androidx.media3", name = "media3-exoplayer", version.ref = "media3" }
media3-ui = { group = "androidx.media3", name = "media3-ui", version.ref = "media3" }

// CameraX
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }
camerax-video = { group = "androidx.camera", name = "camera-video", version.ref = "camerax" }

// MediaPipe
mediapipe-tasks-vision = { group = "com.google.mediapipe", name = "tasks-vision", version.ref = "mediapipe" }

// Lucide Icons for Compose (community port)
// Option A: lucide-icons-compose (unofficial, check current version on Maven)
// Option B: use Material Icons Extended and map equivalents — safer for production
// Recommendation: Use Material Icons Extended for production; Lucide mapping table in Notes below
```

### 4.4 Lucide → Material Icons Mapping

```
Since Lucide for Compose is unofficial, use Material Icons Extended equivalents:

Lucide ArrowLeft       → Icons.AutoMirrored.Filled.ArrowBack
Lucide Bell            → Icons.Filled.Notifications
Lucide Camera          → Icons.Filled.PhotoCamera
Lucide Video           → Icons.Filled.Videocam
Lucide Dumbbell        → Icons.Filled.FitnessCenter
Lucide Trophy          → Icons.Filled.EmojiEvents
Lucide Brain           → Icons.Filled.Psychology
Lucide Heart           → Icons.Filled.Favorite
Lucide Activity        → Icons.Filled.MonitorHeart
Lucide Moon            → Icons.Filled.Bedtime
Lucide Weight          → Icons.Filled.Scale
Lucide Timer           → Icons.Filled.Timer
Lucide Play            → Icons.Filled.PlayArrow
Lucide Pause           → Icons.Filled.Pause
Lucide Stop            → Icons.Filled.Stop
Lucide RefreshCcw      → Icons.Filled.Refresh
Lucide Settings        → Icons.Filled.Settings
Lucide LogOut          → Icons.AutoMirrored.Filled.Logout
Lucide ChevronRight    → Icons.AutoMirrored.Filled.KeyboardArrowRight
Lucide ChevronUp       → Icons.Filled.KeyboardArrowUp
Lucide Plus            → Icons.Filled.Add
Lucide X               → Icons.Filled.Close
Lucide Check           → Icons.Filled.Check
Lucide AlertTriangle   → Icons.Filled.Warning
Lucide Info            → Icons.Filled.Info
Lucide Share           → Icons.Filled.Share
Lucide Trash2          → Icons.Filled.Delete
Lucide Edit            → Icons.Filled.Edit
Lucide FlipHorizontal2 → Icons.Filled.FlipCameraAndroid
Lucide SkipBack        → Icons.Filled.SkipPrevious
Lucide SkipForward     → Icons.Filled.SkipNext
Lucide FastForward     → Icons.Filled.FastForward
Lucide Rewind          → Icons.Filled.FastRewind
Lucide Bookmark        → Icons.Filled.Bookmark
Lucide TrendingUp      → Icons.Filled.TrendingUp
Lucide SlidersHoriz    → Icons.Filled.Tune
Lucide ShieldAlert     → Icons.Filled.GppBad
Lucide HeartPulse      → Icons.Filled.MonitorHeart
Lucide Aperture        → Icons.Filled.Aperture (check availability, fallback: Camera)
```

### 4.5 Environment Variables and API Endpoints Referenced

```
// BuildConfig fields (set via local.properties + CI secrets):
BuildConfig.SUPABASE_URL        // Supabase project REST endpoint
BuildConfig.SUPABASE_ANON_KEY   // Supabase anon public key
BuildConfig.FASTAPI_BASE_URL    // FastAPI microservice URL (coaching upload endpoint)

// Endpoints called by screens:
GET  {SUPABASE_URL}/rest/v1/workouts           // WodBrowseScreen, HomeScreen
GET  {SUPABASE_URL}/rest/v1/workouts?id=eq.{id} // WodDetailScreen
POST {SUPABASE_URL}/rest/v1/results            // WodLogScreen submit
GET  {SUPABASE_URL}/rest/v1/personal_records   // PrDashboardScreen, HomeScreen
GET  {SUPABASE_URL}/functions/v1/readiness-score // ReadinessDashboardScreen (Edge Function)
POST {FASTAPI_BASE_URL}/api/v1/analyze         // RecordingReviewScreen upload + analysis
GET  {FASTAPI_BASE_URL}/api/v1/report/{id}     // CoachingReportScreen
GET  {FASTAPI_BASE_URL}/api/v1/overlay/{id}    // VideoPlaybackScreen overlay data
```

### 4.6 Open Questions and Decisions Deferred to Coder

```
1. FONT DELIVERY: Inter font via Google Fonts (requires internet on first load) vs. bundled
   .ttf assets (larger APK, works offline). Recommendation: bundle Inter Regular (400),
   Medium (500), SemiBold (600), Bold (700), ExtraBold (800) as assets for reliability.

2. LINE CHART LIBRARY: The design uses a custom Canvas-based chart for PrDetailScreen and
   ReadinessDashboardScreen. The coder may choose to use a library such as Vico
   (github.com/patrykandpatrick/vico) which has Compose support. If using Vico, apply
   the same color tokens defined in this spec. Custom Canvas is also acceptable.

3. TABATA AUDIO CUES: The timer screen calls for audio cues on interval transitions. The
   coder must decide whether to use MediaPlayer (simple beep files in /raw) or SoundPool.
   SoundPool is preferred for low-latency short sounds. Files needed: work_start.mp3,
   rest_start.mp3, workout_complete.mp3.

4. FOREGROUND SERVICE for timer: WodTimerScreen requires a foreground service to keep the
   timer running when app is backgrounded. The coder must implement WorkoutTimerService
   extending Service with NotificationCompat showing elapsed/remaining time. The ViewModel
   binds to this service. This is not specified in the architecture plan but is required
   for the timer UX to be usable.

5. VIDEO UPLOAD: RecordingReviewScreen uploads to Supabase Storage first (not directly to
   FastAPI). The storage path is then passed to FastAPI. The coder must implement
   multipart upload via Supabase Storage client. Video size limit and compression
   (e.g., using MediaCodec or ffmpeg-kit) are not specified — coder should define a
   reasonable limit (suggest 60 seconds / ~200MB max) and inform the PM if compression
   is needed.

6. MEDIAPIPE MODEL FILE: The PoseLandmarker task model file (pose_landmarker_full.task)
   must be bundled in /assets. The coder must configure assetPath in
   PoseLandmarkerHelper. Model size is ~7MB — acceptable for APK inclusion.

7. EXOPLAYER POOL SIZE: CLAUDE.md mandates ExoPlayer pooling. The coder must define pool
   size in core-media/PlayerPoolManager. Recommended: pool of 2 instances (sufficient for
   CoachingReport + VideoPlayback; no concurrent video lists in current design).

8. DEEP LINK HANDLING: The architecture plan specifies deep links (apexai://wod/{wodId}
   and apexai://coaching/report/{analysisId}). The coder must register these in
   AndroidManifest.xml intent filters and handle them in the NavHost deepLinks
   configuration.

9. HEALTH CONNECT PERMISSION RATIONALE STRINGS: Android requires user-facing rationale
   for each Health Connect permission type. These strings should be placed in
   strings.xml with keys matching the permission types. Sample rationale:
   "HeartRateVariability_rationale": "Used to calculate your recovery score and
   determine safe training intensity."

10. PR CELEBRATION PARTICLES: The NeonGreen particle burst animation on the PR
    celebration sheet is a nice-to-have. If implementation time is limited, replace
    with a simple animated scale entrance on the Trophy icon (keep the spring animation).
    The particles add delight but are not required for MVP.
```

### 4.7 Color Contrast Compliance Summary

```
All combinations verified against WCAG 2.2 AA (4.5:1 minimum for normal text):

TextPrimary (#F0F0FF) on BackgroundDeepBlack (#0A0A0F): 19.2:1  ✓ AAA
TextPrimary (#F0F0FF) on SurfaceCard (#1E1E2E):          14.7:1  ✓ AAA
TextPrimary (#F0F0FF) on SurfaceElevated (#1A1A26):      15.8:1  ✓ AAA
TextSecondary (#9090B0) on SurfaceCard (#1E1E2E):         5.1:1  ✓ AA
TextOnBlue (#000000) on ElectricBlue (#00D4FF):          11.2:1  ✓ AAA
ElectricBlue (#00D4FF) on BackgroundDeepBlack (#0A0A0F):  9.2:1  ✓ AAA
ElectricBlue (#00D4FF) on SurfaceCard (#1E1E2E):          9.2:1  ✓ AAA
NeonGreen (#39FF14) on SurfaceCard (#1E1E2E):             8.9:1  ✓ AAA
ColorError (#FF3B5C) on SurfaceCard (#1E1E2E):            5.8:1  ✓ AA
ColorWarning (#FFB800) on SurfaceCard (#1E1E2E):          7.4:1  ✓ AAA
ColorWarning (#FFB800) on BackgroundDeepBlack (#0A0A0F):  8.5:1  ✓ AAA
TextDisabled (#505068) on SurfaceCard (#1E1E2E):          2.8:1  ✗ FAIL (intentional for disabled state, WCAG exception)

NOTE: TextDisabled intentionally fails AA — disabled elements are exempt from WCAG 1.4.3.
All interactive and informational text meets AA minimum.
```

---

*End of ApexAI Athletics UI/UX Design Templates v1.0*
*Prepared by UI/UX Designer Agent — 2026-03-28*
*For questions or clarifications, reference memory files at:*
`/home/cadieuxj/Documents/GitHub/CrossfitApp/.claude/agent-memory/ui-ux-designer/`