---
name: reference_key_files
description: Where to find critical files and directories in the ApexAI Athletics repo
type: reference
---

## Android App
- Entry point: `app/src/main/kotlin/com/apexai/crossfit/MainActivity.kt`
- Application class: `app/src/main/kotlin/com/apexai/crossfit/CrossfitApplication.kt`
- Navigation graph: `app/src/main/kotlin/com/apexai/crossfit/core/ui/navigation/AppNavigation.kt`
- Build config: `app/build.gradle.kts`
- Version catalog: `gradle/libs.versions.toml`
- AndroidManifest: `app/src/main/AndroidManifest.xml`
- Strings: `app/src/main/res/values/strings.xml`
- FileProvider paths: `app/src/main/res/xml/file_paths.xml`
- MediaPipe helper: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/data/MediaPipePoseLandmarkerHelper.kt`
- VisionViewModel: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/camera/VisionViewModel.kt`
- LiveCameraScreen: `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/camera/LiveCameraScreen.kt`
- RegisterScreen: `app/src/main/kotlin/com/apexai/crossfit/feature/auth/presentation/register/RegisterScreen.kt`

## Backend
- FastAPI main: `backend/main.py`
- Gemini service: `backend/gemini_service.py`
- Pydantic models: `backend/models.py`
- Dependencies: `backend/requirements.txt`
- Dockerfile: `backend/Dockerfile`

## Database
- Initial schema: `supabase/migrations/001_initial_schema.sql`
- Data retention: `supabase/migrations/002_data_retention.sql`

## CI/CD
- GitHub Actions: `.github/workflows/android-ci.yml`
- Fastlane: `fastlane/Fastfile`, `fastlane/Appfile`

## Documentation
- Technical spec: `docs/TECHNICAL_SPEC.md`
- Architecture plan: `docs/ARCHITECTURE_PLAN.md`
- UI/UX design: `docs/UI_UX_DESIGN.md`
- Code review: `docs/CODE_REVIEW.md`
- Security audit: `docs/SECURITY_AUDIT.md`

## Missing (critical gaps)
- `app/src/main/assets/pose_landmarker_lite.task` — MediaPipe model file, MUST be added
- `app/src/main/res/mipmap-*/ic_launcher*.png` — Launcher icons, MUST be added
- `local.properties` — dev environment config (gitignored, developer must create)
