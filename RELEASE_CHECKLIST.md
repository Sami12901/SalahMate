# Release & GitHub Publishing Checklist (`RELEASE_CHECKLIST.md`)

## 1. Application & Religious Content Integrity
- [x] Unique `applicationId` (`com.samiislam.salahmate`) configured in `app/build.gradle.kts`.
- [x] Custom Sapphire & Silver adaptive launcher icon (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`, and density PNGs in `mipmap-*`) configured without default green template assets.
- [x] Firebase and unused camera/cloud dependencies removed/commented out for minimal APK footprint and strict offline privacy.
- [x] Least-privilege permissions declared in `AndroidManifest.xml` (`INTERNET`, `ACCESS_NETWORK_STATE`, optional `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `VIBRATE`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`).
- [x] First-launch city, calculation authority, and Asr school onboarding flow active.
- [x] Separate handling of Calculation Method (Method 1 = Karachi) and Asr Juristic School (Standard vs. Hanafi).
- [x] Clear distinction between Imsak, Fajr, Maghrib, and Iftar with local adjustment controls.
- [x] Preserved vocalized Uthmani Arabic script and explicit translator/reciter attribution for Quran, Hadith, and Dua content.
- [x] Room database migrations (`MIGRATION_1_2`) and JSON export/import verified.

## 2. GitHub Repository & Release Readiness
- [x] Showcase `README.md` with badges, 17-module feature matrix, architecture diagram, build instructions, and Bangladesh timetable comparison notes.
- [x] `LICENSE` (MIT License for source code + religious text attribution notice) and `CONTENT_LICENSES.md`.
- [x] `CHANGELOG.md` and `RELEASE_NOTES.md` prepared for `v1.0.0`.
- [x] `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, and `SECURITY.md` included.
- [x] GitHub Issue templates (`bug_report.md`, `feature_request.md`, `religious_content_accuracy.md`) and `pull_request_template.md` configured in `.github/`.
- [x] GitHub Actions Continuous Integration (`.github/workflows/android-ci.yml`) and Automated Tag Release (`.github/workflows/android-release.yml`) workflows configured.
- [x] ProGuard / R8 keep rules (`app/proguard-rules.pro`) and secret-safe `.gitignore` verified.
- [x] All unit and Robolectric tests executed and passing via `gradle :app:testDebugUnitTest`.
