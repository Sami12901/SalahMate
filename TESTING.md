# Testing & Verification Documentation (`TESTING.md`)

## Automated Test Suite (`gradle :app:testDebugUnitTest`)
Our automated unit and Robolectric tests verify:
1. **Bangladesh Multi-City Prayer Calculations**: Validates Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha chronological ordering and values across Dhaka, Chattogram, Sylhet, and Rajshahi using University of Islamic Sciences, Karachi (Method 1).
2. **Asr Juristic School Separation**: Confirms that Hanafi Asr (`Madhab.HANAFI`) occurs later than Standard Shafi'i Asr (`Madhab.SHAFI`) while keeping Fajr, Dhuhr, Maghrib, and Isha identical.
3. **User Minute Adjustments & Imsak Separation**: Verifies per-prayer minute offsets (`+5m`, `-3m`) and confirms Imsak is distinctly calculated prior to Fajr (`-10m` default).
4. **Midnight Boundary Countdown Wrap-Around**: Verifies that after Isha, `computeNextPrayerStatus` rolls over to tomorrow's Fajr and formats `HH:MM:SS` accurately.
5. **Qibla Bearing & Distance**: Verifies Dhaka Qibla bearing (`~277.6° W`) and Haversine distance to Makkah.
6. **Zakat Calculation Scenarios**: Tests Silver (`612.36g`) vs. Gold (`87.48g`) Nisab thresholds, deductible debts, and below-Nisab zero payable edge cases.
7. **Room Persistence & JSON Backup Round-Trip**: Tests inserting and restoring Quran bookmarks, Hifz progress, prayer logs, and Tasbih counters via `RobolectricTestRunner`.

## Bangladesh Timetable Comparison Notes
Astronomical calculations using Karachi Method 1 + Hanafi school closely match Islamic Foundation Bangladesh schedules (typically within 1–3 minutes depending on seasonal refraction and precautionary margins). Users can apply `-60` to `+60` minute per-prayer offsets in the Prayer tab to match their local masjid's Jama'ah schedule.
