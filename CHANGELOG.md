# Changelog

All notable changes to **SalahMate** are documented in this file.

## [2.2.0] - 2026-10-26

### Added (P0 — Reliability, Data Safety & Security)
- **Cross-Process Notification Deduplication**: Added `salahmate_alarm_dedup.xml` tracking `(dateIso, prayerName, isAdvanceReminder)` so `AlarmManager` exact alarms and `WorkManager` 30-minute fallback verification never emit duplicate prayer notifications.
- **Broadcast Action Authentication**: Added `EXTRA_INTERNAL_TOKEN` verification (`PrayerNotificationScheduler.verifyInternalAlarmToken`) and explicit `setPackage(context.packageName)` on all `PendingIntent`s to protect `PrayerAlarmReceiver` from external intent spoofing.
- **Cross-Midnight Prayer Rollover**: Fixed `LocalAdhanCalculator.computeNextPrayerStatus()` and `SalahMateViewModel` ticker so times after `00:00` before Fajr automatically resolve against the active calendar day.
- **PBKDF2-HMAC-SHA256 + AES-256-GCM Encrypted Backups**: Added password-protected authenticated encryption (65,536 iterations, 128-bit salt, 96-bit GCM IV, 128-bit authentication tag) alongside plaintext JSON export with explicit privacy notice.
- **Transactional Restore & Live Preview Modal**: Added `UserDataBackupManager.inspectBackupPayload()` and atomic `db.withTransaction` restore across all 5 user tables (`quran_bookmarks`, `hifz_progress`, `prayer_logs`, `tasbih_sessions`, `quran_reading_history`) and DataStore preferences, with duplicate bookmark detection.
- **Hardened Android Backup Rules**: Configured `backup_rules.xml` and `data_extraction_rules.xml` to back up `salahmate_offline.db` and `datastore/` while excluding transient alarm deduplication state.
- **Database Safety**: Removed `fallbackToDestructiveMigration()` from `SalahMateDatabase`.

### Added (P1 — Quran, Hifz & Bangladesh Localization)
- **Mosque Jama'ah (Congregation) Timetable**: Added per-prayer congregation minute offsets (`Fajr`, `Dhuhr`, `Asr`, `Maghrib`, `Isha`) kept strictly distinct from calculated solar Waqt and Imsak, displayed on both the Home Dashboard and Prayer Times screen.
- **Quran Audio Repeat & Ayah Range Loop**: Added `1x`, `3x`, `5x`, and `∞ Infinite` per-Ayah repeat plus configurable `Start..End` Ayah range looping in `QuranRepository` and `QuranReaderScreen`.
- **Hifz Spaced-Revision Interval Planner**: Added selectable review intervals (`1d`, `3d`, `7d`, `14d`, `30d`) in `HifzTrackerScreen` with automatic due-for-review filtering.

### Added (P2 — Worship Analytics, Offline Flashcards Quiz & Worship Reminders)
- **Personal Worship Analytics (7-Day & 30-Day)**: Added 7-day and 30-day prayer completion progress bars, Quran reading session counters, and memorized Surah totals in `PrayerTrackerScreen`.
- **Offline Arabic-Bengali Quranic Vocabulary Flashcards & Quiz**: Added 15 high-frequency Quranic words with interactive flip cards, Quranic references, and a self-scoring multiple-choice quiz in `IslamicLearningScreen`.
- **Independent Worship Reminders**: Added dedicated alarm scheduling and toggles for Morning Adhkar (`06:30 AM`), Evening Adhkar (`05:15 PM`), and Friday Surah Al-Kahf (`09:30 AM`).
