---
name: project_blockers
description: Build blockers and gaps preventing the app from compiling and running on a physical Android device
type: project
---

**Last assessed:** 2026-03-28

## Build Blockers (prevent ./gradlew assembleDebug from succeeding)

| # | Blocker | File(s) | Fix |
|---|---------|---------|-----|
| B-01 | Accompanist Permissions missing from dependency graph | gradle/libs.versions.toml, app/build.gradle.kts | Add accompanist-permissions ~0.36.0 to version catalog and implementation dep |
| B-02 | Launcher icon mipmap resources absent | app/src/main/res/ (no mipmap-* dirs) | Generate ic_launcher + ic_launcher_round adaptive icons in all densities |
| B-03 | JUnit 5 platform not configured | app/build.gradle.kts (missing testOptions.unitTests.useJUnitPlatform()) | Add testOptions block |

## Runtime Crashes (app installs but crashes)

| # | Crash | File | Fix |
|---|-------|------|-----|
| R-01 | MediaPipe model file not found | app/src/main/assets/ (directory missing) | Download pose_landmarker_lite.task, create assets/, add file |

## Quality Gaps (app runs but features are incomplete)

| # | Gap | Impact |
|---|-----|--------|
| Q-01 | CORS wildcard in FastAPI settings default | Backend security — must configure before production |
| Q-02 | python-jose 3.3.0 known CVEs | Backend security — upgrade to joserfc or python-jwt |
| Q-03 | Sentry no PII scrubbing | Privacy compliance gap |
| Q-04 | error_password_short string says "8 characters" but min is 12 | UI/UX inconsistency |
| Q-05 | ReadinessRepositoryImpl.getReadinessHistory() was a stub — confirm fix applied | Feature gap if not fixed |
| Q-06 | No developer onboarding docs (local.properties.example, .env.example) | DX gap |

## Next Agent to Run
**android-frontend-coder** — to fix B-01 (add Accompanist dep), B-02 (generate icons), B-03 (JUnit platform config), and R-01 (add assets dir + MediaPipe model placeholder).
