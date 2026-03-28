---
name: ApexAI Athletics — Screen Inventory and Navigation Graph
description: Authoritative list of all 18 screens with routes, feature modules, and navigation relationships
type: project
---

## Screen Inventory (S01–S18)

| ID | Screen | Feature Module | Route |
|---|---|---|---|
| S01 | SplashScreen | app | splash |
| S02 | LoginScreen | feature-auth | auth/login |
| S03 | RegisterScreen | feature-auth | auth/register |
| S04 | HomeScreen | app | home |
| S05 | WodBrowseScreen | feature-wod | wod |
| S06 | WodDetailScreen | feature-wod | wod/{wodId} |
| S07 | WodLogScreen | feature-wod | wod/{wodId}/log |
| S08 | WodHistoryScreen | feature-wod | wod/history |
| S09 | WodTimerScreen | feature-wod | wod/{wodId}/timer |
| S10 | PrDashboardScreen | feature-pr | pr |
| S11 | PrDetailScreen | feature-pr | pr/{movementId} |
| S12 | ReadinessDashboardScreen | feature-readiness | readiness |
| S13 | HealthConnectSetupScreen | feature-readiness | readiness/setup |
| S14 | LiveCameraScreen | feature-vision | vision/live |
| S15 | RecordingReviewScreen | feature-vision | vision/review/{videoUri} |
| S16 | CoachingReportScreen | feature-coaching | coaching/report/{analysisId} |
| S17 | VideoPlaybackScreen | feature-coaching | coaching/playback/{videoId} |
| S18 | ProfileScreen | feature-auth | profile |

## Bottom Navigation (shown on S04, S05, S10, S12, S18)
Home | WOD | [Camera FAB] | Readiness | Profile

## Key Navigation Rules
- Auth flow clears back stack on success: popUpTo("splash") inclusive=true
- WodTimerScreen: popUpTo("wod/{wodId}") on completion to remove timer from back stack
- Vision/Coaching screens are full-screen (no bottom nav)
- Deep links: apexai://wod/{wodId} and apexai://coaching/report/{analysisId}
