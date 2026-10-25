# SalahMate — Mobile UI & Visual Craft Redesign Plan

## 1. Aesthetic & Design System Upgrade
- **Rich Islamic Geometric Patterns & Glassmorphic Surfaces**:
  - Create a reusable Compose Canvas design toolkit (`ui/components/IslamicDesignComponents.kt`) containing:
    - **8-Pointed Star & Girih Geometric Watermark (`IslamicGeometricBackground`)**: Subtle architectural tessellation rendered behind hero cards and headers.
    - **Glassmorphic Card Container (`GlassmorphicBentoCard`)**: Multi-stop translucent gradient fills with a `1.dp` luminous specular border (`Brush.linearGradient`) and soft elevation tailored for both Alabaster Light and Midnight Slate Dark themes.
    - **Ornate Surah Cartouche & Ayah Rosette Medallion (`AyahRosetteBadge`)**: Custom 8-pointed Rub el Hizb / rosette badge drawn in Canvas to frame verse numbers and Surah headers.
    - **Dynamic Sky Gradient Engine**: Time-aware atmospheric gradients shifting smoothly across Fajr (Dawn Rose & Indigo), Dhuhr (Celestial Sapphire & Cyan), Asr (Warm Amber & Cobalt), Maghrib (Twilight Crimson & Deep Sapphire), and Isha (Starlight Midnight & Silver).
- **Refined Mobile Navigation & Transitions (`MainActivity.kt`)**:
  - Compact top app bar with pill-shaped language/theme quick-toggles, subtle geometric header accent, and elevated pill-indicator bottom navigation bar with smooth `AnimatedContent` screen transitions.

---

## 2. Home Dashboard & Prayer Screen Redesign (`HomeAndPrayerScreens.kt`)
- **Animated Sky-Gradient Hero Card & Prayer Progress Arc**:
  - Replace the static hero banner with an atmospheric **Sky-Gradient Hero Card** overlaid with a subtle geometric star pattern and mosque silhouette.
  - Include a custom **180° Animated Prayer Progress Arc** (`Canvas`) showing real-time progress between the current prayer and next prayer, live tabular countdown timer, Hijri/Gregorian badge, and a glowing status pill.
- **Interactive Horizontal Salah Timeline & 5-Waqt Cards**:
  - Redesign the 5 daily prayers list with glowing active-prayer borders, custom time-of-day icons (Dawn, Zenith Sun, Afternoon Sun, Sunset Horizon, Crescent Night), and one-tap completion checkmarks.
- **Balanced 2-Column Mobile Bento Grid**:
  - Organize Quick Access modules (Quran, Qibla, Dua, Tasbih, Hadith, Zakat, Ramadan, Hifz, Mosque, Calendar, Tracker, Learning) and Daily Progress into a balanced **2-column Bento Grid** with icon badges, live status subtitles, and glassmorphic accents.
- **Redesigned First-Launch Onboarding Sheet**:
  - Polish the onboarding dialog with a geometric header banner, interactive city chips, and clean step cards.

---

## 3. Quran Reader & Hifz Redesign (`QuranAndHifzScreens.kt`)
- **Mushaf-Style Surah Banner & Verse Frames**:
  - Add an ornate **Mushaf Cartouche Header** displaying the Surah name in English/Bengali and Arabic, revelation place badge (Makkah/Madinah), ayah count, and an ornamental **Bismillah** banner (`بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ`).
  - Frame each Ayah card with a custom **Rub el Hizb / 8-Pointed Rosette Number Badge**, generous Arabic typography (`Amiri` font with crisp line-height), distinct Bengali and English translation blocks with left accent bars, and compact pill action buttons (Audio, Bookmark, Last-Read).
- **Floating Glassmorphic Audio Mini-Player**:
  - Add a persistent, floating glassmorphic **Audio Mini-Player Bar** docked at the bottom of the Quran Reader whenever recitation is playing or paused, with animated waveform bars, reciter badge, and play/stop controls.

---

## 4. 3D-Feel Tasbih & Ornate Qibla Compass (`WorshipAndStudyScreens.kt` & `ToolsAndUtilityScreens.kt`)
- **Interactive 3D-Feel Tasbih Bead Ring (`DigitalTasbihScreen`)**:
  - Build a custom Canvas **3D-Shaded Tasbih Bead Ring** with 33/99 radial beads that physically advance and glow along the ring with spring animation on every tap, paired with radial gradient 3D shading on each bead and haptic feedback.
- **Astrolabe-Inspired Ornate Qibla Compass (`QiblaCompassScreen`)**:
  - Upgrade the Qibla compass to a multi-layered **Islamic Astrolabe & Brass/Sapphire Compass Dial** with degree ticks, cardinal calligraphy markers, an animated glowing alignment beam when within ±5° of Makkah, and a 2-column Bento telemetry summary (Qibla Bearing, Device Heading, Distance to Kaaba, Sensor Status).
