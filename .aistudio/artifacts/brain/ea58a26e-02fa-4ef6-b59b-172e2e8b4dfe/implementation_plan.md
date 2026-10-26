# SalahMate — Production-Quality Audit & Full P0–P2 Enhancement Plan

This plan details the complete repository audit findings and the unified P0 (Reliability, Data Safety & Security), P1 (Quran Audio Repeat/Range, Hifz Spaced Revision & Bangladesh Localization), and P2 (Personal Worship Analytics, Arabic-Bengali Flashcards Quiz, Expanded Zakat Worksheet & Independent Worship Reminders) upgrade for **SalahMate** by **Mohammad Sami Islam**.

---

## User Review & Confirmed Decisions

> [!IMPORTANT]
> All core architectural and product decisions below reflect your confirmed choices and preserve 100% of existing user data, 4 Islamic heritage color themes, and offline-first privacy guarantees.

- **Confirmed Decision 1 — Unified P0 + P1 + P2 Delivery**: Deliver the complete P0 reliability/security fixes, P1 Quran/Hifz/Bangladesh localization upgrades, and P2 worship analytics, flashcards quiz, and reminders together in one verified, tested release.
- **Confirmed Decision 2 — Encrypted + Plaintext Transactional Backup with Restore Preview**: Support both password-protected **PBKDF2-HMAC-SHA256 + AES-256-GCM** authenticated encrypted backups and plaintext JSON exports (with a clear privacy warning), coupled with pre-restore validation, duplicate record detection, a live **Restore Preview Modal**, and atomic Room database transactions (`withTransaction`) that roll back cleanly on any error.
- **Confirmed Decision 3 — Full P1 & P2 Feature Suite**:
  - **Mosque Jama'ah Timetable Separation**: Keep solar-calculated Waqt start times and precautionary Imsak distinct from user-configurable local Mosque Jama'ah (congregation) times.
  - **Quran Repeat/Range Loop & Hifz Spaced-Revision Planner**: Add configurable Ayah repeat (`1x`, `3x`, `5x`, `Infinite`) and custom Ayah range looping (`Start Ayah .. End Ayah`) alongside a spaced-repetition Hifz review planner (`1d`, `3d`, `7d`, `14d`, `30d` intervals) and daily Quran reading goals.
  - **Offline Arabic-Bengali Flashcards Quiz & Weekly Worship Analytics**: Add an interactive Quranic vocabulary flashcard deck and self-test quiz in the Learning module, plus a 7-day/30-day Personal Worship Analytics dashboard.
  - **Independent Worship Reminders**: Add independent, opt-in schedules for Morning Adhkar, Evening Adhkar, and Friday Surah Al-Kahf reminders alongside the 5 daily prayer alarms.

---

## Repository Audit Summary & Identified Defects

| Severity | Area | Root Cause Identified During Audit | Planned Fix & Regression Test |
| :--- | :--- | :--- | :--- |
| **P0 Critical** | **Broadcast Receiver Security** | `PrayerAlarmReceiver` is exported in the manifest and accepts both system boot/time broadcasts and internal app alarm/action broadcasts without caller verification, allowing external apps to spoof prayer completion or trigger fake alarms. | Split system boot/time/package-replace events from private internal alarm/notification actions, scope internal `PendingIntent`s explicitly to the app package, and validate an internal integrity token on alarm/action intents. |
| **P0 High** | **Cross-Process Alarm + Worker Deduplication** | `PrayerNotificationScheduler` stores fired slot timestamps only in an in-memory `ConcurrentHashMap`. If Android reclaims the process between an `AlarmManager` broadcast and a `WorkManager` fallback job, the notification can fire twice. | Persist fired slot keys (`prayerType|isAdvance|dateIso`) in persistent preferences so both `AlarmManager` and `WorkManager` share durable deduplication across process restarts. |
| **P0 High** | **Midnight Date Rollover in Active Session** | The 1-second countdown ticker recalculates `NextPrayerStatus` against the cached `_todaySchedule`, which does not automatically roll over to the new calendar date when the clock crosses `00:00` while the app is open. | Compare the active timezone's current `yyyy-MM-dd` against `_todaySchedule.value.dateIso` in the ticker and automatically recalculate the schedule and alarms across midnight boundaries. |
| **P0 High** | **Non-Transactional & Incomplete Backup Import** | Backup export/import omits `tasbihSessions` on import, omits `favoriteItems`, `readingHistory`, and user preferences, can duplicate bookmarks on repeated imports, and lacks atomic rollback on malformed JSON. | Upgrade backup schema to v3 (while supporting v1/v2 imports), run imports inside `db.withTransaction`, deduplicate bookmarks by `(surahNumber, ayahNumber)`, include preferences/favorites/history, add a Restore Preview step, and add optional AES-256-GCM encryption. |
| **P1 Medium** | **Waqt vs. Mosque Jama'ah Distinction** | Users in Bangladesh often need both the astronomical Waqt start time and their local mosque's Jama'ah (congregation) time side by side. | Add optional per-prayer Mosque Jama'ah offset/time configuration and display both **Waqt Start** and **Mosque Jama'ah** clearly in the Prayer schedule and Home cards. |
| **P1 Medium** | **Quran Audio Repeat & Hifz Interval Scheduling** | Audio playback plays once or continuously to the end of the Surah without single-Ayah repeat or range looping for memorization, and Hifz review dates are not automatically scheduled by spaced intervals. | Add Ayah repeat count (`1x`, `3x`, `5x`, `∞`) and range loop (`fromAyah..toAyah`) controls in the audio player, plus one-tap Hifz spaced-revision interval scheduling (`1`, `3`, `7`, `14`, `30` days). |

