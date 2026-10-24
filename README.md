# SalahMate (সালাহমেট) — Offline-First Islamic Companion for Android

[![License: MIT](https://img.shields.io/badge/License-MIT-0F4C81.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024--36)-3DDC84.svg)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg)](gradle/libs.versions.toml)
[![UI: Jetpack Compose + Material 3](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20M3-4285F4.svg)](app/src/main/java/com/example/MainActivity.kt)
[![Architecture: Offline-First](https://img.shields.io/badge/Architecture-Offline--First%20%7C%20Zero%20Tracking-10B981.svg)](PRIVACY.md)

**SalahMate** is a complete, privacy-respecting, offline-first Islamic Android application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **Room**, **DataStore**, **Retrofit/Moshi**, and the **Batoulapps Adhan** astronomical library.

It provides accurate local prayer times, opt-in Adhan notifications, a native home-screen widget, a full 114-Surah Quran reader with Bengali and English translations and audio recitation, Hifz memorization tracking, verified Hadith and Dua libraries, a sensor-fused Qibla compass, Digital Tasbih, Ramadan Suhoor/Iftar schedules, Hijri calendar events, a transparent Zakat calculator, daily prayer tracking, Islamic learning guides, and a Mosque finder—**without requiring user accounts, cloud servers, or Firebase**.

---

## Table of Contents
1. [Visual Identity & Localization](#1-visual-identity--localization)
2. [Complete Module Matrix](#2-complete-module-matrix)
3. [System Architecture](#3-system-architecture)
4. [Prayer Calculation Accuracy & Bangladesh Timetables](#4-prayer-calculation-accuracy--bangladesh-timetables)
5. [Getting Started & Building from Source](#5-getting-started--building-from-source)
6. [Running Automated Tests](#6-running-automated-tests)
7. [GitHub Releases & CI/CD Workflows](#7-github-releases--cicd-workflows)
8. [Documentation Index](#8-documentation-index)
9. [License & Religious Content Attribution](#9-license--religious-content-attribution)

---

## 1. Visual Identity & Localization

- **Sapphire Blue, Brushed Silver & Crisp Alabaster Theme**: Original Islamic architectural aesthetic supporting both **Light Mode** (`#0F4C81` Royal Sapphire, `#475569` Brushed Silver, `#F8FAFC` Crisp Alabaster) and **Dark Mode** (`#58A6FF` Luminous Sapphire, `#CBD5E1` Metallic Silver, `#09111E` Midnight Slate).
- **Bundled Typography**: Uses locally bundled Google Fonts (`Plus Jakarta Sans` for geometric headings/numerals and `Amiri` for vocalized classical Arabic script) so typography renders identically offline without network font-provider failures.
- **Instant Bilingual Switch (`English` / `বাংলা`)**: Defaults to English on first launch with a one-tap top-bar toggle to Bengali (`বাংলা`), while preserving right-to-left (RTL) vocalized Arabic script with user-adjustable font sizing (`20sp`–`44sp`).
- **Adaptive Layouts**: Automatically switches between a bottom `NavigationBar` on compact phones and a side `NavigationRail` on tablets, foldables, and landscape displays (`>= 600.dp`).

---

## 2. Complete Module Matrix

| Module | Offline Support | Primary Data Source / Engine | Key Capabilities |
| :--- | :---: | :--- | :--- |
| **First-Launch Onboarding** | 100% Offline | Jetpack DataStore + Preset Catalog | Prompts user to pick city (Bangladesh & global presets, manual Lat/Lon, or optional GPS), calculation authority, and Asr school. |
| **Home Dashboard** | 100% Offline | `LocalAdhanCalculator` + Room | Sapphire hero banner, Gregorian & Hijri dates, current/next prayer, isolated 1-second countdown, 5-prayer schedule, Sunrise/Sunset/Imsak/Midnight, last-read Quran card, and quick-access hub. |
| **Prayer Times Engine** | Offline + Optional Online | `com.batoulapps.adhan` + `AlAdhan.com` API | Calculates Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Imsak, Midnight, and Last Third. Supports separate Asr Madhab (`Standard` vs. `Hanafi`), per-prayer minute offsets (`-60` to `+60`m), and cached online comparison. |
| **Prayer Notifications & Adhan** | 100% Offline | `AlarmManager` + `BroadcastReceiver` | Per-prayer switches, System Tone / Synthesized Adhan Chime / Silent modes, advance reminders (`5`–`30`m), Snooze (`10m`) & Dismiss actions, live Test/Preview button, and reboot/timezone rescheduling. |
| **Home-Screen Widget** | 100% Offline | `SalahMateWidgetProvider` (`AppWidget`) | Native Android widget supporting compact and expanded sizes with active/next prayer, Hijri date, and 5-prayer timetable. |
| **Quran Reader & Audio** | Bundled + Downloadable | `AlQuran.cloud` API + Room + `MediaPlayer` | All 114 Surahs & 30 Juz index, vocalized Uthmani Arabic (`quran-uthmani`), Bengali (`bn.bengali`), English (`en.sahih`), Alafasy audio (`ar.alafasy`), bookmarks, search, copy/share, and offline Surah Download Manager. |
| **Hifz Tracker** | 100% Offline | Room (`hifz_progress`) | Manual Surah/Ayah memorization status (`LEARNING`, `NEEDS_REVISION`, `MEMORIZED`), review schedule, and Tajweed notes (never auto-marked by audio). |
| **Hadith Library** | Offline + Optional API | Verified Bundled Dataset + `Sunnah.com` API | Curated Hadith from *Sahih al-Bukhari*, *Sahih Muslim*, *Riyad as-Salihin*, and *40 Hadith Nawawi* with book/number/grading attribution, search, and bookmarks. |
| **Dua & Daily Adhkar** | 100% Offline | Verified Bundled Dataset (*Hisnul Muslim*) | Morning/Evening, Sleep/Waking, Prayer, Forgiveness, Travel, Eating, and Protection supplications with Arabic, transliteration, Bengali/English meanings, and completion tracking. |
| **Qibla Compass** | 100% Offline | `com.batoulapps.adhan.Qibla` + Sensors | Live rotating compass rose (`TYPE_ROTATION_VECTOR` / `ACCELEROMETER` + `MAGNETIC_FIELD`), numeric bearing fallback, distance to Kaaba (km), and Figure-8 calibration alerts. |
| **Digital Tasbih** | 100% Offline | Room (`tasbih_sessions`) + Haptics | Tactile tap-to-count button with vibration feedback, preset & custom Dhikr phrases, target counts (`33`, `34`, `100`, `1000`), reset confirmation, and lifetime totals. |
| **Ramadan, Suhoor & Iftar** | 100% Offline | `LocalAdhanCalculator` | 30-day projection clearly distinguishing **Imsak (precautionary margin)**, **Fajr (fast begins)**, and **Maghrib / Iftar (fast breaks)** with local offset controls. |
| **Hijri Calendar & Events** | 100% Offline | Kuwaiti / Tabular Hijri Algorithm | Gregorian/Hijri date conversion, moon-sighting day offset (`-3` to `+3` days), and major Islamic events with moon-sighting disclaimers. |
| **Zakat Calculator** | 100% Offline | `ZakatCalculatorEngine` | Editable local Gold/Silver prices per gram, Silver (`612.36g`) vs. Gold (`87.48g`) Nisab selector, itemized assets & deductible debts, and transparent `2.5%` Zakat breakdown. |
| **Prayer Tracker** | 100% Offline | Room (`prayer_logs`) | Private daily 5-prayer completion checklist, historical log, and personal gratitude reflections. |
| **Islamic Learning** | 100% Offline | Verified Bundled Dataset | Asma ul Husna, The Four Kalimas, Ayatul Kursi phrase study, and Step-by-Step Wudu (Ablution) guide with primary citations. |
| **Mosque Finder** | Intent Launcher | Android Geo Intent / OpenStreetMap | Launches installed map app for nearby Masjids around the selected city/coordinates without background location tracking. |
| **Settings & JSON Backup** | 100% Offline | Storage Access Framework (`SAF`) | Language/Theme controls, offline storage usage summary, and full JSON export/import of user bookmarks, Hifz, prayer logs, and Tasbih sessions. |

---

## 3. System Architecture

```
┌───────────────────────────────────────────────────────────────────────────────────┐
│                        UI LAYER (Jetpack Compose + Material 3)                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐  │
│  │ Home & Prayer   │ │ Quran Reader &  │ │ Worship & Study │ │ Tools, Calendar │  │
│  │ Schedule Screen │ │ Audio / Hifz    │ │ (Dua/Hadith/Ilm)│ │ Qibla & Zakat   │  │
│  └────────┬────────┘ └────────┬────────┘ └────────┬────────┘ └────────┬────────┘  │
│           └───────────────────┴─────────┬─────────┴───────────────────┘           │
│                                         ▼                                         │
│                     SalahMateViewModel (StateFlow + UI Events)                    │
└─────────────────────────────────────────┬─────────────────────────────────────────┘
                                          │
┌─────────────────────────────────────────▼─────────────────────────────────────────┐
│                         DOMAIN & REPOSITORY LAYER                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌─────────────────────────┐  │
│  │ Prayer & Qibla       │  │ Quran, Audio &       │  │ Hadith, Dua, Zakat,     │  │
│  │ Repository           │  │ Offline Download Mgr │  │ Hifz & Tracker Repo     │  │
│  └───────┬──────┬───────┘  └───────┬──────┬───────┘  └────────────┬────────────┘  │
└──────────┼──────┼──────────────────┼──────┼───────────────────────┼───────────────┘
           │      │                  │      │                       │
┌──────────▼──┐ ┌─▼──────────────┐ ┌─▼──────▼───────┐ ┌─────────────▼───────────────┐
│ Local Adhan │ │ AlAdhan REST   │ │ AlQuran.cloud  │ │ Room DB + DataStore +       │
│ Engine +    │ │ API (Retrofit) │ │ API + Media    │ │ Verified Bundled JSON       │
│ Sensors/GPS │ │ + Room Cache   │ │ Player + Files │ │ + AlarmManager + AppWidget  │
└─────────────┘ └────────────────┘ └────────────────┘ └─────────────────────────────┘
```

---

## 4. Prayer Calculation Accuracy & Bangladesh Timetables

1. **Method 1 = University of Islamic Sciences, Karachi**: In both SalahMate's local engine and the `AlAdhan.com` API (`method=1`), Method 1 uses Fajr angle `18.0°` and Isha angle `18.0°`, the standard astronomical authority used across Bangladesh and South Asia.
2. **Separate Asr Juristic School (`Madhab`)**: Asr timing is configured independently of the calculation authority:
   - **Standard (`school=0`)**: Shafi'i, Maliki, and Hanbali (shadow length factor = `1x` object height + noon shadow).
   - **Hanafi (`school=1`)**: Hanafi school (shadow length factor = `2x` object height + noon shadow, resulting in a later Asr time).
3. **Imsak vs. Fajr vs. Maghrib / Iftar**:
   - **Imsak**: A precautionary margin before Fajr (default `-10 minutes`, user-adjustable).
   - **Fajr**: True dawn when obligatory fasting begins and Fajr prayer enters.
   - **Maghrib / Iftar**: Sunset when obligatory fasting ends and Maghrib prayer enters.
4. **Local Mosque Timetable Verification**: Astronomical calculations can differ by 1–3 minutes from local mosque or Islamic Foundation timetables due to atmospheric refraction, elevation, or local precautionary margins. Users can apply individual `-60` to `+60` minute adjustments per prayer in the **Prayer** tab.

---

## 5. Getting Started & Building from Source

### Prerequisites
- **JDK**: Java 17 or Java 21
- **Android SDK**: API Level 36 (`minSdk = 24`, `targetSdk = 36`)
- **Environment File**: `.env.example` is tracked in git and provides safe placeholders.

### Clone & Build
```bash
git clone https://github.com/<your-username>/salahmate.git
cd salahmate

# Optional: Copy .env.example to .env if configuring an optional Sunnah.com API key
cp .env.example .env

# Assemble Debug APK
gradle assembleDebug
```
The compiled debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 6. Running Automated Tests

SalahMate includes deterministic JUnit and Robolectric tests verifying Bangladesh multi-city prayer calculations, Standard vs. Hanafi Asr separation, per-prayer minute offsets, post-Isha midnight countdown wrap-around, Qibla bearing math, Zakat Nisab calculations, Room database persistence, and JSON backup export/import round-trips:

```bash
gradle :app:testDebugUnitTest
```

See [`TESTING.md`](TESTING.md) for detailed test case documentation.

---

## 7. GitHub Releases & CI/CD Workflows

This repository includes two automated GitHub Actions workflows in [`.github/workflows/`](.github/workflows/):
- **[`android-ci.yml`](.github/workflows/android-ci.yml)**: Runs automatically on every push and pull request to `main` / `master` to execute the unit/Robolectric test suite and assemble the Debug APK.
- **[`android-release.yml`](.github/workflows/android-release.yml)**: Triggers automatically whenever you push a version tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`) or trigger a manual workflow dispatch. It builds the APK and AAB artifacts, computes `SHA256SUMS.txt`, and publishes a GitHub Release with [`RELEASE_NOTES.md`](RELEASE_NOTES.md).

---

## 8. Documentation Index

- [`BUILD.md`](BUILD.md) — Environment setup, Gradle commands, and signing configuration.
- [`API_SOURCES.md`](API_SOURCES.md) — Endpoint specifications and fallback architecture for Adhan, AlAdhan, AlQuran.cloud, and Sunnah.com.
- [`CONTENT_LICENSES.md`](CONTENT_LICENSES.md) — Open-source software licenses and religious text/audio attribution requirements.
- [`PRIVACY.md`](PRIVACY.md) — Complete privacy disclosure, permission usage, and offline data storage guarantees.
- [`TESTING.md`](TESTING.md) — Automated test coverage and Bangladesh prayer timetable comparison notes.
- [`RELEASE_CHECKLIST.md`](RELEASE_CHECKLIST.md) — Pre-release verification gates.
- [`RELEASE_NOTES.md`](RELEASE_NOTES.md) — Release notes for `v1.0.0`.
- [`CHANGELOG.md`](CHANGELOG.md) — Version history following *Keep a Changelog*.
- [`CONTRIBUTING.md`](CONTRIBUTING.md) — Developer guidelines and religious content verification rules.
- [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md) — Community standards.
- [`SECURITY.md`](SECURITY.md) — Vulnerability reporting and API key security policy.

---

## 9. License & Religious Content Attribution

- **Source Code**: Licensed under the **[MIT License](LICENSE)**.
- **Religious Texts & Audio Recitations**: Quranic Arabic script (`quran-uthmani` via Tanzil / AlQuran.cloud), Bengali translation (*Muhiuddin Khan*), English translation (*Saheeh International*), audio recitation (*Mishary Rashid Alafasy*), and canonical Hadith/Dua texts remain subject to their respective attribution and verbatim integrity requirements detailed in [`CONTENT_LICENSES.md`](CONTENT_LICENSES.md).
