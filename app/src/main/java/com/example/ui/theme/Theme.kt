package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.core.preferences.IslamicColorTheme

/**
 * Rich atmospheric gradient and metallic accent tokens for each of the 4 Islamic themes.
 */
@Immutable
data class IslamicThemePalette(
    val theme: IslamicColorTheme,
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val metallicAccent: Color,
    val metallicHighlight: Color,
    val dialDeepCore: Color,
    val swatchPreviewColors: List<Color>
) {
    val heroBannerGradient: List<Color>
        get() = listOf(heroStart, heroMid, heroEnd)
    val accentGold: Color
        get() = metallicAccent
    val accentGoldLight: Color
        get() = metallicHighlight
    val swatchPrimary: Color
        get() = swatchPreviewColors.getOrElse(1) { heroMid }
    val qiblaDialCenterGradient: List<Color>
        get() = listOf(heroMid, heroStart, dialDeepCore)
    val tasbihHeadBeadColors: List<Color>
        get() = listOf(Color(0xFFFFFBEB), metallicHighlight, metallicAccent)
    val tasbihActiveBeadColors: List<Color>
        get() = listOf(swatchPreviewColors.last(), swatchPreviewColors.getOrElse(1) { heroMid }, heroStart)
    val tasbihMedallionGradient: List<Color>
        get() = listOf(heroEnd, heroMid, heroStart)
}

fun paletteForIslamicTheme(theme: IslamicColorTheme): IslamicThemePalette = when (theme) {
    IslamicColorTheme.MADINAH_EMERALD -> IslamicThemePalette(
        theme = theme,
        heroStart = Color(0xFF05291E),
        heroMid = Color(0xFF0B523B),
        heroEnd = Color(0xFF136F50),
        metallicAccent = Color(0xFFD4AF37),
        metallicHighlight = Color(0xFFFDE68A),
        dialDeepCore = Color(0xFF06231A),
        swatchPreviewColors = listOf(
            Color(0xFF063D2C),
            Color(0xFF10B981),
            Color(0xFFFDE68A)
        )
    )
    IslamicColorTheme.KAABA_MIDNIGHT_GOLD -> IslamicThemePalette(
        theme = theme,
        heroStart = Color(0xFF0C0A09),
        heroMid = Color(0xFF1C1917),
        heroEnd = Color(0xFF3B2B10),
        metallicAccent = Color(0xFFEAB308),
        metallicHighlight = Color(0xFFFEF08A),
        dialDeepCore = Color(0xFF12100E),
        swatchPreviewColors = listOf(
            Color(0xFF141210),
            Color(0xFFD4AF37),
            Color(0xFFFEF08A)
        )
    )
    IslamicColorTheme.AL_AQSA_SAPPHIRE -> IslamicThemePalette(
        theme = theme,
        heroStart = Color(0xFF081C38),
        heroMid = Color(0xFF0F4C81),
        heroEnd = Color(0xFF1D68A8),
        metallicAccent = Color(0xFF93C5FD),
        metallicHighlight = Color(0xFFFDE68A),
        dialDeepCore = Color(0xFF0A192F),
        swatchPreviewColors = listOf(
            Color(0xFF0A2546),
            Color(0xFF2563EB),
            Color(0xFFBAE6FD)
        )
    )
    IslamicColorTheme.DESERT_ROSE_PARCHMENT -> IslamicThemePalette(
        theme = theme,
        heroStart = Color(0xFF2A1115),
        heroMid = Color(0xFF6B262B),
        heroEnd = Color(0xFF9D3E35),
        metallicAccent = Color(0xFFF59E0B),
        metallicHighlight = Color(0xFFFDE68A),
        dialDeepCore = Color(0xFF261014),
        swatchPreviewColors = listOf(
            Color(0xFF4A181F),
            Color(0xFFBE4B40),
            Color(0xFFFDE68A)
        )
    )
}

val LocalIslamicThemePalette = staticCompositionLocalOf {
    paletteForIslamicTheme(IslamicColorTheme.MADINAH_EMERALD)
}