---

## 1. Overview & Core Concept

- **What It Does**: Evolves SalahMate into a hardened, production-grade offline Islamic companion for Bangladesh and global Muslims—combining astronomically verified prayer & Jama'ah schedules, failsafe offline notifications, authenticated encrypted backups, memorization-grade Quran audio looping, and interactive Arabic-Bengali learning tools.
- **Target Audience**: Users seeking a trustworthy, zero-ad, zero-tracking, 100% open-source Islamic companion that works reliably offline on any phone or tablet.
- **Key Value**: Eliminates duplicate or missed notifications, guarantees zero data loss during upgrades or backup restores, separates calculated solar Waqt from local Mosque Jama'ah times, and empowers daily Quran Hifz and worship consistency.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Prayer & Mosque Jama'ah Flow**: On the Home and Prayer screens, each obligatory prayer row displays the **Calculated Waqt Start**, an optional **Mosque Jama'ah Badge** (configurable per prayer in minutes after Waqt or fixed local offset), and individual **Advance + Waqt Alarm** statuses.
  2. **Quran Repeat & Hifz Revision Flow**: In the Quran Reader, a collapsible **Memorization Loop Bar** lets users pick Repeat Count (`1x`, `3x`, `5x`, `Loop`) and Ayah Range (`Ayah X to Ayah Y`). In the Hifz Tracker, marking a Surah/passage reviewed automatically computes the next review date using user-adjustable spaced intervals (`1d`, `3d`, `7d`, `14d`, `30d`) and tracks daily reading goals.
  3. **Encrypted Backup & Restore Preview Flow**: In Settings & Backup, users can export either **Plaintext JSON** (with a clear privacy notice) or **Password-Protected Encrypted Backup (`.smbak` / JSON envelope)** using PBKDF2 + AES-256-GCM. Selecting a backup file opens a **Restore Preview Dialog** showing exact record counts (Bookmarks, Hifz, Prayer Logs, Tasbih, Favorites, Preferences), schema version, and duplicate handling before committing atomically.
  4. **Worship Analytics, Flashcards Quiz & Reminders Flow**:
     - **Personal Worship Dashboard**: Visual 7-day and 30-day prayer completion rate, Quran reading goal progress, and Dhikr streak cards.
     - **Arabic-Bengali Flashcards & Quiz**: Interactive flip cards and a 4-option multiple-choice revision quiz for high-frequency Quranic words and daily Duas.
     - **Independent Worship Reminders**: Toggleable Morning Adhkar, Evening Adhkar, and Friday Surah Al-Kahf local reminders.
- **Visual Identity & Theme**:
  - Preserves all 4 handcrafted Islamic heritage themes (**Sacred Madinah Emerald**, **Kaaba Midnight Gold**, **Al-Aqsa Sapphire**, **Desert Rose Parchment**) across Light, Dark, and System modes.
  - Uses responsive `BoxWithConstraints` and `FlowRow` layouts so chips, badges, and dialogs never clip on compact 320dp phones or 840dp+ tablets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1 — Cross-Process Notification Deduplication & Broadcast Hardening**:
  - *Chosen Approach*: Persist fired notification slot keys in DataStore/SharedPreferences and restrict internal alarm/action intents to package-scoped `PendingIntent`s with an internal verification token.
  - *Why*: Prevents duplicate notifications when Android kills the app process between `AlarmManager` and `WorkManager` runs, and blocks malicious third-party apps from spoofing prayer completion broadcasts.
