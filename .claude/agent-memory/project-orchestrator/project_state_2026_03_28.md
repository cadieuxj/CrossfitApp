---
name: project_state_2026_03_28
description: Full audit of ApexAI Athletics codebase as of 2026-03-28 — what exists, what works, what is missing
type: project
---

**Audit date:** 2026-03-28

## What Exists (Confirmed by File Audit)

### Android App (88 Kotlin files confirmed)
- All feature modules present: auth, home, wod, pr, readiness, vision, profile
- MVVM + clean architecture correctly layered across all features
- Hilt DI configured throughout; CrossfitApplication annotated @HiltAndroidApp
- MainActivity: single-activity, edge-to-edge, splash screen, AppNavigation entry point
- Compose BOM 2025.01.00, Material3, Navigation Compose all wired
- All P0 and P1 code review fixes confirmed applied (VisionViewModel has real CameraX VideoCapture, DiscardRecording event, LaunchedEffect camera init, etc.)
- All security fixes confirmed applied (privacy checkbox in RegisterScreen, HealthConnect consent body in strings.xml, JWT audience="authenticated" in main.py)
- AndroidManifest: correct permissions, FileProvider scoped to videos/, Health Connect queries block
- strings.xml: complete, covering all screens
- themes.xml, colors.xml: present

### Backend (Python FastAPI)
- main.py: full implementation confirmed, user-scoped client for reads (H-06 fixed), rate limiter, admin guard on cache/refresh
- gemini_service.py: _parse_analysis_response confirmed complete (lines 541+)
- models.py: present
- requirements.txt: pinned, Python 3.11+
- Dockerfile: present
- 002_data_retention.sql: pg_cron retention jobs present (Law 25 compliance)

### QA / CI
- Unit tests: 7 test files (ViewModel, Repository, ACWR, kinematic angle, player pool tests)
- Instrumented tests: 5 test files (login screen, WOD log, live camera, readiness screens, Health Connect)
- .github/workflows/android-ci.yml: present
- fastlane/Fastfile + Appfile: present

## CRITICAL BUILD BLOCKERS (app will NOT compile or crash at launch)

### BLOCKER 1 — Accompanist Permissions library missing from dependency graph
- `LiveCameraScreen.kt` imports `com.google.accompanist.permissions.*` (3 imports)
- `accompanist-permissions` is NOT in `gradle/libs.versions.toml` and NOT in `app/build.gradle.kts`
- Build will fail with "Unresolved reference: accompanist"
- Fix: Add `accompanist-permissions` to version catalog and build.gradle.kts

### BLOCKER 2 — MediaPipe model asset file missing
- `MediaPipePoseLandmarkerHelper.kt` line 47: `.setModelAssetPath("pose_landmarker_lite.task")`
- The `app/src/main/assets/` directory does NOT EXIST
- `pose_landmarker_lite.task` is not present anywhere in the project
- App will crash at runtime when MediaPipe is initialized (FileNotFoundException)
- Fix: Download pose_landmarker_lite.task from Google and add to app/src/main/assets/

### BLOCKER 3 — Launcher icon resources missing
- AndroidManifest references `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`
- No mipmap-* directories exist under app/src/main/res/
- Build will fail: "error: resource mipmap/ic_launcher not found"
- Fix: Generate/add ic_launcher and ic_launcher_round in all mipmap densities

### BLOCKER 4 — JUnit 5 not configured for Android unit tests
- build.gradle.kts uses `testImplementation(libs.junit5.api)` and `testRuntimeOnly(libs.junit5.engine)`
- The `android { testOptions { unitTests { useJUnitPlatform() } } }` block is ABSENT from build.gradle.kts
- Unit tests will not run (Gradle won't find the JUnit Platform runner)
- Fix: Add testOptions block with useJUnitPlatform() to the android{} block in build.gradle.kts

## QUALITY GAPS (app compiles but features broken or incomplete)

- strings.xml uses "Password must be at least 8 characters" but security fix raised minimum to 12 — string is stale
- `local.properties` file not present (expected for dev — not a build blocker if env vars set)
- CORS wildcard still present in main.py settings default (H-06 backend issue, not Android build blocker)
- python-jose 3.3.0 has known CVEs (M-06 in security audit) — upgrade to python-jwt or joserfc
- Sentry SDK present with no PII scrubbing config (H-11)
- No `local.properties.example` or `.env.example` for developer onboarding documentation

## What the Previous Agents Produced
1. app-spec-writer → docs/TECHNICAL_SPEC.md
2. android-architecture-planner → docs/ARCHITECTURE_PLAN.md
3. ui-ux-designer → docs/UI_UX_DESIGN.md
4. backend-dev → supabase/, backend/ (main.py, gemini_service.py, models.py, migrations)
5. android-frontend-coder → app/src/main/kotlin/ (88 files)
6. android-qa-engineer → unit + instrumented tests, android-ci.yml, Fastfile
7. code-review-advisor → docs/CODE_REVIEW.md (24 findings, P0+P1 fixes applied)
8. quebec-security-auditor → docs/SECURITY_AUDIT.md (18 findings, HIGH fixes applied)
