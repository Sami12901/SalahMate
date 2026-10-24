# Changelog

All notable changes to **SalahMate** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-10-24

### Added
- **First-Launch Location & Calculation Onboarding**: Interactive setup dialog allowing users to select their city (8 Bangladesh divisions + global presets, manual Latitude/Longitude, or optional one-tap GPS), calculation authority (highlighting *University of Islamic Sciences, Karachi — Method 1*), and Asr Juristic School (*Standard* vs. *Hanafi*).
- **Home Dashboard**: Sapphire Blue & Brushed Silver hero banner displaying Gregorian and adjusted Hijri dates, current/next prayer status, isolated 1-second countdown timer, today's 5 daily prayers, Sunrise/Sunset/Imsak/Islamic Midnight indicators, last-read Quran card, and responsive quick-access grid.
- **Dual-Provider Prayer Engine**:
  - Offline primary calculation engine powered by `com.batoulapps.adhan:adhan:1.2.1`.
  - Optional online provider integration with `AlAdhan.com` REST API (`v1/timingsByCity` and `v1/timings`) backed by local Room caching (`cached_prayer_days`) and side-by-side comparison diffs.
  - Per-prayer manual minute adjustments (`-60` to `+60` minutes) for Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, and Imsak.
- **Opt-In Prayer Notifications & Adhan Alerts**:
  - Individual enable/disable switches for Fajr, Dhuhr, Asr, Maghrib, and Isha.
  - System Notification Tone, Synthesized Melodic Adhan Chime, and Silent Visual Reminder modes.
  - Advance reminder minutes (`0`, `5`, `10`, `15`, `30` min), Snooze (`+10m`) and Dismiss notification actions, and a live **Preview & Test Notification** button.
  - Automatic alarm rescheduling on `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, and `MY_PACKAGE_REPLACED`.
- **Native Home-Screen Widget (`SalahMateWidgetProvider`)**: Compact and expanded widget layouts displaying current/next prayer, Hijri date, and today's 5-prayer schedule.
- **Full 114-Surah Quran Reader & Audio Recitation**:
  - Complete 114-Surah and 30-Juz navigation index.
  - Pre-bundled offline essential Surahs (*Al-Fatihah*, *Al-'Asr*, *Al-Kawthar*, *Al-Ikhlas*, *Al-Falaq*, *An-Nas*) for immediate cold-start offline reading.
  - Multi-edition fetching and caching from `AlQuran.cloud` (`quran-uthmani` Arabic, `bn.bengali` Muhiuddin Khan, `en.sahih` Saheeh International, and `ar.alafasy` Mishary Rashid Alafasy audio).
  - Persistent audio playback bar with Play/Pause/Stop and continuous Ayah-by-Ayah Surah playback.
  - Bookmarks, reading progress history, offline full-text search, copy/share, and user-controlled Offline Surah Download Manager.
- **Quran Memorization / Hifz Tracker**: Room-persisted Surah and Ayah memorization tracking (`LEARNING`, `NEEDS_REVISION`, `MEMORIZED`), review schedule dates, and personal Tajweed notes.
- **Hadith Library & Daily Adhkar**:
  - Verified offline Hadith collection (*Sahih al-Bukhari*, *Sahih Muslim*, *Riyad as-Salihin*, *40 Hadith Nawawi*) with Arabic text, Bengali/English translations, narrator, primary source reference, and authenticity grading, plus optional live `Sunnah.com` API sync.
  - Categorized Dua & Daily Adhkar (*Hisnul Muslim* / *Sahihain*) with vocalized Arabic, transliteration, Bengali/English meanings, and completion tracking.
- **Qibla Compass**: Live sensor-fused compass rose (`TYPE_ROTATION_VECTOR` / `ACCELEROMETER` + `MAGNETIC_FIELD`), numeric bearing toggle, Haversine distance to Makkah, and Figure-8 calibration guidance.
- **Digital Tasbih**: Tap-to-count interface with haptic vibration feedback, preset and custom Dhikr phrases, target presets, reset confirmation dialog, and lifetime counts.
- **Ramadan, Hijri Calendar, Zakat Calculator, Prayer Tracker, Islamic Learning, Mosque Finder & JSON Backup/Restore**.