- **Decision 2 — PBKDF2-HMAC-SHA256 + AES-256-GCM Authenticated Encryption for Backups**:
  - *Chosen Approach*: Use standard Android/Java `SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")` (65,536 iterations, random 16-byte salt) and `Cipher.getInstance("AES/GCM/NoPadding")` (12-byte IV, 128-bit authentication tag) encoded in a versioned JSON envelope, while keeping plaintext JSON export available with a clear privacy warning.
  - *Why*: Provides cryptographic confidentiality and tamper detection using built-in platform primitives without adding bloated third-party crypto dependencies.
- **Decision 3 — Non-Destructive Room Migrations & Transactional Restore**:
  - *Chosen Approach*: Preserve `MIGRATION_1_2` and add `MIGRATION_2_3` for any new columns/tables, never calling `fallbackToDestructiveMigration()`, and execute all backup imports inside `db.withTransaction { ... }`.
  - *Why*: Guarantees zero data loss for existing users upgrading the app or importing backups.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI (4 Islamic Themes)                    │
│  ┌──────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐  │
│  │ Home & Prayer    │  │ Quran & Hifz Planner │  │ Worship, Flashcards, │  │
│  │ • Waqt vs Jamaah │  │ • Ayah Repeat/Range  │  │ Zakat & Encrypted    │  │
│  │ • Alarm Status   │  │ • Spaced Intervals   │  │ Backup Preview Modal │  │
│  └────────┬─────────┘  └──────────┬───────────┘  └──────────┬───────────┘  │
└───────────┼───────────────────────┼─────────────────────────┼──────────────┘
            ▼                       ▼                         ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     SalahMateViewModel (MVVM StateFlows)                   │
│  • Midnight Date-Rollover Detector   • Restore Preview & Crypto Coordinator│
│  • Waqt + Mosque Jama'ah Formatter   • Weekly/Monthly Worship Aggregator   │
└───────────┬───────────────────────┬─────────────────────────┬──────────────┘
            ▼                       ▼                         ▼
┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────────┐
│ Offline Alarm & Work │ │ Room DB (v2 -> v3)   │ │ DataStore Preferences    │
│ • Exact AlarmManager │ │ • MIGRATION_1_2, 2_3 │ │ • Calc Method & Madhab   │
│ • WorkManager Sync   │ │ • Atomic Transactions│ │ • Mosque Jama'ah Offsets │
│ • Persistent Dedup   │ │ • Bookmarks, Hifz,   │ │ • Worship Reminder Flags │
│ • Worship Reminders  │ │   Logs, Tasbih, Favs │ │ • Daily Quran Goal       │
└──────────────────────┘ └──────────────────────┘ └──────────────────────────┘
```

- **Data Model & State Enhancements**:
  - **Mosque Jama'ah Offsets & Worship Reminders (`UserPreferencesState`)**: Per-prayer Jama'ah offset minutes (`fajrJamaahOffsetMin`, `dhuhrJamaahOffsetMin`, `asrJamaahOffsetMin`, `maghribJamaahOffsetMin`, `ishaJamaahOffsetMin`), toggleable `showMosqueJamaahTimes`, `dailyQuranGoalAyahs`, and independent reminder toggles (`morningAdhkarReminderEnabled`, `eveningAdhkarReminderEnabled`, `jumuahKahfReminderEnabled`).
  - **Quran Audio Repeat & Range State (`QuranAudioPlaybackState`)**: `repeatCountPerAyah` (`1`, `3`, `5`, `0` for infinite), `currentRepeatIteration`, `rangeStartAyah`, and `rangeEndAyah`.
  - **Backup Envelope & Restore Preview (`BackupPreviewSummary`)**: Validates schema version (`1`, `2`, or `3`), detects encrypted vs. plaintext payloads, counts records by category, identifies duplicate bookmarks, and commits atomically inside `withTransaction`.
  - **Deterministic Unit & Robolectric Verification**: Full unit and Robolectric tests covering midnight prayer rollover, cross-process notification deduplication, PBKDF2 + AES-256-GCM encryption/decryption and tamper rejection, transactional backup restore, Hifz spaced-revision interval calculation, and Zakat custom market rate calculations.
