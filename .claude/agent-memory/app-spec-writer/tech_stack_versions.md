---
name: ApexAI Athletics Tech Stack and Library Versions
description: Authoritative library versions from libs.versions.toml for the Android project
type: project
---

These versions are sourced from ARCHITECTURE_PLAN.md Section 14.1 and are authoritative for all build files.

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
ksp = "2.0.21-1.0.28"
hilt-navigation-compose = "1.2.0"
security-crypto = "1.1.0-alpha06"

Gemini models in FastAPI:
- Primary analysis: gemini-1.5-pro (GEMINI_MODEL env var)
- Visual correction: gemini-2.0-flash (GEMINI_FLASH_MODEL env var)

**Why:** "Gemini 3.1 Pro" referenced in PDF does not exist at knowledge cutoff. Use 1.5 Pro with a config parameter so switching models requires only an env var change.

**How to apply:** All build.gradle.kts files must reference these via libs.versions.toml version catalog, never hardcode versions.
