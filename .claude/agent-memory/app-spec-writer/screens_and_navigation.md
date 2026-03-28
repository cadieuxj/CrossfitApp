---
name: ApexAI Athletics Screen Inventory and Navigation
description: Complete list of all 18 screens, their feature modules, and the Compose Navigation graph structure
type: project
---

**18 screens across 6 feature modules.** No wireframes exist — frontend agent designs inline using Material 3 dark/athletic theme.

Screen inventory:
- S01 SplashScreen (app) — animated logo, auth state check
- S02 LoginScreen (feature-auth)
- S03 RegisterScreen (feature-auth)
- S04 HomeScreen / Dashboard (app) — today's readiness, recent WODs, quick actions
- S05 WodBrowseScreen (feature-wod)
- S06 WodDetailScreen (feature-wod)
- S07 WodLogScreen (feature-wod)
- S08 WodHistoryScreen (feature-wod)
- S09 WodTimerScreen (feature-wod)
- S10 PrDashboardScreen (feature-pr)
- S11 PrDetailScreen (feature-pr)
- S12 ReadinessDashboardScreen (feature-readiness)
- S13 HealthConnectSetupScreen (feature-readiness)
- S14 LiveCameraScreen (feature-vision) — full-screen, no bottom nav
- S15 RecordingReviewScreen (feature-vision) — full-screen, no bottom nav
- S16 CoachingReportScreen (feature-coaching) — full-screen, no bottom nav
- S17 VideoPlaybackScreen (feature-coaching) — full-screen, no bottom nav
- S18 ProfileScreen (feature-auth)

Bottom navigation: Home | WOD | Camera (FAB center) | Readiness | Profile

Deep links supported: apexai://wod/{wodId} and apexai://coaching/report/{analysisId}

**How to apply:** Any new screen must be added to this inventory and assigned a feature module before implementation begins.
