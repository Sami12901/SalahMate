# Privacy Policy (`PRIVACY.md`)

## Core Privacy Principles
SalahMate is designed as a **serverless, offline-first personal Islamic companion**.

1. **No Account or Cloud Requirement**: You never need to create an account, sign in, or subscribe to use SalahMate.
2. **Local Data Storage**: Your prayer completion logs, Hifz memorization progress, Quran bookmarks, reading history, Tasbih counters, and personal reflection notes are stored exclusively on your device inside the local Room database (`salahmate_offline.db`).
3. **Location Privacy**:
   - You can select any city or enter latitude/longitude manually **without granting GPS permission**.
   - If you choose the optional "Use GPS" button, `ACCESS_COARSE_LOCATION` is used once in the foreground to read your approximate coordinates for local prayer time and Qibla calculation.
   - SalahMate **never** tracks your location in the background and never stores a location history trail.
4. **Network Requests**:
   - Network access is used only when you opt into `AlAdhan.com` online prayer comparison, stream/download Quran Surahs from `AlQuran.cloud`, or configure an optional `Sunnah.com` API key.
   - No personal notes, prayer logs, or analytics are ever transmitted.
5. **User-Controlled Backup**: Exported JSON backups are saved directly to the location you choose via the Android System File Picker (`Storage Access Framework`). Treat exported backup files with care as they contain your personal study notes and prayer logs.
