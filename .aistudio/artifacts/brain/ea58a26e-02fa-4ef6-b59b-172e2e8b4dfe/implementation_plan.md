# SalahMate — GitHub Publishing, Open-Source Governance & Automated Release Suite

This plan upgrades **SalahMate** into a complete, publication-ready open-source GitHub repository with a comprehensive showcase README, MIT licensing, community contribution and religious-content verification standards, security disclosure policies, structured release notes, ProGuard/R8 rules, and automated GitHub Actions CI/CD pipelines for testing and APK/AAB release publishing.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following choices have been confirmed from your clarifications for GitHub publishing and release management:

- **Confirmed Decision 1 — Open-Source Software License (`MIT License`)**: The repository will include a standard `LICENSE` file under the MIT License for the application source code, while clearly distinguishing the religious text/audio content attributions (Tanzil Uthmani Quran script, Muhiuddin Khan Bengali translation, Saheeh International English translation, Alafasy audio, and classical Hadith texts) in `CONTENT_LICENSES.md` and `README.md`.
- **Confirmed Decision 2 — Full GitHub Actions CI + Automated Release Workflows**: We will configure two production-grade GitHub Actions pipelines:
  1. **Continuous Integration (`android-ci.yml`)**: Runs on every push and pull request to `main`/`master` to validate `.env.example` setup, compile the debug APK, and run the full JUnit + Robolectric test suite (`testDebugUnitTest`) with uploaded test reports and APK artifacts.
  2. **Automated Tag Release (`android-release.yml`)**: Triggers on semantic version tags (`v*.*.*`, e.g., `v1.0.0`) or manual `workflow_dispatch` to build signed/release-ready APK and AAB artifacts, generate SHA-256 checksums, and publish a GitHub Release automatically using the structured release notes.
- **Confirmed Decision 3 — Complete Community Governance, Release Notes & Issue/PR Templates**: We will add `CONTRIBUTING.md` (with strict Islamic text verification rules), `CODE_OF_CONDUCT.md`, `SECURITY.md`, `CHANGELOG.md`, `RELEASE_NOTES.md`, GitHub Issue templates (Bug Report, Feature Request, Religious Text / Calculation Accuracy Report), a Pull Request template, and hardened `.gitignore` and `proguard-rules.pro` configurations.

---

### 1. Overview & Core Concept

- **What It Does**: Prepares SalahMate for public hosting on GitHub and reproducible binary distribution via GitHub Releases, F-Droid, or Google Play.
- **Target Audience / Persona**: Open-source contributors, Android developers, QA reviewers, and end users downloading verified APKs from GitHub Releases.
- **Key Value**: Gives visitors an immediate, trustworthy understanding of SalahMate's architecture, accuracy guarantees, privacy posture, build steps, and verification checklist while automating CI testing and release packaging.

---

### 2. User Experience & Repository Presentation

- **Showcase `README.md` Structure**:
  - **Badges & Identity Header**: Build/CI status, Android `minSdk 24` / `targetSdk 36`, Kotlin & Jetpack Compose badges, MIT License badge, and Offline-First / Zero-Tracking badge.
  - **Feature Matrix Table**: Complete breakdown of all 17 modules (Home Dashboard, Local Adhan + AlAdhan Prayer Engine, Opt-In Prayer Notifications, Home-Screen Widget, 114-Surah Quran Reader & Audio, Hifz Tracker, Hadith Library, Dua & Adhkar, Qibla Compass, Digital Tasbih, Ramadan Suhoor/Iftar, Hijri Calendar, Zakat Calculator, Prayer Tracker, Islamic Learning, Mosque Finder, and Local JSON Backup/Restore) with offline capability indicators.
  - **Architecture & Prayer Accuracy Section**: Explains the dual-provider prayer architecture, the separation of Calculation Method (Method 1 = Karachi) from Asr Juristic School (Standard vs. Hanafi), Imsak vs. Fajr vs. Maghrib/Iftar distinctions, and Bangladesh timetable comparison notes.
  - **Quick-Start, Build & Secrets Guide**: Step-by-step instructions for cloning, building, running Robolectric tests, and configuring optional `SUNNAH_API_KEY` secrets safely without committing `.env` or keystores.
