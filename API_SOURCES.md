# External APIs & Calculation Sources (`API_SOURCES.md`)

## 1. Local Astronomical Engine (Primary)
- **Library**: `com.batoulapps.adhan:adhan:1.2.1`
- **Capabilities**: Calculates Fajr, Sunrise, Dhuhr, Asr (Standard Shafi'i 1x shadow & Hanafi 2x shadow), Maghrib, Isha, Middle of the Night, Last Third of the Night, and spherical Qibla azimuth completely offline.

## 2. AlAdhan Prayer Times API (Optional Online Comparison)
- **Base URL**: `https://api.aladhan.com/v1/`
- **Endpoints Used**:
  - `GET v1/timingsByCity/{DD-MM-YYYY}?city={city}&country={country}&method={method}&school={school}`
  - `GET v1/timings/{DD-MM-YYYY}?latitude={lat}&longitude={lon}&method={method}&school={school}`
- **Accuracy Notice**: Method `1` corresponds to *University of Islamic Sciences, Karachi*. `school=0` is Standard (Shafi'i) and `school=1` is Hanafi. Results are cached in Room (`cached_prayer_days`) for offline fallback.

## 3. AlQuran.cloud API (Quran Text, Translations & Recitation)
- **Base URL**: `https://api.alquran.cloud/v1/`
- **Endpoint Used**: `GET v1/surah/{number}/editions/quran-uthmani,bn.bengali,en.sahih,ar.alafasy`
- **Editions**:
  - `quran-uthmani`: Tanzil Uthmani vocalized Arabic script
  - `bn.bengali`: Muhiuddin Khan Bengali translation
  - `en.sahih`: Saheeh International English translation
  - `ar.alafasy`: Mishary Rashid Alafasy 128kbps MP3 recitation

## 4. Sunnah.com API (Optional Remote Hadith Provider)
- **Base URL**: `https://api.sunnah.com/v1/`
- **Authentication**: Requires `X-API-Key` header via `BuildConfig.SUNNAH_API_KEY`. When unconfigured or offline, SalahMate uses its verified local Hadith dataset.
