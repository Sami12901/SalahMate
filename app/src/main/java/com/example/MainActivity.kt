package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.preferences.ThemePreference
import com.example.core.utils.BilingualStrings
import com.example.feature.AppDestination
import com.example.feature.SalahMateViewModel
import com.example.feature.home.FirstLaunchOnboardingDialog
import com.example.feature.home.HomeDashboardScreen
import com.example.feature.home.PrayerEngineAndNotificationsScreen
import com.example.feature.quran.HifzTrackerScreen
import com.example.feature.quran.QuranReaderScreen
import com.example.feature.tools.HijriCalendarScreen
import com.example.feature.tools.MosqueFinderScreen
import com.example.feature.tools.PrayerTrackerScreen
import com.example.feature.tools.QiblaCompassScreen
import com.example.feature.tools.RamadanScheduleScreen
import com.example.feature.tools.SettingsAndBackupScreen
import com.example.feature.tools.ToolsHubScreen
import com.example.feature.tools.ZakatCalculatorScreen
import com.example.feature.worship.DigitalTasbihScreen
import com.example.feature.worship.DuaAndAdhkarScreen
import com.example.feature.worship.HadithLibraryScreen
import com.example.feature.worship.IslamicLearningScreen
import com.example.feature.worship.WorshipHubScreen
import com.example.ui.components.AyahRosetteMedallion
import com.example.ui.components.ThemePalettePickerDialog
import com.example.ui.theme.SalahMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val salahMateViewModel: SalahMateViewModel = viewModel()
            SalahMateApp(viewModel = salahMateViewModel)
        }
    }
}

private data class TopLevelNavItem(
    val destination: AppDestination,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahMateApp(viewModel: SalahMateViewModel) {
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val currentDest by viewModel.currentDestination.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (prefs.themePreference) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
    }

    val strings = remember(prefs.languageCode) {
        BilingualStrings(isBn = prefs.languageCode == "bn")
    }

    LaunchedEffect(statusBanner) {
        statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    val topLevelNavItems = listOf(
        TopLevelNavItem(AppDestination.HOME, strings.navHome, Icons.Default.Home, "nav_home"),
        TopLevelNavItem(AppDestination.PRAYER, strings.navPrayer, Icons.Default.AccessTime, "nav_prayer"),
        TopLevelNavItem(AppDestination.QURAN, strings.navQuran, Icons.AutoMirrored.Filled.MenuBook, "nav_quran"),
        TopLevelNavItem(AppDestination.WORSHIP, strings.navWorship, Icons.Default.AutoStories, "nav_worship"),
        TopLevelNavItem(AppDestination.TOOLS, strings.navMore, Icons.Default.Explore, "nav_tools")
    )

    val isSubScreen = currentDest !in topLevelNavItems.map { it.destination }
    var showThemePaletteDialog by remember { mutableStateOf(false) }

    SalahMateTheme(
        darkTheme = useDarkTheme,
        colorTheme = prefs.colorTheme
    ) {
        if (!prefs.onboardingCompleted) {
            FirstLaunchOnboardingDialog(viewModel = viewModel, strings = strings)
        }

        if (showThemePaletteDialog) {
            ThemePalettePickerDialog(
                selectedColorTheme = prefs.colorTheme,
                selectedThemeMode = prefs.themePreference,
                strings = strings,
                onSelectColorTheme = { viewModel.setColorTheme(it) },
                onSelectThemeMode = { viewModel.setThemePreference(it) },
                onDismiss = { showThemePaletteDialog = false }
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp
            val isCompactPhone = maxWidth < 360.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            navigationIcon = {
                                if (isSubScreen) {
                                    IconButton(
                                        onClick = { viewModel.navigateBack() },
                                        modifier = Modifier
                                            .padding(start = 4.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                            .testTag("top_back_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Navigate Back"
                                        )
                                    }
                                } else {
                                    AyahRosetteMedallion(
                                        numberText = "۞",
                                        size = 34.dp,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                    )
                                }
                            },
                            title = {
                                Column {
                                    Text(
                                        text = strings.appTitle,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = if (isCompactPhone) 17.sp else 19.sp
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.testTag("app_top_title")
                                    )
                                    Text(
                                        text = "${prefs.cityName} • ${prefs.calculationMethod.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                FilterChip(
                                    selected = prefs.languageCode == "bn",
                                    onClick = { viewModel.toggleLanguage() },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    label = {
                                        Text(
                                            text = if (prefs.languageCode == "bn") "বাংলা" else "EN",
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Language,
                                            contentDescription = "Switch Language",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("language_toggle_chip")
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                IconButton(
                                    onClick = { showThemePaletteDialog = true },
                                    modifier = Modifier.testTag("theme_palette_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Choose Islamic Color Theme",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val nextTheme = if (useDarkTheme) ThemePreference.LIGHT else ThemePreference.DARK
                                        viewModel.setThemePreference(nextTheme)
                                    },
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (useDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Dark/Light Theme",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.navigateTo(AppDestination.SETTINGS) },
                                    modifier = Modifier.testTag("top_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings & Backup"
                                    )
                                }
                            }
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                        )
                    }
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        Column {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                topLevelNavItems.forEach { item ->
                                    val isSelected = currentDest == item.destination
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.navigateTo(item.destination) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = item.label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            topLevelNavItems.forEach { item ->
                                NavigationRailItem(
                                    selected = currentDest == item.destination,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        com.example.ui.components.AdaptiveScreenContainer {
                            AnimatedContent(
                                targetState = currentDest,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith
                                        fadeOut(animationSpec = tween(180))
                                },
                                label = "screen_transition"
                            ) { targetDest ->
                                when (targetDest) {
                                    AppDestination.HOME -> HomeDashboardScreen(viewModel, strings)
                                    AppDestination.PRAYER -> PrayerEngineAndNotificationsScreen(viewModel, strings)
                                    AppDestination.QURAN -> QuranReaderScreen(viewModel, strings)
                                    AppDestination.WORSHIP -> WorshipHubScreen(viewModel, strings)
                                    AppDestination.TOOLS -> ToolsHubScreen(viewModel, strings)
                                    AppDestination.QIBLA -> QiblaCompassScreen(viewModel, strings)
                                    AppDestination.TASBIH -> DigitalTasbihScreen(viewModel, strings)
                                    AppDestination.DUA -> DuaAndAdhkarScreen(viewModel, strings)
                                    AppDestination.HADITH -> HadithLibraryScreen(viewModel, strings)
                                    AppDestination.HIFZ -> HifzTrackerScreen(viewModel, strings)
                                    AppDestination.RAMADAN -> RamadanScheduleScreen(viewModel, strings)
                                    AppDestination.CALENDAR -> HijriCalendarScreen(viewModel, strings)
                                    AppDestination.ZAKAT -> ZakatCalculatorScreen(viewModel, strings)
                                    AppDestination.PRAYER_TRACKER -> PrayerTrackerScreen(viewModel, strings)
                                    AppDestination.LEARNING -> IslamicLearningScreen(viewModel, strings)
                                    AppDestination.MOSQUE_FINDER -> MosqueFinderScreen(viewModel, strings)
                                    AppDestination.SETTINGS -> SettingsAndBackupScreen(viewModel, strings)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