// --- 1. Sacred Madinah Emerald Schemes ---
private val MadinahLightScheme = lightColorScheme(
    primary = MadinahPrimaryLight,
    onPrimary = OnMadinahPrimaryLight,
    primaryContainer = MadinahContainerLight,
    onPrimaryContainer = OnMadinahContainerLight,
    secondary = MadinahSecondaryLight,
    onSecondary = OnMadinahSecondaryLight,
    secondaryContainer = MadinahSecContainerLight,
    onSecondaryContainer = OnMadinahSecContainerLight,
    tertiary = MadinahTertiaryLight,
    onTertiary = OnMadinahTertiaryLight,
    tertiaryContainer = MadinahTertContainerLight,
    onTertiaryContainer = OnMadinahTertContainerLight,
    background = MadinahBackgroundLight,
    onBackground = OnMadinahBackgroundLight,
    surface = MadinahSurfaceLight,
    onSurface = OnMadinahSurfaceLight,
    surfaceVariant = MadinahSurfaceVariantLight,
    onSurfaceVariant = OnMadinahSurfaceVariantLight,
    outline = MadinahOutlineLight
)

private val MadinahDarkScheme = darkColorScheme(
    primary = MadinahPrimaryDark,
    onPrimary = OnMadinahPrimaryDark,
    primaryContainer = MadinahContainerDark,
    onPrimaryContainer = OnMadinahContainerDark,
    secondary = MadinahSecondaryDark,
    onSecondary = OnMadinahSecondaryDark,
    secondaryContainer = MadinahSecContainerDark,
    onSecondaryContainer = OnMadinahSecContainerDark,
    tertiary = MadinahTertiaryDark,
    onTertiary = OnMadinahTertiaryDark,
    tertiaryContainer = MadinahTertContainerDark,
    onTertiaryContainer = OnMadinahTertContainerDark,
    background = MadinahBackgroundDark,
    onBackground = OnMadinahBackgroundDark,
    surface = MadinahSurfaceDark,
    onSurface = OnMadinahSurfaceDark,
    surfaceVariant = MadinahSurfaceVariantDark,
    onSurfaceVariant = OnMadinahSurfaceVariantDark,
    outline = MadinahOutlineDark
)

// --- 2. Kaaba Midnight Gold Schemes ---
private val KaabaLightScheme = lightColorScheme(
    primary = KaabaPrimaryLight,
    onPrimary = OnKaabaPrimaryLight,
    primaryContainer = KaabaContainerLight,
    onPrimaryContainer = OnKaabaContainerLight,
    secondary = KaabaSecondaryLight,
    onSecondary = OnKaabaSecondaryLight,
    secondaryContainer = KaabaSecContainerLight,
    onSecondaryContainer = OnKaabaSecContainerLight,
    tertiary = KaabaTertiaryLight,
    onTertiary = OnKaabaTertiaryLight,
    tertiaryContainer = KaabaTertContainerLight,
    onTertiaryContainer = OnKaabaTertContainerLight,
    background = KaabaBackgroundLight,
    onBackground = OnKaabaBackgroundLight,
    surface = KaabaSurfaceLight,
    onSurface = OnKaabaSurfaceLight,
    surfaceVariant = KaabaSurfaceVariantLight,
    onSurfaceVariant = OnKaabaSurfaceVariantLight,
    outline = KaabaOutlineLight
)

private val KaabaDarkScheme = darkColorScheme(
    primary = KaabaPrimaryDark,
    onPrimary = OnKaabaPrimaryDark,
    primaryContainer = KaabaContainerDark,
    onPrimaryContainer = OnKaabaContainerDark,
    secondary = KaabaSecondaryDark,
    onSecondary = OnKaabaSecondaryDark,
    secondaryContainer = KaabaSecContainerDark,
    onSecondaryContainer = OnKaabaSecContainerDark,
    tertiary = KaabaTertiaryDark,
    onTertiary = OnKaabaTertiaryDark,
    tertiaryContainer = KaabaTertContainerDark,
    onTertiaryContainer = OnKaabaTertContainerDark,
    background = KaabaBackgroundDark,
    onBackground = OnKaabaBackgroundDark,
    surface = KaabaSurfaceDark,
    onSurface = OnKaabaSurfaceDark,
    surfaceVariant = KaabaSurfaceVariantDark,
    onSurfaceVariant = OnKaabaSurfaceVariantDark,
    outline = KaabaOutlineDark
)

// --- 3. Al-Aqsa Sapphire Schemes ---
private val SapphireLightScheme = lightColorScheme(
    primary = SapphirePrimaryLight,
    onPrimary = OnSapphirePrimaryLight,
    primaryContainer = SapphireContainerLight,
    onPrimaryContainer = OnSapphireContainerLight,
    secondary = SilverSecondaryLight,
    onSecondary = OnSilverSecondaryLight,
    secondaryContainer = SilverContainerLight,
    onSecondaryContainer = OnSilverContainerLight,
    tertiary = CelestialTertiaryLight,
    onTertiary = OnCelestialTertiaryLight,
    tertiaryContainer = CelestialContainerLight,
    onTertiaryContainer = OnCelestialContainerLight,
    background = AlabasterBackgroundLight,
    onBackground = OnAlabasterBackgroundLight,
    surface = AlabasterSurfaceLight,
    onSurface = OnAlabasterSurfaceLight,
    surfaceVariant = AlabasterSurfaceVariantLight,
    onSurfaceVariant = OnAlabasterSurfaceVariantLight,
    outline = SilverOutlineLight
)

