# SalahMate v1.0.0 — Release Notes

**SalahMate v1.0.0** is the initial public release of the offline-first, privacy-respecting Islamic companion app for Android (`Android 7.0 Nougat / API 24` through `Android 16 / API 36`).

---

## Highlights in v1.0.0

### 1. Accurate, Transparent Prayer Times (Offline + Online Comparison)
- **Offline Astronomical Engine**: Powered locally by the Batoulapps Adhan library (`com.batoulapps.adhan:adhan:1.2.1`) with zero internet dependency.
- **First-Launch Setup**: Select from 8 Bangladesh cities (Dhaka, Chattogram, Sylhet, Rajshahi, Khulna, Barishal, Rangpur, Mymensingh), global cities, manual Latitude/Longitude, or optional one-tap GPS.
- **Separate Authority & Madhab Controls**: Choose your calculation authority (including *University of Islamic Sciences, Karachi — Method 1*) independently from your Asr Juristic School (*Standard Shafi'i/Maliki/Hanbali* vs. *Hanafi*).
- **Per-Prayer Minute Adjustments**: Fine-tune Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, and Imsak (`-60` to `+60` min) to match your local congregation's Jama'ah schedule.
- **Optional AlAdhan API Mode**: Fetch, cache, and compare schedules against `api.aladhan.com`.

### 2. Opt-In Prayer Notifications & Native Home-Screen Widget
- Individual toggles for Fajr, Dhuhr, Asr, Maghrib, and Isha with advance reminder options (`5`–`30` min before prayer).
- Choose between System Notification Tone, Synthesized Adhan Chime, or Silent Reminder mode, with Snooze (`+10m`), Dismiss, and a live **Preview & Test Notification** button.
- Resizable **Home-Screen Widget** (`Compact` & `Expanded`) showing the active/next prayer, Hijri date, and today's 5-prayer timetable.

### 3. Full 114-Surah Quran Reader, Recitation & Hifz Tracker
- Vocalized Uthmani Arabic script (`Amiri` font with adjustable size `20sp`–`44sp`) paired with toggleable **Bengali** (*Muhiuddin Khan*) and **English** (*Saheeh International*) translations.
- Stream or cache **Sheikh Mishary Rashid Alafasy** audio recitation (`ar.alafasy`) with continuous Ayah-by-Ayah Surah playback.
- Offline Surah Download Manager, Ayah bookmarks, search, and a manual Room-backed **Hifz Memorization Tracker**.

### 4. Complete Daily Worship & Utility Suite
- **Hadith Library & Daily Adhkar**: Verified narrations from *Sahih al-Bukhari*, *Sahih Muslim*, *Riyad as-Salihin*, *40 Hadith Nawawi*, and *Hisnul Muslim* with full source and grading references.
- **Qibla Compass**: Live rotating compass rose + numeric bearing mode (`277.6° W` from Dhaka) and distance to the Kaaba.
- **Digital Tasbih**: Tactile counter with haptic feedback, custom Dhikr phrases, and persistent session history.
- **Ramadan & Hijri Calendar**: Clear distinction between *Imsak*, *Fajr*, *Maghrib*, and *Iftar*, plus moon-sighting day adjustment (`-3` to `+3` days).
- **Transparent Zakat Calculator**: Editable local Gold/Silver prices per gram, Silver (`612.36g`) and Gold (`87.48g`) Nisab standards, and itemized `2.5%` Zakat calculation.
- **Local JSON Backup & Restore**: Export and import your personal bookmarks, Hifz logs, prayer tracker history, and Tasbih counts using Android's system file picker.

---

## Installation & Verification

1. Download `SalahMate-v1.0.0-debug.apk` (or signed release APK) and `SHA256SUMS.txt` from the **Assets** section below.
2. Verify the APK checksum before installing:
   ```bash
   sha256sum -c SHA256SUMS.txt
   ```
3. Transfer the APK to your Android device (`Android 7.0+`) and install it.

---

## Known Platform Notes & Disclaimers
- **Local Timetable Verification**: Astronomical prayer times are mathematical estimates and may differ by 1–3 minutes from local mosque or government timetables. Always verify fasting and prayer times against a trusted local schedule.
- **Android 12+ Exact Alarms & Battery Optimization**: If your device restricts `SCHEDULE_EXACT_ALARM` or applies aggressive OEM battery savers, SalahMate automatically falls back to windowed alarms and displays a diagnostic status banner in the **Prayer** tab.
- **Compass Sensors**: Devices or emulators without a hardware magnetometer automatically display the exact numeric Qibla bearing relative to True North.
