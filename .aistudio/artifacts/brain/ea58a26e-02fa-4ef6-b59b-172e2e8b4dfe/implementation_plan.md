# 4 Beautiful Islamic Color Themes Implementation Plan

## 1. 4 New Islamic Theme Palettes (Light & Dark Mode Support)
- **Define `IslamicColorTheme` Enum (`UserPreferencesRepository.kt`)**:
  1. **`MADINAH_EMERALD` (Sacred Madinah Emerald — সবুজ মদিনা)**:
     - Inspired by the Green Dome of Al-Masjid an-Nabawi: rich emerald green (`#0D6E52` / `#34D399`), illuminated brass gold (`#D4AF37`), and soft sage/ivory surfaces in Light mode with deep forest-jade slate in Dark mode.
  2. **`KAABA_MIDNIGHT_GOLD` (Kaaba Midnight Gold — কাবা সোনালি কৃষ্ণ)**:
     - Inspired by the Kiswah of the Sacred Kaaba: burnished gold (`#B48327` / `#FBBF24`), warm amber highlights, and silk cream parchment in Light mode with velvet obsidian-gold surfaces in Dark mode.
  3. **`AL_AQSA_SAPPHIRE` (Al-Aqsa Sapphire — আল-আকসা নীলাভ)**:
     - Inspired by the Dome of the Rock tiles: celestial cobalt & sapphire blue (`#0F4C81` / `#58A6FF`), silver accents, and crisp alabaster in Light mode with midnight sapphire slate in Dark backups.
  4. **`DESERT_ROSE_PARCHMENT` (Desert Rose Parchment — মরু গোলাপ ও প্রাচীন পাণ্ডুলিপি)**:
     - Inspired by illuminated desert manuscripts & Andalusian terracotta: warm desert rose/terracotta (`#9D3E35` / `#FB7185`), antique gold, and warm sand-parchment in Light mode with deep maroon-plum slate in Dark mode.
- **DataStore Persistence**:
  - Persist `colorTheme: IslamicColorTheme` in `UserPreferencesState` and `UserPreferencesRepository` (with `setColorTheme(theme: IslamicColorTheme)` in `SalahMateViewModel`) so the selected theme is saved across app restarts and included in JSON backups.

## 2. Theme-Reactive Gradients, Gauges, Qibla Dial & Tasbih Beads
- **Centralized Theme Palette Tokens (`Color.kt` & `Theme.kt`)**:
  - Create full Material 3 `lightColorScheme` and `darkColorScheme` palettes for all 4 `IslamicColorTheme` options in `Theme.kt` and expose a `CompositionLocal` (`LocalIslamicThemePalette`) carrying theme-specific gradient colors, metallic accents, and swatch preview colors.
- **Reactive Visual Components (`IslamicDesignComponents.kt`, `ToolsAndUtilityScreens.kt`, `WorshipAndStudyScreens.kt`)**:
  - Update `skyGradientForPrayer(...)`, `MushafSurahHeaderBanner`, `GlassmorphicModalDialog`, the Qibla Astrolabe Dial, and the 3D 33-Bead Tasbih Ring to blend the active `LocalIslamicThemePalette` primary/metallic gradients so switching themes transforms the entire visual atmosphere of the app.

## 3. Quick Theme Palette Picker (Top App Bar) & Interactive Swatch Cards (Settings)
- **Top App Bar Quick Theme Picker (`MainActivity.kt`)**:
  - Add a palette icon button (`Modifier.testTag("theme_palette_button")`) in the Top App Bar that opens a `GlassmorphicModalDialog` (`ThemePalettePickerDialog`) allowing instant switching between the 4 Islamic themes and Light/Dark/System mode from anywhere in the app.
- **Interactive Visual Swatch Grid in Settings (`ToolsAndUtilityScreens.kt`)**:
  - Upgrade the Appearance section in `SettingsAndBackupScreen` with a 2-column grid of rich visual swatch cards (`theme_swatch_madinah_emerald`, `theme_swatch_kaaba_midnight_gold`, `theme_swatch_al_aqsa_sapphire`, `theme_swatch_desert_rose_parchment`), each displaying a live 3-color preview gradient, bilingual title/subtitle, and active checkmark badge.

## 4. Verification
- Compile the APK with `compile_applet` and run the Robolectric unit test suite (`gradle :app:testDebugUnitTest`) to verify all themes, preferences, and UI tags work seamlessly.
