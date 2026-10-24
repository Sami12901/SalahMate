package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = lightColorScheme(
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

@Composable
fun SalahMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
