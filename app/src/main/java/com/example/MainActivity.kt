package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    SalahMateTheme(darkTheme = useDarkTheme) {
        if (!prefs.onboardingCompleted) {
            FirstLaunchOnboardingDialog(viewModel = viewModel, strings = strings)
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            if (isSubScreen) {
                                IconButton(
                                    onClick = { viewModel.navigateBack() },
                                    modifier = Modifier.testTag("top_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Navigate Back"
                                    )
                                }
                            }
                        },
                        title = {
                            Text(
                                text = strings.appTitle,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("app_top_title")
                            )
                        },
                        actions = {
                            // Instant English / Bengali Language Switch
                            FilterChip(
                                selected = prefs.languageCode == "bn",
                                onClick = { viewModel.toggleLanguage() },
                                label = { Text(if (prefs.languageCode == "bn") "বাংলা" else "EN") },
                                leadingIcon = {
                                    Icon(Icons.Default.Language, contentDescription = "Switch Language")
                                },
                                modifier = Modifier.testTag("language_toggle_chip")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    val nextTheme = if (useDarkTheme) ThemePreference.LIGHT else ThemePreference.DARK
                                    viewModel.setThemePreference(nextTheme)
                                },
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (useDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Dark/Light Theme"
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
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar {
                            topLevelNavItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentDest == item.destination,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
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
                        NavigationRail {
                            topLevelNavItems.forEach { item ->
                                NavigationRailItem(
                                    selected = currentDest == item.destination,
                                    onClick = { viewModel.navigateTo(item.destination) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        when (currentDest) {
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
