package com.example.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.preferences.AsrJuristicSchool
import com.example.core.preferences.CalculationMethodOption
import com.example.core.preferences.NotificationSoundMode
import com.example.core.preferences.PRESET_CITIES
import com.example.core.preferences.PrayerProviderMode
import com.example.core.preferences.PresetCity
import com.example.core.utils.BilingualStrings
import com.example.domain.prayer.DailyPrayerType
import com.example.feature.AppDestination
import com.example.feature.SalahMateViewModel
import com.example.ui.components.BentoFeatureCard
import com.example.ui.components.GlassmorphicContainerCard
import com.example.ui.components.IslamicGeometricBackground
import com.example.ui.components.PrayerProgressArcGauge
import com.example.ui.components.skyGradientForPrayer
import com.example.ui.theme.VerifiedEmerald
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstLaunchOnboardingDialog(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    var selectedCity by remember { mutableStateOf(PRESET_CITIES.first()) }
    var selectedMethod by remember { mutableStateOf(CalculationMethodOption.KARACHI) }
    var selectedSchool by remember { mutableStateOf(AsrJuristicSchool.HANAFI) }
    var useCustomCoords by remember { mutableStateOf(false) }
    var customCityName by remember { mutableStateOf("Custom Location") }
    var customLatText by remember { mutableStateOf("23.8103") }
    var customLonText by remember { mutableStateOf("90.4125") }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.detectGpsLocationOnce()
        }
    }

    AlertDialog(
        onDismissRequest = { /* Require explicit confirmation on first launch */ },
        title = {
            Column {
                Text(
                    text = strings.tr("Welcome to SalahMate", "সালাহমেট-এ স্বাগতম"),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = strings.tr(
                        "Select your city, prayer calculation authority, and Asr school to begin.",
                        "শুরু করতে আপনার শহর, নামাজের সময় গণনা পদ্ধতি এবং আসরের মাজহাব নির্বাচন করুন।"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(420.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.tr("1. Select City / Coordinates", "১. শহর বা স্থানাঙ্ক নির্বাচন করুন"),
                            style = MaterialTheme.typography.titleMedium
                        )
                        AssistChip(
                            onClick = { gpsPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                            label = { Text(strings.tr("Use GPS", "জিপিএস")) },
                            leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.testTag("onboarding_gps_chip")
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PRESET_CITIES.forEach { city ->
                            FilterChip(
                                selected = !useCustomCoords && selectedCity.nameEn == city.nameEn,
                                onClick = {
                                    useCustomCoords = false
                                    selectedCity = city
                                    selectedMethod = city.recommendedMethod
                                    selectedSchool = city.recommendedSchool
                                },
                                label = { Text(if (strings.isBn) city.nameBn else city.nameEn) }
                            )
                        }
                        FilterChip(
                            selected = useCustomCoords,
                            onClick = { useCustomCoords = true },
                            label = { Text(strings.tr("Custom Lat/Lon", "কাস্টম স্থানাঙ্ক")) }
                        )
                    }
                    if (useCustomCoords) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = customCityName,
                                onValueChange = { customCityName = it },
                                label = { Text(strings.tr("City / Area Name", "শহরের নাম")) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customLatText,
                                    onValueChange = { customLatText = it },
                                    label = { Text("Latitude") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = customLonText,
                                    onValueChange = { customLonText = it },
                                    label = { Text("Longitude") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Text(
                        text = strings.tr("2. Calculation Method", "২. সময় গণনা পদ্ধতি"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CalculationMethodOption.entries.forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = {
                                    Text(
                                        text = if (strings.isBn) method.titleBn else method.titleEn,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider()
                    Text(
                        text = strings.tr("3. Asr Juristic School (Madhab)", "৩. আসরের ওয়াক্ত নির্ধারণ (মাজহাব)"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AsrJuristicSchool.entries.forEach { school ->
                            FilterChip(
                                selected = selectedSchool == school,
                                onClick = { selectedSchool = school },
                                label = { Text(if (strings.isBn) school.labelBn else school.labelEn) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (useCustomCoords) {
                        val lat = customLatText.toDoubleOrNull()?.coerceIn(-90.0, 90.0) ?: 23.8103
                        val lon = customLonText.toDoubleOrNull()?.coerceIn(-180.0, 180.0) ?: 90.4125
                        viewModel.completeOnboardingSetup(
                            city = customCityName.ifBlank { "Custom" },
                            country = "Manual",
                            lat = lat,
                            lon = lon,
                            tzId = java.util.TimeZone.getDefault().id,
                            method = selectedMethod,
                            school = selectedSchool
                        )
                    } else {
                        viewModel.completeOnboardingSetup(
                            city = selectedCity.nameEn,
                            country = selectedCity.countryEn,
                            lat = selectedCity.latitude,
                            lon = selectedCity.longitude,
                            tzId = selectedCity.timeZoneId,
                            method = selectedMethod,
                            school = selectedSchool
                        )
                    }
                },
                modifier = Modifier.testTag("complete_onboarding_button")
            ) {
                Text(strings.tr("Save & Continue", "সংরক্ষণ করুন"))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val schedule by viewModel.todaySchedule.collectAsStateWithLifecycle()
    val countdown by viewModel.nextPrayerCountdown.collectAsStateWithLifecycle()
    val readingHistory by viewModel.readingHistory.collectAsStateWithLifecycle()
    val prayerLogs by viewModel.prayerLogsList.collectAsStateWithLifecycle()

    var showLocationDialog by remember { mutableStateOf(false) }

    val todayReadingAyahs = readingHistory.firstOrNull { it.dateIso == schedule.dateIso }?.ayahsReadCount ?: 0
    val todayPrayerLog = prayerLogs.firstOrNull { it.dateIso == schedule.dateIso }

    val arcProgress = remember(countdown.remainingHours, countdown.remainingMinutes, countdown.remainingSeconds) {
        val totalRemainingSec = countdown.remainingHours * 3600L + countdown.remainingMinutes * 60L + countdown.remainingSeconds
        val fourHoursSec = 4 * 3600L
        val elapsedRatio = 1f - (totalRemainingSec.toFloat() / fourHoursSec.toFloat()).coerceIn(0f, 1f)
        elapsedRatio.coerceIn(0.12f, 0.92f)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Sky-Gradient & Islamic Geometric Hero Card with 180-Degree Prayer Progress Arc
        item {
            ElevatedCard(
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_prayer_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            skyGradientForPrayer(
                                currentPrayer = countdown.currentPrayer,
                                nextPrayer = countdown.nextPrayer
                            )
                        )
                        .border(
                            BorderStroke(
                                1.2.dp,
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF93C5FD).copy(alpha = 0.65f),
                                        Color(0xFFFDE68A).copy(alpha = 0.45f),
                                        Color(0xFF93C5FD).copy(alpha = 0.65f)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hero_mosque_banner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(278.dp)
                    )
                    IslamicGeometricBackground(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(278.dp),
                        patternColor = Color.White,
                        patternAlpha = 0.08f
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (strings.isBn) schedule.hijriDate.formattedBn else schedule.hijriDate.formattedEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFDE68A),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = schedule.readableGregorianDate,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFFE2E8F0)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.35f),
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { showLocationDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("change_city_chip")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Select City",
                                    tint = Color(0xFFFDE68A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = prefs.cityName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        val nextName = if (strings.isBn) countdown.nextPrayer.titleBn else countdown.nextPrayer.titleEn
                        PrayerProgressArcGauge(
                            progressFraction = arcProgress,
                            countdownText = countdown.formattedCountdown,
                            nextPrayerLabel = "${strings.nextPrayer}: $nextName",
                            nextPrayerTimeText = "${countdown.nextPrayerTimeFormatted} • ${strings.countdown}",
                            countdownTestTag = "next_prayer_countdown_text"
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.18f))

                        val currentPrayerText = countdown.currentPrayer?.let {
                            if (strings.isBn) it.titleBn else it.titleEn
                        } ?: strings.tr("Duha / Post-Sunrise", "ইশরাক / চাশত")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4ADE80))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${strings.currentPrayer}: $currentPrayerText",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "${prefs.calculationMethod.name} • ${prefs.asrSchool.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (schedule.isStaleCache) Color(0xFFFCD34D) else Color(0xFFBAE6FD)
                            )
                        }
                    }
                }
            }
        }

        // 2. Today's 5 Prayers + Sun/Night Celestial Strip in Glassmorphic Card
        item {
            GlassmorphicContainerCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.todaysSchedule,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = { viewModel.navigateTo(AppDestination.PRAYER) },
                            modifier = Modifier.testTag("adjust_prayer_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.tr("Method & Alerts", "পদ্ধতি ও অ্যালার্ম"))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuxiliaryTimeBadge(
                            label = strings.imsak,
                            timeText = schedule.imsakFormatted,
                            modifier = Modifier.weight(1f)
                        )
                        AuxiliaryTimeBadge(
                            label = strings.sunrise,
                            timeText = schedule.sunriseFormatted,
                            modifier = Modifier.weight(1f)
                        )
                        AuxiliaryTimeBadge(
                            label = strings.sunset,
                            timeText = schedule.sunsetFormatted,
                            modifier = Modifier.weight(1f)
                        )
                        AuxiliaryTimeBadge(
                            label = strings.midnight,
                            timeText = schedule.midnightFormatted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    schedule.entries.filter { it.isObligatory }.forEach { entry ->
                        val isNext = countdown.nextPrayer == entry.type
                        val isCurrent = countdown.currentPrayer == entry.type
                        val rowShape = RoundedCornerShape(14.dp)
                        val rowBg = when {
                            isNext -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                            isCurrent -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                        }
                        val rowBorder = if (isNext) {
                            BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                        } else {
                            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                        val prayerIcon = when (entry.type) {
                            DailyPrayerType.FAJR -> Icons.Default.WbTwilight
                            DailyPrayerType.DHUHR -> Icons.Default.WbSunny
                            DailyPrayerType.ASR -> Icons.Default.WbSunny
                            DailyPrayerType.MAGHRIB -> Icons.Default.WbTwilight
                            DailyPrayerType.ISHA -> Icons.Default.AccessTime
                            DailyPrayerType.SUNRISE -> Icons.Default.WbSunny
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(rowShape)
                                .background(rowBg)
                                .border(rowBorder, rowShape)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isNext) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = prayerIcon,
                                        contentDescription = null,
                                        tint = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (strings.isBn) entry.type.titleBn else entry.type.titleEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                        if (isNext) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = strings.tr("NEXT", "পরবর্তী"),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    if (entry.adjustmentMinutes != 0) {
                                        val sign = if (entry.adjustmentMinutes > 0) "+" else ""
                                        Text(
                                            text = strings.tr("Adjusted $sign${entry.adjustmentMinutes} min", "সমন্বয়কৃত $sign${entry.adjustmentMinutes} মি."),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = entry.formatted12h,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val isNotifOn = when (entry.type) {
                                    DailyPrayerType.FAJR -> prefs.notifyFajr
                                    DailyPrayerType.DHUHR -> prefs.notifyDhuhr
                                    DailyPrayerType.ASR -> prefs.notifyAsr
                                    DailyPrayerType.MAGHRIB -> prefs.notifyMaghrib
                                    DailyPrayerType.ISHA -> prefs.notifyIsha
                                    DailyPrayerType.SUNRISE -> false
                                }
                                Icon(
                                    imageVector = if (isNotifOn) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = if (isNotifOn) "Notification Enabled" else "Notification Disabled",
                                    tint = if (isNotifOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${schedule.sourceLabel} • ${strings.timetableDisclaimer}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Glassmorphic Last-Read Quran & Daily Worship Progress Card
        item {
            GlassmorphicContainerCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentColor = VerifiedEmerald
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = strings.tr("Last-Read Quran Position", "সর্বশেষ পঠিত কুরআন অবস্থান"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${prefs.lastReadSurahName} • Ayah ${prefs.lastReadAyahNumber}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.openSurah(prefs.lastReadSurahNumber)
                                viewModel.navigateTo(AppDestination.QURAN)
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("resume_quran_button")
                        ) {
                            Text(strings.tr("Resume", "পড়ুন"))
                        }
                    }

                    val progressFraction = (todayReadingAyahs.toFloat() / prefs.dailyQuranGoalAyahs.coerceAtLeast(1)).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.tr(
                                "Daily Goal: $todayReadingAyahs / ${prefs.dailyQuranGoalAyahs} Ayahs",
                                "দৈনিক তিলাওয়াত: $todayReadingAyahs / ${prefs.dailyQuranGoalAyahs} আয়াত"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        val completedPrayers = todayPrayerLog?.completedCount ?: 0
                        Text(
                            text = strings.tr("Prayers Logged: $completedPrayers/5", "আজকের নামাজ: $completedPrayers/৫"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedEmerald
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // 4. Balanced 2-Column Mobile Bento Grid for All Islamic Modules
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.quickAccess,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.tr("12 Offline-First Modules", "১২টি অফলাইন মডিউল"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val quickActions = listOf(
                QuickModuleItem(
                    strings.navQuran,
                    strings.tr("114 Surahs & Audio", "১১৪ সূরা ও অডিও"),
                    Icons.AutoMirrored.Filled.MenuBook,
                    AppDestination.QURAN,
                    "quick_quran"
                ),
                QuickModuleItem(
                    strings.qibla,
                    strings.tr("Compass & Bearing", "মক্কার দিকনির্দেশ"),
                    Icons.Default.Explore,
                    AppDestination.QIBLA,
                    "quick_qibla"
                ),
                QuickModuleItem(
                    strings.dua,
                    strings.tr("Daily Adhkar & Hisn", "সকাল-সন্ধ্যার দোয়া"),
                    Icons.Default.Favorite,
                    AppDestination.DUA,
                    "quick_dua"
                ),
                QuickModuleItem(
                    strings.tasbih,
                    strings.tr("3D Bead Counter", "ডিজিটাল তাসবীহ"),
                    Icons.Default.TouchApp,
                    AppDestination.TASBIH,
                    "quick_tasbih"
                ),
                QuickModuleItem(
                    strings.hadith,
                    strings.tr("Bukhari, Muslim & 40", "বিশুদ্ধ হাদিস সমগ্র"),
                    Icons.Default.AutoStories,
                    AppDestination.HADITH,
                    "quick_hadith"
                ),
                QuickModuleItem(
                    strings.zakat,
                    strings.tr("2.5% Nisab Engine", "যাকাত ক্যালকুলেটর"),
                    Icons.Default.Calculate,
                    AppDestination.ZAKAT,
                    "quick_zakat"
                ),
                QuickModuleItem(
                    strings.ramadan,
                    strings.tr("Suhoor & Iftar Times", "সাহরি ও ইফতার"),
                    Icons.Default.WbTwilight,
                    AppDestination.RAMADAN,
                    "quick_ramadan"
                ),
                QuickModuleItem(
                    strings.hifz,
                    strings.tr("Memorization Tracker", "হিফজ ও রিভিশন"),
                    Icons.Default.School,
                    AppDestination.HIFZ,
                    "quick_hifz"
                ),
                QuickModuleItem(
                    strings.calendar,
                    strings.tr("Hijri Dates & Events", "হিজরি ক্যালেন্ডার"),
                    Icons.Default.CalendarMonth,
                    AppDestination.CALENDAR,
                    "quick_calendar"
                ),
                QuickModuleItem(
                    strings.tracker,
                    strings.tr("5-Waqt Daily Log", "নামাজ ট্র্যাকার"),
                    Icons.Default.CheckCircle,
                    AppDestination.PRAYER_TRACKER,
                    "quick_tracker"
                ),
                QuickModuleItem(
                    strings.learning,
                    strings.tr("99 Names & Wudu", "আসমাউল হুসনা ও শিক্ষা"),
                    Icons.Default.WbSunny,
                    AppDestination.LEARNING,
                    "quick_learning"
                ),
                QuickModuleItem(
                    strings.mosque,
                    strings.tr("Nearby Masjids Map", "নিকটস্থ মসজিদ"),
                    Icons.Default.Place,
                    AppDestination.MOSQUE_FINDER,
                    "quick_mosque"
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quickActions.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { item ->
                            BentoFeatureCard(
                                title = item.title,
                                subtitle = item.subtitle,
                                icon = item.icon,
                                onClick = { viewModel.navigateTo(item.destination) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(item.testTag)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationPickerDialog(
            viewModel = viewModel,
            strings = strings,
            onDismiss = { showLocationDialog = false }
        )
    }
}

private data class QuickModuleItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val destination: AppDestination,
    val testTag: String
)

@Composable
private fun AuxiliaryTimeBadge(
    label: String,
    timeText: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f))
            .border(
                width = 0.8.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationPickerDialog(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings,
    onDismiss: () -> Unit
) {
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    var customCity by remember { mutableStateOf(prefs.cityName) }
    var customCountry by remember { mutableStateOf(prefs.countryName) }
    var customLat by remember { mutableStateOf(prefs.latitude.toString()) }
    var customLon by remember { mutableStateOf(prefs.longitude.toString()) }

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.detectGpsLocationOnce()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.tr("Select City or Coordinates", "শহর বা স্থানাঙ্ক নির্বাচন করুন")) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(380.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = { gpsLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.tr("Detect Current GPS Location (Optional)", "বর্তমান জিপিএস অবস্থান নির্ণয় করুন"))
                    }
                }
                item {
                    Text(
                        text = strings.tr("Preset Cities (No GPS Permission Needed)", "নির্ধারিত শহরসমূহ (জিপিএস ছাড়াই)"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PRESET_CITIES.forEach { city ->
                            FilterChip(
                                selected = prefs.cityName == city.nameEn,
                                onClick = {
                                    viewModel.selectPresetCity(city)
                                    onDismiss()
                                },
                                label = { Text(if (strings.isBn) city.nameBn else city.nameEn) }
                            )
                        }
                    }
                }
                item {
                    HorizontalDivider()
                    Text(
                        text = strings.tr("Manual Latitude & Longitude", "ম্যানুয়াল অক্ষাংশ ও দ্রাঘিমাংশ"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = customCity,
                        onValueChange = { customCity = it },
                        label = { Text(strings.tr("City / Locality Label", "শহরের নাম")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customLat,
                            onValueChange = { customLat = it },
                            label = { Text("Latitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customLon,
                            onValueChange = { customLon = it },
                            label = { Text("Longitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = customLat.toDoubleOrNull()?.coerceIn(-90.0, 90.0) ?: prefs.latitude
                    val lon = customLon.toDoubleOrNull()?.coerceIn(-180.0, 180.0) ?: prefs.longitude
                    viewModel.setCustomCoordinates(customCity, customCountry, lat, lon)
                    onDismiss()
                }
            ) {
                Text(strings.tr("Apply Coordinates", "স্থানাঙ্ক সংরক্ষণ"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.tr("Close", "বন্ধ করুন"))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerEngineAndNotificationsScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val schedule by viewModel.todaySchedule.collectAsStateWithLifecycle()
    val diagnostics by viewModel.notificationDiagnostics.collectAsStateWithLifecycle()

    var fajrOffset by remember(prefs.fajrOffsetMin) { mutableIntStateOf(prefs.fajrOffsetMin) }
    var sunriseOffset by remember(prefs.sunriseOffsetMin) { mutableIntStateOf(prefs.sunriseOffsetMin) }
    var dhuhrOffset by remember(prefs.dhuhrOffsetMin) { mutableIntStateOf(prefs.dhuhrOffsetMin) }
    var asrOffset by remember(prefs.asrOffsetMin) { mutableIntStateOf(prefs.asrOffsetMin) }
    var maghribOffset by remember(prefs.maghribOffsetMin) { mutableIntStateOf(prefs.maghribOffsetMin) }
    var ishaOffset by remember(prefs.ishaOffsetMin) { mutableIntStateOf(prefs.ishaOffsetMin) }
    var imsakOffset by remember(prefs.imsakOffsetMin) { mutableIntStateOf(prefs.imsakOffsetMin) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.triggerTestPrayerNotification()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_engine_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Calculation Provider & Method Card
        item {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.tr("Prayer Time Calculation Engine", "নামাজের সময় গণনা ইঞ্জিন"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = strings.tr(
                            "Primary engine: Local Adhan Kotlin Library (100% offline). Optional: AlAdhan Online API comparison with Room caching.",
                            "প্রধান ইঞ্জিন: লোকাল আযান লাইব্রেরি (সম্পূর্ণ অফলাইন)। ঐচ্ছিক: আল-আযান অনলাইন এপিআই ও ক্যাশ।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.providerMode == PrayerProviderMode.LOCAL_ADHAN,
                            onClick = {
                                viewModel.updateCalculationSettings(
                                    prefs.calculationMethod,
                                    prefs.asrSchool,
                                    PrayerProviderMode.LOCAL_ADHAN
                                )
                            },
                            label = { Text(strings.tr("Local Adhan (Offline)", "লোকাল আযান (অফলাইন)")) },
                            modifier = Modifier.testTag("provider_local_chip")
                        )
                        FilterChip(
                            selected = prefs.providerMode == PrayerProviderMode.ONLINE_ALADHAN,
                            onClick = {
                                viewModel.updateCalculationSettings(
                                    prefs.calculationMethod,
                                    prefs.asrSchool,
                                    PrayerProviderMode.ONLINE_ALADHAN
                                )
                            },
                            label = { Text(strings.tr("AlAdhan API + Cache", "আল-আযান এপিআই")) },
                            modifier = Modifier.testTag("provider_online_chip")
                        )
                    }

                    HorizontalDivider()
                    Text(
                        text = strings.tr("Calculation Authority", "গণনা কর্তৃপক্ষ (Method)"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    CalculationMethodOption.entries.forEach { method ->
                        FilterChip(
                            selected = prefs.calculationMethod == method,
                            onClick = {
                                viewModel.updateCalculationSettings(
                                    method,
                                    prefs.asrSchool,
                                    prefs.providerMode
                                )
                            },
                            label = {
                                Text(
                                    text = if (strings.isBn) method.titleBn else method.titleEn,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider()
                    Text(
                        text = strings.tr("Asr Juristic School (Handled Separately)", "আসরের মাজহাব (পৃথক সেটিং)"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    AsrJuristicSchool.entries.forEach { school ->
                        FilterChip(
                            selected = prefs.asrSchool == school,
                            onClick = {
                                viewModel.updateCalculationSettings(
                                    prefs.calculationMethod,
                                    school,
                                    prefs.providerMode
                                )
                            },
                            label = { Text(if (strings.isBn) school.labelBn else school.labelEn) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 2. Manual Minute Adjustments Card
        item {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("Manual Minute Adjustments", "ম্যানুয়াল মিনিট সমন্বয়"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = strings.timetableDisclaimer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    MinuteOffsetRow("Fajr", fajrOffset) { fajrOffset = it }
                    MinuteOffsetRow("Sunrise", sunriseOffset) { sunriseOffset = it }
                    MinuteOffsetRow("Dhuhr", dhuhrOffset) { dhuhrOffset = it }
                    MinuteOffsetRow("Asr", asrOffset) { asrOffset = it }
                    MinuteOffsetRow("Maghrib", maghribOffset) { maghribOffset = it }
                    MinuteOffsetRow("Isha", ishaOffset) { ishaOffset = it }
                    MinuteOffsetRow("Imsak (Before Fajr)", imsakOffset) { imsakOffset = it }

                    Button(
                        onClick = {
                            viewModel.updatePrayerOffsets(
                                fajrOffset, sunriseOffset, dhuhrOffset,
                                asrOffset, maghribOffset, ishaOffset, imsakOffset
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_offsets_button")
                    ) {
                        Text(strings.tr("Save Minute Adjustments", "মিনিট সমন্বয় সংরক্ষণ করুন"))
                    }
                }
            }
        }

        // 3. Opt-In Prayer Notifications & Adhan Card
        item {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("Prayer Notifications & Adhan Alerts", "নামাজের নোটিফিকেশন ও আযান অ্যালার্ট"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (strings.isBn) diagnostics.schedulingModeDescriptionBn else diagnostics.schedulingModeDescriptionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    PrayerToggleRow(strings.fajr, prefs.notifyFajr) {
                        viewModel.updateNotificationSettings(it, prefs.notifyDhuhr, prefs.notifyAsr, prefs.notifyMaghrib, prefs.notifyIsha, prefs.advanceReminderMinutes, prefs.notificationSoundMode)
                    }
                    PrayerToggleRow(strings.dhuhr, prefs.notifyDhuhr) {
                        viewModel.updateNotificationSettings(prefs.notifyFajr, it, prefs.notifyAsr, prefs.notifyMaghrib, prefs.notifyIsha, prefs.advanceReminderMinutes, prefs.notificationSoundMode)
                    }
                    PrayerToggleRow(strings.asr, prefs.notifyAsr) {
                        viewModel.updateNotificationSettings(prefs.notifyFajr, prefs.notifyDhuhr, it, prefs.notifyMaghrib, prefs.notifyIsha, prefs.advanceReminderMinutes, prefs.notificationSoundMode)
                    }
                    PrayerToggleRow(strings.maghrib, prefs.notifyMaghrib) {
                        viewModel.updateNotificationSettings(prefs.notifyFajr, prefs.notifyDhuhr, prefs.notifyAsr, it, prefs.notifyIsha, prefs.advanceReminderMinutes, prefs.notificationSoundMode)
                    }
                    PrayerToggleRow(strings.isha, prefs.notifyIsha) {
                        viewModel.updateNotificationSettings(prefs.notifyFajr, prefs.notifyDhuhr, prefs.notifyAsr, prefs.notifyMaghrib, it, prefs.advanceReminderMinutes, prefs.notificationSoundMode)
                    }

                    HorizontalDivider()
                    Text(
                        text = strings.tr("Notification Sound Mode", "নোটিফিকেশন সাউন্ড মোড"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NotificationSoundMode.entries.forEach { mode ->
                        FilterChip(
                            selected = prefs.notificationSoundMode == mode,
                            onClick = {
                                viewModel.updateNotificationSettings(
                                    prefs.notifyFajr, prefs.notifyDhuhr, prefs.notifyAsr,
                                    prefs.notifyMaghrib, prefs.notifyIsha,
                                    prefs.advanceReminderMinutes, mode
                                )
                            },
                            label = { Text(if (strings.isBn) mode.labelBn else mode.labelEn) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text(
                        text = strings.tr("Advance Reminder Before Prayer", "নামাজের পূর্বে অগ্রিম রিমাইন্ডার"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 5, 10, 15, 30).forEach { mins ->
                            FilterChip(
                                selected = prefs.advanceReminderMinutes == mins,
                                onClick = {
                                    viewModel.updateNotificationSettings(
                                        prefs.notifyFajr, prefs.notifyDhuhr, prefs.notifyAsr,
                                        prefs.notifyMaghrib, prefs.notifyIsha,
                                        mins, prefs.notificationSoundMode
                                    )
                                },
                                label = { Text(if (mins == 0) "On Time" else "$mins min before") }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !diagnostics.hasNotificationPermission) {
                                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.triggerTestPrayerNotification()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_notification_button")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.tr("Preview & Test Notification Now", "এখনই টেস্ট নোটিফিকেশন ও ধ্বনি শুনুন"))
                    }
                }
            }
        }
    }
}

@Composable
private fun MinuteOffsetRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onValueChange((value - 1).coerceIn(-60, 60)) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("-1m")
            }
            Text(
                text = String.format(Locale.US, "%+d min", value),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            OutlinedButton(
                onClick = { onValueChange((value + 1).coerceIn(-60, 60)) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("+1m")
            }
        }
    }
}

@Composable
private fun PrayerToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
