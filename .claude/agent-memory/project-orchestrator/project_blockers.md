---
name: project_blockers
description: Build blockers and gaps preventing the app from compiling and running on a physical Android device
type: project
---

**Last assessed:** 2026-03-28 (post-WorkManager iteration)

## Build Blockers — ALL RESOLVED

| # | Blocker | Status |
|---|---------|--------|
| B-01 | Accompanist Permissions missing from dependency graph | RESOLVED — present in libs.versions.toml (0.36.0) and build.gradle.kts |
| B-02 | Launcher icon mipmap resources absent | RESOLVED — confirmed mipmap dirs and icons present |
| B-03 | JUnit 5 platform not configured | RESOLVED — testOptions.unitTests.useJUnitPlatform() present in build.gradle.kts |
| R-01 | MediaPipe model file missing | RESOLVED — pose_landmarker_lite.task confirmed in app/src/main/assets/ |

## New Gaps Introduced by WorkManager Addition (2026-03-28)

| # | Gap | Severity | Details |
|---|-----|----------|---------|
| W-01 | Missing MacroReminderWorker unit test | Warning | No test file covers worker logic — only one in the suite that has no test coverage |
| W-02 | `navigate_to` intent extra not handled in MainActivity | Warning | Worker sets putExtra("navigate_to", "nutrition/log") but MainActivity never reads this extra — tapping notification opens app but does NOT deep-link to macro log screen |
| W-03 | No proguard-rules.pro file | Warning | isMinifyEnabled=true in release but no proguard-rules.pro file found anywhere in the project; WorkManager/Hilt Worker keep rules are missing |
| W-04 | RECEIVE_BOOT_COMPLETED permission absent | Info | WorkManager re-enqueues on app restart by default (API 23+); no explicit boot-completed handling needed — acceptable |
| W-05 | ExistingPeriodicWorkPolicy.KEEP on reschedule | Info | If user changes time zone, the initial delay becomes stale until next app cold start — low-impact, not a blocker |

## Remaining Quality Gaps (pre-existing, unresolved)

| # | Gap | Impact |
|---|-----|--------|
| Q-01 | CORS wildcard in FastAPI settings default | Backend security — must configure before production |
| Q-02 | python-jose 3.3.0 known CVEs | Backend security — upgrade to joserfc or python-jwt |
| Q-03 | Sentry no PII scrubbing | Privacy compliance gap |
| Q-04 | error_password_short string says "8 characters" but min is 12 | UI/UX inconsistency |
| Q-05 | No developer onboarding docs (local.properties.example, .env.example) | DX gap |

## Production Gate Status

- Build compiles: PASS (all B-xx blockers resolved)
- Runtime launch: PASS (R-01 resolved)
- WorkManager/Hilt integration: PASS (wiring correct)
- Notification tap-to-navigate: FAIL (W-02 — intent extra not consumed)
- ProGuard rules: FAIL (W-03 — no rules file, release minification may strip WorkManager)
- Worker test coverage: FAIL (W-01)