- **Structured Release Documentation (`CHANGELOG.md`, `RELEASE_NOTES.md`, `RELEASE_CHECKLIST.md`)**:
  - Follows the *Keep a Changelog* and Semantic Versioning (`v1.0.0`) standards.
  - Provides ready-to-paste GitHub Release Notes including APK installation instructions, SHA-256 verification commands, known platform constraints (Android 12+ exact alarm permissions, emulator magnetometer fallback), and attribution notices.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Separate Code License (MIT) vs. Sacred Text Integrity Rules**
  - *Chosen Approach*: License all Kotlin/Compose source code under the permissive MIT License while explicitly mandating in `LICENSE`, `CONTENT_LICENSES.md`, and `CONTRIBUTING.md` that Quranic Arabic text (`quran-uthmani`) and canonical Hadith narrations must never be altered, truncated, or stripped of diacritics in forks or pull requests.
  - *Why*: Encourages open-source collaboration on the Android engineering while safeguarding religious text authenticity.

- **Decision 2 — Keystore-Safe GitHub Release Workflow**
  - *Chosen Approach*: Configure the GitHub Actions release workflow to support repository secrets (`KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) when present, while gracefully falling back to producing verifiable debug/unsigned release artifacts and SHA-256 checksums when secrets are not yet configured on a fresh fork.
  - *Why*: Ensures the workflow succeeds out-of-the-box as soon as the repository is pushed to GitHub.

---

### 4. Technical Architecture & Release Pipeline *(Technical Reference)*

- **GitHub CI/CD & Release Architecture Diagram**:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        GITHUB REPOSITORY & GOVERNANCE                        │
│  ┌────────────────────┐  ┌─────────────────────┐  ┌───────────────────────┐  │
│  │ Showcase README.md │  │ LICENSE (MIT) +     │  │ Issue & PR Templates  │  │
│  │ + Architecture Doc │  │ CONTENT_LICENSES.md │  │ + CONTRIBUTING / CoC  │  │
│  └─────────┬──────────┘  └──────────┬──────────┘  └───────────┬───────────┘  │
└────────────┼────────────────────────┼─────────────────────────┼──────────────┘
             │                        │                         │
             ▼                        ▼                         ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     GITHUB ACTIONS AUTOMATION PIPELINES                      │
│                                                                              │
│  [Push / Pull Request] ──► Continuous Integration Workflow                   │
│                            ├─ Setup JDK 21 + Android SDK + Gradle Cache      │
│                            ├─ Prepare .env from .env.example                 │
│                            ├─ Run Unit & Robolectric Suite (9/9 Tests)       │
│                            └─ Assemble Debug APK & Upload Test Reports       │
│                                                                              │
│  [Tag v*.*.* / Manual] ──► Release Packaging & Publishing Workflow           │
│                            ├─ Decode Signing Keystore (or Fallback)          │
│                            ├─ Apply R8 / ProGuard Rules (Retrofit/Moshi/Room)│
│                            ├─ Build Release APK + Android App Bundle (AAB)   │
│                            ├─ Generate SHA-256 Checksums                     │
│                            └─ Publish GitHub Release with RELEASE_NOTES.md   │
└──────────────────────────────────────────────────────────────────────────────┘
```

- **ProGuard / R8 & Security Hardening**:
  - **ProGuard Rules**: Keep rules for Retrofit interfaces, Moshi `@Json` DTOs (`AlAdhanResponse`, `AlQuranMultiEditionResponse`, `SunnahApiResponse`), Room `@Entity` and `@Dao` classes, and Batoulapps Adhan calculation classes so release minification never strips reflective or serialized fields.
  - **Git Ignore & Secret Protection**: Harden `.gitignore` to exclude `.env`, `*.jks`, `*.keystore`, release signing properties, and local build outputs so sensitive credentials can never be accidentally committed when pushing to GitHub.
