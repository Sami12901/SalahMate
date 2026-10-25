# Universal Responsive Design & View-Size Optimization Plan

## 1. Full-Width Glassmorphic Modals & Crushed Chip Fix
- **Custom-Width Dialog Containers (`FirstLaunchOnboardingDialog` & `LocationPickerDialog`)**:
  - Replace narrow default `AlertDialog` width constraints with `Dialog(onDismissRequest = ..., properties = DialogProperties(usePlatformDefaultWidth = false))` wrapped in a rich Islamic glassmorphic `Surface`/`Card` (`Modifier.fillMaxWidth(0.94f).widthIn(max = 560.dp)`).
  - Replace fixed `.height(420.dp)` scrolling boxes with adaptive `BoxWithConstraints`-based height bounds (`heightIn(max = maxHeight * 0.85f)`) and a pinned bottom confirmation footer so sections never clip on compact phones or float awkwardly on tablets.
- **Zero-Wrap Section Headers & Single-Line Chips (`Use GPS` Fix)**:
  - Fix the crushed `"Use GPS"` chip shown in the screenshot by placing section headers inside a responsive `Row` where the title uses `Modifier.weight(1f).padding(end = 8.dp)` and the `"Use GPS"` `AssistChip` uses `maxLines = 1`, `softWrap = false`, and an explicit horizontal padding so it always renders cleanly on a single line across all screen widths.
  - Enforce `maxLines = 1` and `TextOverflow.Ellipsis` on all `AssistChip`, `FilterChip`, `SuggestionChip`, and action button labels across every screen.

## 2. Adaptive Screen Containers & Responsive Bento Grids Across All Devices
- **Responsive Screen Wrapper (`AdaptiveScreenContainer`)**:
  - Introduce a reusable `AdaptiveScreenContainer` in `IslamicDesignComponents.kt` that inspects `BoxWithConstraints` (`maxWidth`) to provide compact spacing (`12.dp` horizontal padding) on small phones (`< 360.dp`), standard balanced spacing (`16.dp`) on regular phones, and centered multi-column / max-width (`widthIn(max = 840.dp)`) layouts on foldables and tablets (`>= 600.dp`).
  - Upgrade the top app bar and bottom navigation bar in `MainActivity.kt` so chips, badges, and navigation labels scale cleanly without truncation on compact devices while utilizing centered wide containers on large screens.
- **Adaptive 2-Column / 3-Column Bento Grids**:
  - Enhance the Home Quick-Action Bento grid, Fasting & Streak cards, Worship trackers, and Tools hub to automatically adjust column counts and card heights based on available screen width (`2 columns` on phones, `3–4 columns` on wide screens/landscape) with equal-height cards and clean typography hierarchy.

## 3. Dynamic Canvas Scaling (`BoxWithConstraints`) for Prayer Arc, Qibla, and Tasbih
- **Home Hero Card & 180° Prayer Arc Gauge (`PrayerProgressArcGauge`)**:
  - Dynamically scale the semi-circular solar/lunar arc width, height, and stroke thickness based on the parent container's width (`clamp` between `130.dp` on small screens and `190.dp` on large phones/tablets) so the countdown timer and next-waqt badge remain crisp and unobstructed.
- **Ornate Qibla Astrolabe Dial (`QiblaCompassScreen`)**:
  - Replace hardcoded `280.dp` dial dimensions with responsive `BoxWithConstraints` sizing (`minOf(maxWidth * 0.78f, maxHeight * 0.46f).coerceIn(210.dp, 360.dp)`) so the compass fits comfortably alongside the degree telemetry cards on both compact phones and landscape/tablet layouts.
- **Interactive 3D-Feel 33-Bead Tasbih Ring (`DigitalTasbihScreen`)**:
  - Scale the circular bead ring and central counter button dynamically (`minOf(maxWidth * 0.80f, 340.dp).coerceAtLeast(220.dp)`) and adjust bead radii proportionally so tap targets remain generous and visually balanced on every screen size.

## 4. Verification & Compatibility
- Preserve all existing Compose `Modifier.testTag(...)` identifiers (`onboarding_gps_chip`, `onboarding_confirm_button`, `location_selector_chip`, `scheduled_alarms_status_table`, etc.) and open-source attribution (**Mohammad Sami Islam**, `com.samiislam.salahmate`).
- Compile the APK with `compile_applet` and run the Robolectric test suite (`gradle :app:testDebugUnitTest`) to verify all user journeys pass.