private val SapphireDarkScheme = darkColorScheme(
    primary = SapphirePrimaryDark,
    onPrimary = OnSapphirePrimaryDark,
    primaryContainer = SapphireContainerDark,
    onPrimaryContainer = OnSapphireContainerDark,
    secondary = SilverSecondaryDark,
    onSecondary = OnSilverSecondaryDark,
    secondaryContainer = SilverContainerDark,
    onSecondaryContainer = OnSilverContainerDark,
    tertiary = CelestialTertiaryDark,
    onTertiary = OnCelestialTertiaryDark,
    tertiaryContainer = CelestialContainerDark,
    onTertiaryContainer = OnCelestialContainerDark,
    background = MidnightBackgroundDark,
    onBackground = OnMidnightBackgroundDark,
    surface = MidnightSurfaceDark,
    onSurface = OnMidnightSurfaceDark,
    surfaceVariant = MidnightSurfaceVariantDark,
    onSurfaceVariant = OnMidnightSurfaceVariantDark,
    outline = SilverOutlineDark
)

// --- 4. Desert Rose Parchment Schemes ---
private val DesertLightScheme = lightColorScheme(
    primary = DesertPrimaryLight,
    onPrimary = OnDesertPrimaryLight,
    primaryContainer = DesertContainerLight,
    onPrimaryContainer = OnDesertContainerLight,
    secondary = DesertSecondaryLight,
    onSecondary = OnDesertSecondaryLight,
    secondaryContainer = DesertSecContainerLight,
    onSecondaryContainer = OnDesertSecContainerLight,
    tertiary = DesertTertiaryLight,
    onTertiary = OnDesertTertiaryLight,
    tertiaryContainer = DesertTertContainerLight,
    onTertiaryContainer = OnDesertTertContainerLight,
    background = DesertBackgroundLight,
    onBackground = OnDesertBackgroundLight,
    surface = DesertSurfaceLight,
    onSurface = OnDesertSurfaceLight,
    surfaceVariant = DesertSurfaceVariantLight,
    onSurfaceVariant = OnDesertSurfaceVariantLight,
    outline = DesertOutlineLight
)

private val DesertDarkScheme = darkColorScheme(
    primary = DesertPrimaryDark,
    onPrimary = OnDesertPrimaryDark,
    primaryContainer = DesertContainerDark,
    onPrimaryContainer = OnDesertContainerDark,
    secondary = DesertSecondaryDark,
    onSecondary = OnDesertSecondaryDark,
    secondaryContainer = DesertSecContainerDark,
    onSecondaryContainer = OnDesertSecContainerDark,
    tertiary = DesertTertiaryDark,
    onTertiary = OnDesertTertiaryDark,
    tertiaryContainer = DesertTertContainerDark,
    onTertiaryContainer = OnDesertTertContainerDark,
    background = DesertBackgroundDark,
    onBackground = OnDesertBackgroundDark,
    surface = DesertSurfaceDark,
    onSurface = OnDesertSurfaceDark,
    surfaceVariant = DesertSurfaceVariantDark,
    onSurfaceVariant = OnDesertSurfaceVariantDark,
    outline = DesertOutlineDark
)

@Composable
fun SalahMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: IslamicColorTheme = IslamicColorTheme.MADINAH_EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (colorTheme) {
        IslamicColorTheme.MADINAH_EMERALD -> if (darkTheme) MadinahDarkScheme else MadinahLightScheme
        IslamicColorTheme.KAABA_MIDNIGHT_GOLD -> if (darkTheme) KaabaDarkScheme else KaabaLightScheme
        IslamicColorTheme.AL_AQSA_SAPPHIRE -> if (darkTheme) SapphireDarkScheme else SapphireLightScheme
        IslamicColorTheme.DESERT_ROSE_PARCHMENT -> if (darkTheme) DesertDarkScheme else DesertLightScheme
    }
    val palette = paletteForIslamicTheme(colorTheme)

    CompositionLocalProvider(LocalIslamicThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
