package com.example.feature.tools

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.preferences.ThemePreference
import com.example.core.utils.BilingualStrings
import com.example.domain.prayer.DailyPrayerType
import com.example.domain.prayer.LocalAdhanCalculator
import com.example.domain.qibla.CompassSensorReliability
import com.example.domain.qibla.NisabStandard
import com.example.domain.qibla.ZakatCalculatorEngine
import com.example.domain.qibla.ZakatInputModel
import com.example.feature.AppDestination
import com.example.feature.SalahMateViewModel
import com.example.ui.components.GlassmorphicContainerCard
import com.example.ui.components.IslamicGeometricBackground
import com.example.ui.components.ThemeSwatchGrid
import com.example.ui.components.drawRubElHizbStar
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IlluminatedGold
import com.example.ui.theme.IlluminatedGoldLight
import com.example.ui.theme.LocalIslamicThemePalette
import com.example.ui.theme.MidnightSapphire
import com.example.ui.theme.SapphireElevated
import com.example.ui.theme.VerifiedEmerald
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ToolsHubScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    val themePalette = LocalIslamicThemePalette.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tools_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = themePalette.heroBannerGradient
                        )
                    )
            ) {
                IslamicGeometricBackground(
                    modifier = Modifier.matchParentSize(),
                    patternColor = themePalette.accentGoldLight,
                    patternAlpha = 0.08f
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✦  ISLAMIC INSTRUMENTS & UTILITIES  ✦",
                        style = MaterialTheme.typography.labelSmall,
                        color = themePalette.accentGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr("Islamic Tools, Calendars & Settings", "ইসলামিক টুলস, ক্যালেন্ডার ও সেটিংস"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "High-precision Qibla astrolabe, Zakat & Nisab engine, Hijri calendar, and JSON backup",
                            "নির্ভুল কিবলা কম্পাস, যাকাত ও নিসাব ক্যালকুলেটর, হিজরি ক্যালেন্ডার ও ব্যাকআপ"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }
        }

        val toolCards = listOf(
            Triple(strings.qibla, strings.tr("Compass rose & numeric bearing to Makkah", "মক্কার দিকনির্দেশক কম্পাস ও নিউমেরিক বিয়ারিং"), AppDestination.QIBLA),
            Triple(strings.zakat, strings.tr("Transparent 2.5% Zakat & Nisab breakdown", "স্বচ্ছ ২.৫% যাকাত ও নিসাব ক্যালকুলেটর"), AppDestination.ZAKAT),
            Triple(strings.ramadan, strings.tr("Suhoor, Imsak, Fajr & Maghrib/Iftar schedule", "সাহরি, ইমসাক, ফজর ও ইফতারের সময়সূচী"), AppDestination.RAMADAN),
            Triple(strings.calendar, strings.tr("Hijri Calendar, Moon-Sighting offset & Islamic events", "হিজরি ক্যালেন্ডার, চাঁদ দেখা সমন্বয় ও ইসলামিক দিবস"), AppDestination.CALENDAR),
            Triple(strings.tracker, strings.tr("Daily 5-prayer completion log & reflection history", "দৈনিক ৫ ওয়াক্ত নামাজ ট্র্যাকার ও ইতিহাস"), AppDestination.PRAYER_TRACKER),
            Triple(strings.mosque, strings.tr("Open nearby mosques in your preferred map app", "মানচিত্রে নিকটস্থ মসজিদ খুঁজুন"), AppDestination.MOSQUE_FINDER),
            Triple(strings.settings, strings.tr("Language, Theme, Offline Storage & JSON Backup/Restore", "ভাষা, থিম, স্টোরেজ এবং ব্যাকআপ এক্সপোর্ট/ইমপোর্ট"), AppDestination.SETTINGS)
        )

        items(toolCards) { (title, subtitle, dest) ->
            val iconForDest = when (dest) {
                AppDestination.QIBLA -> Icons.Default.Explore
                AppDestination.ZAKAT -> Icons.Default.Calculate
                AppDestination.RAMADAN -> Icons.Default.WbTwilight
                AppDestination.CALENDAR -> Icons.Default.CalendarMonth
                AppDestination.PRAYER_TRACKER -> Icons.Default.CheckCircle
                AppDestination.MOSQUE_FINDER -> Icons.Default.Place
                else -> Icons.Default.Settings
            }
            GlassmorphicContainerCard(
                onClick = { viewModel.navigateTo(dest) },
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_card_${dest.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                iconForDest,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// --- 2.9 Qibla Compass Screen (Ornate Astrolabe Dial) ---
@Composable
fun QiblaCompassScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val qibla by viewModel.qiblaInfo.collectAsStateWithLifecycle()
    val compass by viewModel.compassState.collectAsStateWithLifecycle()
    val themePalette = LocalIslamicThemePalette.current
    var numericModeOnly by remember { mutableStateOf(false) }

    val dialRotation = if (compass.hasSensor) -compass.deviceHeadingDegrees else 0f
    val qiblaRelativeAngle = (qibla.bearingFromTrueNorthDegrees.toFloat() + dialRotation)
    val normalizedDiff = ((qiblaRelativeAngle % 360f) + 360f) % 360f
    val isAlignedWithKaaba = normalizedDiff <= 8f || normalizedDiff >= 352f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("qibla_compass_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = themePalette.heroBannerGradient
                        )
                    )
            ) {
                IslamicGeometricBackground(
                    modifier = Modifier.matchParentSize(),
                    patternColor = themePalette.accentGoldLight,
                    patternAlpha = 0.08f
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "✦  GREAT-CIRCLE ASTROLABE QIBLA  ✦",
                        style = MaterialTheme.typography.labelSmall,
                        color = themePalette.accentGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${strings.qibla} • ${prefs.cityName}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "Qibla Bearing: %.1f° (%s from True North) • Distance to Kaaba: %,.1f km",
                            qibla.bearingFromTrueNorthDegrees,
                            qibla.cardinalDirection,
                            qibla.distanceToKaabaKm
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = themePalette.accentGoldLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("qibla_bearing_text")
                    )

                    // 2-Column Bento Telemetry Strip inside Hero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = strings.tr("TRUE NORTH BEARING", "উত্তর থেকে কোণ"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f° %s", qibla.bearingFromTrueNorthDegrees, qibla.cardinalDirection),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = strings.tr("MAKKAH DISTANCE", "মক্কার দূরত্ব"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.0f km", qibla.distanceToKaabaKm),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IlluminatedGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (!numericModeOnly) {
                        // Ornate Astrolabe Qibla Dial dynamically sized for any Android screen
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val dialSize = (maxWidth * 0.80f).coerceIn(216.dp, 340.dp)
                            Box(
                                modifier = Modifier
                                    .size(dialSize)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2f
                                val center = Offset(size.width / 2f, size.height / 2f)

                                // Theme-Reactive Astrolabe Radial Dial Face
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = themePalette.qiblaDialCenterGradient,
                                        center = center,
                                        radius = radius
                                    ),
                                    radius = radius,
                                    center = center
                                )

                                // Outer Double Brass/Gold Astrolabe Bezel
                                drawCircle(
                                    color = themePalette.accentGold.copy(alpha = 0.85f),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                                drawCircle(
                                    color = themePalette.accentGoldLight.copy(alpha = 0.35f),
                                    radius = radius - 10.dp.toPx(),
                                    center = center,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )

                                // Subtle Center Rub el Hizb Sacred Geometry
                                drawRubElHizbStar(
                                    center = center,
                                    outerRadius = radius * 0.52f,
                                    color = themePalette.accentGoldLight.copy(alpha = 0.16f),
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                // Rotating Compass Rose (72 degree ticks + Cardinal Pointers)
                                rotate(degrees = dialRotation, pivot = center) {
                                    for (deg in 0 until 360 step 5) {
                                        val isCardinal = deg % 90 == 0
                                        val isMajor = deg % 30 == 0
                                        val tickLen = when {
                                            isCardinal -> 18.dp.toPx()
                                            isMajor -> 12.dp.toPx()
                                            else -> 6.dp.toPx()
                                        }
                                        val angleRad = Math.toRadians((deg - 90).toDouble())
                                        val outerR = radius - 12.dp.toPx()
                                        val innerR = outerR - tickLen
                                        val start = Offset(
                                            x = center.x + (outerR * cos(angleRad)).toFloat(),
                                            y = center.y + (outerR * sin(angleRad)).toFloat()
                                        )
                                        val end = Offset(
                                            x = center.x + (innerR * cos(angleRad)).toFloat(),
                                            y = center.y + (innerR * sin(angleRad)).toFloat()
                                        )
                                        drawLine(
                                            color = when {
                                                deg == 0 -> Color(0xFFFF6B6B) // True North Red/Coral marker
                                                isCardinal -> IlluminatedGoldLight
                                                isMajor -> Color.White.copy(alpha = 0.65f)
                                                else -> Color.White.copy(alpha = 0.28f)
                                            },
                                            start = start,
                                            end = end,
                                            strokeWidth = if (isCardinal) 3.dp.toPx() else if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                // Ornate Qibla Pointer Needle pointing toward Makkah
                                rotate(degrees = qiblaRelativeAngle, pivot = center) {
                                    val needleTipY = center.y - radius + 24.dp.toPx()
                                    val needleTailY = center.y + radius * 0.35f

                                    // Needle Glow
                                    drawLine(
                                        color = VerifiedEmerald.copy(alpha = 0.35f),
                                        start = Offset(center.x, needleTailY),
                                        end = Offset(center.x, needleTipY),
                                        strokeWidth = 14.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )

                                    // Sculpted Diamond Spearhead Path
                                    val needlePath = Path().apply {
                                        moveTo(center.x, needleTipY)
                                        lineTo(center.x + 9.dp.toPx(), center.y - 14.dp.toPx())
                                        lineTo(center.x, needleTailY)
                                        lineTo(center.x - 9.dp.toPx(), center.y - 14.dp.toPx())
                                        close()
                                    }
                                    drawPath(
                                        path = needlePath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(IlluminatedGoldLight, VerifiedEmerald, EmeraldPrimary)
                                        )
                                    )

                                    // Kaaba Medallion Orb at Tip
                                    drawCircle(
                                        color = if (isAlignedWithKaaba) VerifiedEmerald else IlluminatedGold,
                                        radius = 13.dp.toPx(),
                                        center = Offset(center.x, needleTipY + 6.dp.toPx())
                                    )
                                    drawCircle(
                                        color = Color(0xFF0A192F),
                                        radius = 6.dp.toPx(),
                                        center = Offset(center.x, needleTipY + 6.dp.toPx())
                                    )
                                }

                                // Center Astrolabe Pivot Hub
                                drawCircle(
                                    color = IlluminatedGold,
                                    radius = 11.dp.toPx(),
                                    center = center
                                )
                                drawCircle(
                                    color = MidnightSapphire,
                                    radius = 5.dp.toPx(),
                                    center = center
                                )
                            }
                        }
                        }

                        // Alignment Status Pill
                        Surface(
                            shape = CircleShape,
                            color = if (isAlignedWithKaaba) VerifiedEmerald.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isAlignedWithKaaba) {
                                    strings.tr("✓ Aligned with Sacred Kaaba", "✓ পবিত্র কাবার দিকে মুখ করা হয়েছে")
                                } else {
                                    strings.tr(
                                        "Align gold-emerald pointer to the top apex (${String.format(Locale.US, "%.1f°", qibla.bearingFromTrueNorthDegrees)})",
                                        "সোনালি-সবুজ কাঁটাটি উপরের শীর্ষবিন্দুতে মেলান (${String.format(Locale.US, "%.1f°", qibla.bearingFromTrueNorthDegrees)})"
                                    )
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isAlignedWithKaaba) Color(0xFF80CBC4) else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            strings.tr("Numeric Bearing Mode (No Sensor Needed)", "শুধু সংখ্যাসূচক ডিগ্রি মোড"),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(checked = numericModeOnly, onCheckedChange = { numericModeOnly = it })
                    }

                    // Sensor Reliability & Calibration Banner
                    val sensorMsg = when {
                        !compass.hasSensor -> strings.tr(
                            "Magnetometer/Rotation sensor unavailable on this device/emulator. Use the exact numeric Qibla bearing (${String.format(Locale.US, "%.1f°", qibla.bearingFromTrueNorthDegrees)} ${qibla.cardinalDirection}) relative to True North.",
                            "এই ডিভাইসে ম্যাগনেটোমিটার সেন্সর নেই। উত্তর দিক থেকে ${String.format(Locale.US, "%.1f°", qibla.bearingFromTrueNorthDegrees)} (${qibla.cardinalDirection}) কোণে কিবলা নির্ণয় করুন।"
                        )
                        compass.reliability == CompassSensorReliability.LOW -> strings.tr(
                            "Low compass accuracy detected due to magnetic interference. Move away from metal/electronics and wave your phone in a Figure-8 motion to calibrate.",
                            "চুম্বকীয় বাধার কারণে কম্পাসের নির্ভুলতা কম। ধাতব বস্তু থেকে দূরে গিয়ে ফোনটি ইংরেজি '8' আকৃতিতে ঘুরিয়ে ক্যালিব্রেট করুন।"
                        )
                        else -> strings.tr(
                            "Live sensor active (${compass.reliability.name}). Device Heading: ${String.format(Locale.US, "%.0f°", compass.deviceHeadingDegrees)}",
                            "কম্পাস সেন্সর সক্রিয় (${compass.reliability.name})। বর্তমান দিক: ${String.format(Locale.US, "%.0f°", compass.deviceHeadingDegrees)}"
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (!compass.hasSensor || compass.reliability == CompassSensorReliability.LOW)
                                CautionAmber.copy(alpha = 0.22f)
                            else Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sensorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- 2.11 Ramadan, Suhoor, and Iftar Screen ---
@Composable
fun RamadanScheduleScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val todaySchedule by viewModel.todaySchedule.collectAsStateWithLifecycle()

    val thirtyDayProjection = remember(prefs) {
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val baseCal = Calendar.getInstance(tz)
        (0 until 30).map { offset ->
            val dayCal = (baseCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val sched = LocalAdhanCalculator.calculateLocalSchedule(prefs, dayCal)
            Triple(offset + 1, sched, sched.hijriDate)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ramadan_schedule_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.ramadan,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Important Distinction: Imsak (${todaySchedule.imsakFormatted}) is a precautionary margin (${prefs.imsakOffsetMin}m) before Fajr. Obligatory fasting begins at Fajr (${todaySchedule.prayerTimeFor(DailyPrayerType.FAJR)?.formatted12h}) and breaks at Maghrib / Iftar (${todaySchedule.prayerTimeFor(DailyPrayerType.MAGHRIB)?.formatted12h}).",
                            "গুরুত্বপূর্ণ পার্থক্য: ইমসাক (${todaySchedule.imsakFormatted}) হলো ফজরের পূর্বে সতর্কতামূলক সময় (${prefs.imsakOffsetMin} মি.)। সাহরির শেষ সময় ফজর (${todaySchedule.prayerTimeFor(DailyPrayerType.FAJR)?.formatted12h}) এবং ইফতারের সময় মাগরিব (${todaySchedule.prayerTimeFor(DailyPrayerType.MAGHRIB)?.formatted12h})।"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = strings.tr(
                            "Disclaimer: Calculated schedules are astronomical estimates and do not constitute an official moon-sighting announcement or religious ruling.",
                            "সতর্কবার্তা: এই সময়সূচী জ্যোতির্বিজ্ঞানভিত্তিক হিসাব; রমজান শুরু ও ঈদের তারিখ স্থানীয় চাঁদ দেখা কমিটির ঘোষণার ওপর নির্ভরশীল।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(thirtyDayProjection, key = { it.first }) { (dayNum, sched, hijri) ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Day $dayNum • ${sched.dateIso}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = hijri.formattedEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Imsak: ${sched.imsakFormatted} • Fajr: ${sched.prayerTimeFor(DailyPrayerType.FAJR)?.formatted12h}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Maghrib / Iftar: ${sched.prayerTimeFor(DailyPrayerType.MAGHRIB)?.formatted12h}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// --- 2.12 Hijri Calendar & Islamic Events Screen ---
@Composable
fun HijriCalendarScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val schedule by viewModel.todaySchedule.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hijri_calendar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.calendar,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${schedule.hijriDate.formattedEn} (${schedule.hijriDate.monthNameAr})",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = schedule.readableGregorianDate,
                        style = MaterialTheme.typography.titleMedium
                    )

                    @OptIn(ExperimentalLayoutApi::class)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.tr("Moon-Sighting Adjustment:", "চাঁদ দেখা সমন্বয় (দিন):"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (-2..2).forEach { adj ->
                                FilterChip(
                                    selected = prefs.hijriAdjustmentDays == adj,
                                    onClick = { viewModel.setHijriAdjustment(adj) },
                                    label = {
                                        Text(
                                            text = if (adj >= 0) "+$adj" else "$adj",
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = strings.tr(
                            "Note: Official Islamic event dates vary by 1–2 days depending on regional crescent moon sighting.",
                            "দ্রষ্টব্য: স্থানীয় চাঁদ দেখার ওপর ভিত্তি করে ইসলামিক দিবসের তারিখ ১–২ দিন পরিবর্তিত হতে পারে।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = strings.tr("Key Islamic Calendar Events", "গুরুত্বপূর্ণ ইসলামিক দিবসসমূহ"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(LocalAdhanCalculator.ISLAMIC_EVENTS) { ev ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (strings.isBn) ev.titleBn else ev.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hijri Day ${ev.hijriDay}, Month ${ev.hijriMonth}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = if (strings.isBn) ev.descriptionBn else ev.descriptionEn,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// --- 2.13 Transparent Zakat Calculator Screen ---
@Composable
fun ZakatCalculatorScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()

    var cashText by remember { mutableStateOf("250000") }
    var goldGramsText by remember { mutableStateOf("15") }
    var silverGramsText by remember { mutableStateOf("0") }
    var tradeInventoryText by remember { mutableStateOf("0") }
    var investmentsText by remember { mutableStateOf("50000") }
    var debtsText by remember { mutableStateOf("10000") }
    var goldPriceText by remember(prefs.goldPricePerGram) { mutableStateOf(prefs.goldPricePerGram.toString()) }
    var silverPriceText by remember(prefs.silverPricePerGram) { mutableStateOf(prefs.silverPricePerGram.toString()) }
    var nisabStandard by remember { mutableStateOf(NisabStandard.SILVER_612G) }

    val inputModel = ZakatInputModel(
        cashOnHandAndBank = cashText.toDoubleOrNull() ?: 0.0,
        goldGrams24kEquivalent = goldGramsText.toDoubleOrNull() ?: 0.0,
        silverGrams = silverGramsText.toDoubleOrNull() ?: 0.0,
        tradeInventoryValue = tradeInventoryText.toDoubleOrNull() ?: 0.0,
        investmentAndReceivables = investmentsText.toDoubleOrNull() ?: 0.0,
        deductibleDebtsAndLiabilities = debtsText.toDoubleOrNull() ?: 0.0,
        goldPricePerGram = goldPriceText.toDoubleOrNull() ?: prefs.goldPricePerGram,
        silverPricePerGram = silverPriceText.toDoubleOrNull() ?: prefs.silverPricePerGram,
        nisabStandard = nisabStandard
    )
    val result = ZakatCalculatorEngine.calculate(inputModel)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("zakat_calculator_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.zakat,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "Total Zakat Payable (2.5%%): %s%,.2f",
                            prefs.zakatCurrencySymbol,
                            result.totalZakatPayable
                        ),
                        style = MaterialTheme.typography.displayMedium,
                        color = if (result.isZakatDue) VerifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("zakat_payable_result")
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "Net Zakatable Wealth: %s%,.2f • Selected Nisab Threshold: %s%,.2f (%s)",
                            prefs.zakatCurrencySymbol,
                            result.netZakatableWealth,
                            prefs.zakatCurrencySymbol,
                            result.nisabThresholdAmount,
                            if (result.isZakatDue) "Zakat Obligatory" else "Below Nisab Threshold"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("1. Editable Local Gold & Silver Market Prices (Per Gram)", "১. স্থানীয় স্বর্ণ ও রৌপ্যের বাজারমূল্য (প্রতি গ্রাম — পরিবর্তনযোগ্য)"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Enter current local market rates (e.g., BAJUS Bangladesh or local bullion dealer). Never rely on stale hardcoded prices.",
                            "বর্তমান স্থানীয় বাজারদর (যেমন বাজুস রেট) এন্ট্রি করুন।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goldPriceText,
                            onValueChange = { goldPriceText = it },
                            label = { Text("Gold / gram (${prefs.zakatCurrencySymbol})") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = silverPriceText,
                            onValueChange = { silverPriceText = it },
                            label = { Text("Silver / gram (${prefs.zakatCurrencySymbol})") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    NisabStandard.entries.forEach { std ->
                        FilterChip(
                            selected = nisabStandard == std,
                            onClick = { nisabStandard = std },
                            label = { Text(if (strings.isBn) std.labelBn else std.labelEn) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("2. Zakatable Assets & Deductible Liabilities", "২. যাকাতযোগ্য সম্পদ ও বাদযোগ্য ঋণ"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = cashText,
                        onValueChange = { cashText = it },
                        label = { Text(strings.tr("Cash on Hand & Bank Balances", "নগদ অর্থ ও ব্যাংক জমা")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = goldGramsText,
                            onValueChange = { goldGramsText = it },
                            label = { Text(strings.tr("Gold Weight (Grams)", "স্বর্ণ (গ্রাম)")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = silverGramsText,
                            onValueChange = { silverGramsText = it },
                            label = { Text(strings.tr("Silver Weight (Grams)", "রৌপ্য (গ্রাম)")) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = tradeInventoryText,
                        onValueChange = { tradeInventoryText = it },
                        label = { Text(strings.tr("Eligible Trade Inventory / Merchandise", "ব্যবসায়িক পণ্যের বর্তমান মূল্য")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = investmentsText,
                        onValueChange = { investmentsText = it },
                        label = { Text(strings.tr("Other Zakatable Savings / Receivables", "অন্যান্য যাকাতযোগ্য সঞ্চয় ও প্রাপ্য")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = debtsText,
                        onValueChange = { debtsText = it },
                        label = { Text(strings.tr("Immediate Deductible Debts / Liabilities", "তাৎক্ষণিক পরিশোধযোগ্য ঋণ বা দায়")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.tr("3. Transparent Calculation Breakdown", "৩. বিস্তারিত হিসাব বিবরণী"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(String.format(Locale.US, "• Included Cash & Bank: + %s%,.2f", prefs.zakatCurrencySymbol, result.cashTotal))
                    Text(String.format(Locale.US, "• Included Gold Value: + %s%,.2f", prefs.zakatCurrencySymbol, result.goldTotalValue))
                    Text(String.format(Locale.US, "• Included Silver Value: + %s%,.2f", prefs.zakatCurrencySymbol, result.silverTotalValue))
                    Text(String.format(Locale.US, "• Included Trade & Investments: + %s%,.2f", prefs.zakatCurrencySymbol, result.tradeAndInvestmentsTotal))
                    Text(String.format(Locale.US, "• Deducted Immediate Liabilities: - %s%,.2f", prefs.zakatCurrencySymbol, result.deductedLiabilities))
                    HorizontalDivider()
                    Text(
                        text = strings.tr(
                            "Excluded by standard fiqh rules: Personal residence, personal clothing, household furniture, and primary personal vehicle.",
                            "হিসাবের বাইরে (যাকাতমুক্ত): বসবাসের ঘর, নিত্যব্যবহার্য পোশাক, আসবাবপত্র ও ব্যক্তিগত বাহন।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.tr(
                            "Scholarly Disclaimer: Different schools of jurisprudence (Hanafi, Shafi'i, Maliki, Hanbali) differ on personal jewelry and long-term debt deductions. This calculator is an informational aid, not a fatwa for complex commercial estates.",
                            "বিশেষ দ্রষ্টব্য: অলঙ্কার ও দীর্ঘমেয়াদী ঋণের ক্ষেত্রে মাজহাবভেদে পার্থক্য রয়েছে। জটিল ব্যবসায়িক হিসাবের জন্য বিজ্ঞ আলেমের পরামর্শ নিন।"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- 2.14 Daily Prayer Tracker Screen ---
@Composable
fun PrayerTrackerScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val schedule by viewModel.todaySchedule.collectAsStateWithLifecycle()
    val logs by viewModel.prayerLogsList.collectAsStateWithLifecycle()
    val readingHistory by viewModel.readingHistory.collectAsStateWithLifecycle()
    val hifzList by viewModel.hifzProgressList.collectAsStateWithLifecycle()

    val todayLog = logs.firstOrNull { it.dateIso == schedule.dateIso }
    var reflectionText by remember(todayLog?.personalReflection) { mutableStateOf(todayLog?.personalReflection ?: "") }

    val totalLoggedDays = logs.size
    val totalPrayersCompleted = logs.sumOf { it.completedCount }
    val last7Logs = logs.take(7)
    val last30Logs = logs.take(30)
    val last7Completed = last7Logs.sumOf { it.completedCount }
    val last7Target = (last7Logs.size.coerceAtLeast(1)) * 5
    val last30Completed = last30Logs.sumOf { it.completedCount }
    val last30Target = (last30Logs.size.coerceAtLeast(1)) * 5
    val memorizedSurahsCount = hifzList.count { it.status == "MEMORIZED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_tracker_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.tracker,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "100% Private & Local: Your prayer checklist and worship analytics never leave your device.",
                            "১০০% ব্যক্তিগত ও অফলাইন: আপনার নামাজের হিসাব ও ইবাদত বিশ্লেষণ শুধুমাত্র আপনার ফোনেই সংরক্ষিত থাকে।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.tr(
                            "Total Prayers Logged: $totalPrayersCompleted across $totalLoggedDays days",
                            "মোট সম্পন্ন নামাজ: $totalPrayersCompleted ওয়াক্ত ($totalLoggedDays দিন)"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = VerifiedEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Personal Worship Analytics (7-Day & 30-Day Summary)
        item {
            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worship_analytics_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("Personal Worship Analytics (7-Day & 30-Day)", "ব্যক্তিগত ইবাদত বিশ্লেষণ (৭ দিন ও ৩০ দিন)"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.tr("Last 7 Recorded Days", "সর্বশেষ ৭ দিনের নামাজ"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$last7Completed / $last7Target (${((last7Completed.toFloat() / last7Target) * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedEmerald
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (last7Completed.toFloat() / last7Target.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = VerifiedEmerald
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.tr("Last 30 Recorded Days", "সর্বশেষ ৩০ দিনের নামাজ"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$last30Completed / $last30Target (${((last30Completed.toFloat() / last30Target) * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (last30Completed.toFloat() / last30Target.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.tr("Quran Sessions: ${readingHistory.size}", "কুরআন তিলাওয়াত সেশন: ${readingHistory.size}"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = strings.tr("Memorized Surahs: $memorizedSurahsCount", "মুখস্থকৃত সূরা: $memorizedSurahsCount"),
                            style = MaterialTheme.typography.labelMedium,
                            color = VerifiedEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${strings.tr("Today's Checklist", "আজকের নামাজ চেকলিস্ট")} (${schedule.dateIso})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    val checks = listOf(
                        DailyPrayerType.FAJR to (todayLog?.fajrCompleted == true),
                        DailyPrayerType.DHUHR to (todayLog?.dhuhrCompleted == true),
                        DailyPrayerType.ASR to (todayLog?.asrCompleted == true),
                        DailyPrayerType.MAGHRIB to (todayLog?.maghribCompleted == true),
                        DailyPrayerType.ISHA to (todayLog?.ishaCompleted == true)
                    )

                    checks.forEach { (pType, isDone) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (strings.isBn) pType.titleBn else pType.titleEn,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = {
                                    viewModel.togglePrayerCompleted(schedule.dateIso, pType)
                                },
                                modifier = Modifier.testTag("tracker_check_${pType.name.lowercase()}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reflectionText,
                        onValueChange = { reflectionText = it },
                        label = { Text(strings.tr("Personal Note / Gratitude Reflection", "ব্যক্তিগত নোট বা শুকরিয়া")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.savePrayerNote(schedule.dateIso, reflectionText) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.tr("Save Reflection Note", "নোট সংরক্ষণ করুন"))
                    }
                }
            }
        }

        items(logs, key = { it.dateIso }) { log ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(log.dateIso, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (log.personalReflection.isNotBlank()) {
                            Text(log.personalReflection, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "${log.completedCount} / 5 Prayers",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (log.completedCount == 5) VerifiedEmerald else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- 2.16 Mosque Finder Screen ---
@Composable
fun MosqueFinderScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mosque_finder_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.mosque,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Selected Location: ${prefs.cityName}, ${prefs.countryName} (${String.format(Locale.US, "%.4f, %.4f", prefs.latitude, prefs.longitude)})",
                            "নির্বাচিত অবস্থান: ${prefs.cityName}, ${prefs.countryName} (${String.format(Locale.US, "%.4f, %.4f", prefs.latitude, prefs.longitude)})"
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.tr(
                            "SalahMate never tracks your location in the background or spams public geocoding endpoints. Tap below to search for nearby Masjids around ${prefs.cityName} in your installed map application.",
                            "সালাহমেট কখনও ব্যাকগ্রাউন্ডে আপনার অবস্থান ট্র্যাক করে না। আপনার ফোনের ম্যাপ অ্যাপে নিকটস্থ মসজিদ খুঁজতে নিচের বাটনে ট্যাপ করুন।"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${prefs.latitude},${prefs.longitude}?q=mosque+masjid+${Uri.encode(prefs.cityName)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val browserUri = Uri.parse("https://www.openstreetmap.org/search?query=mosque%20${Uri.encode(prefs.cityName)}#map=14/${prefs.latitude}/${prefs.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_mosque_map_button")
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.tr("Open Nearby Mosques in Map App", "ম্যাপ অ্যাপে নিকটস্থ মসজিদ দেখুন"))
                    }
                }
            }
        }
    }
}

// --- Settings, Privacy & JSON Backup/Restore Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsAndBackupScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val cachedAyahsCount by viewModel.totalCachedAyahsCount.collectAsStateWithLifecycle()
    val backupPreview by viewModel.backupPreviewState.collectAsStateWithLifecycle()

    var usePasswordEncryption by remember { mutableStateOf(false) }
    var backupPassword by remember { mutableStateOf("") }
    var pendingImportPayload by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val pwd = if (usePasswordEncryption) backupPassword.takeIf { it.isNotBlank() } else null
                val json = viewModel.exportUserDataJson(pwd)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                viewModel.showStatusMessage(
                    if (usePasswordEncryption && backupPassword.isNotBlank()) {
                        strings.tr("AES-256-GCM Encrypted backup exported.", "এনক্রিপ্টেড ব্যাকআপ এক্সপোর্ট সম্পন্ন হয়েছে।")
                    } else {
                        strings.tr("Plaintext JSON backup exported.", "JSON ব্যাকআপ এক্সপোর্ট সম্পন্ন হয়েছে।")
                    }
                )
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    pendingImportPayload = content
                    val pwd = backupPassword.takeIf { it.isNotBlank() }
                    viewModel.inspectBackupBeforeImport(content, pwd)
                }
            }
        }
    }

    // Restore Preview Modal before executing atomic import
    val currentPreview = backupPreview
    val currentPayload = pendingImportPayload
    if (currentPreview != null && currentPayload != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.clearBackupPreview()
                pendingImportPayload = null
            },
            title = {
                Text(
                    text = strings.tr("Restore Backup Preview", "ব্যাকআপ রিস্টোর প্রিভিউ"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.tr(
                            "Schema v${currentPreview.schemaVersion} • ${if (currentPreview.isEncrypted) "AES-256-GCM Encrypted" else "Plaintext JSON"}",
                            "স্কিমা v${currentPreview.schemaVersion} • ${if (currentPreview.isEncrypted) "AES-256-GCM এনক্রিপ্টেড" else "সাধারণ JSON"}"
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("• Quran Bookmarks: ${currentPreview.bookmarksCount} (${currentPreview.duplicateBookmarksSkipped} duplicates skipped)")
                    Text("• Hifz Progress Records: ${currentPreview.hifzCount}")
                    Text("• Prayer Tracker Logs: ${currentPreview.prayerLogsCount}")
                    Text("• Tasbih Sessions: ${currentPreview.tasbihCount}")
                    Text("• Reading History Entries: ${currentPreview.readingHistoryCount}")
                    Text("• Settings Included: ${if (currentPreview.hasUserPreferences) "Yes" else "No"}")
                    Text(
                        text = strings.tr(
                            "Import runs inside an atomic database transaction. If any record fails validation, no existing data will be altered.",
                            "এই রিস্টোরটি একটি নিরাপদ ট্রানজেকশনে সম্পন্ন হবে। কোনো ত্রুটি হলে বর্তমান ডেটা অপরিবর্তিত থাকবে।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pwd = backupPassword.takeIf { it.isNotBlank() }
                        viewModel.importUserDataJson(currentPayload, pwd)
                        pendingImportPayload = null
                    },
                    modifier = Modifier.testTag("confirm_restore_backup_button")
                ) {
                    Text(strings.tr("Confirm & Restore Now", "নিশ্চিত করুন ও রিস্টোর করুন"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.clearBackupPreview()
                        pendingImportPayload = null
                    }
                ) {
                    Text(strings.tr("Cancel", "বাতিল"))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_backup_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.tr("Appearance, 4 Islamic Color Themes & Language", "৪টি ইসলামিক কালার থিম, ডার্ক মোড ও ভাষা"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Select one of 4 handcrafted Islamic heritage themes. Every theme adapts automatically to Light and Dark mode.",
                            "৪টি নান্দনিক ইসলামিক থিম থেকে আপনার পছন্দেরটি বেছে নিন। প্রতিটি থিম লাইট ও ডার্ক মোডে স্বয়ংক্রিয়ভাবে মানিয়ে নেয়।"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ThemeSwatchGrid(
                        selectedTheme = prefs.colorTheme,
                        isBn = prefs.languageCode == "bn",
                        onSelectTheme = { viewModel.setColorTheme(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

                    Text(
                        text = strings.tr("Brightness Mode (Light / Dark / System)", "উজ্জ্বলতা মোড (লাইট / ডার্ক / সিস্টেম)"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePreference.entries.forEach { t ->
                            FilterChip(
                                selected = prefs.themePreference == t,
                                onClick = { viewModel.setThemePreference(t) },
                                label = {
                                    Text(
                                        text = when (t) {
                                            ThemePreference.SYSTEM -> strings.tr("System Auto", "সিস্টেম অটো")
                                            ThemePreference.LIGHT -> strings.tr("Light Mode", "লাইট মোড")
                                            ThemePreference.DARK -> strings.tr("Dark Mode", "ডার্ক মোড")
                                        },
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

                    Text(
                        text = strings.tr("App Interface Language", "অ্যাপের ভাষা"),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = prefs.languageCode == "en",
                            onClick = { if (prefs.languageCode != "en") viewModel.toggleLanguage() },
                            label = { Text("English", maxLines = 1, softWrap = false) }
                        )
                        FilterChip(
                            selected = prefs.languageCode == "bn",
                            onClick = { if (prefs.languageCode != "bn") viewModel.toggleLanguage() },
                            label = { Text("বাংলা (Bengali)", maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("Local Data Backup & Restore (AES-256-GCM / JSON)", "ব্যক্তিগত ডেটা ব্যাকআপ ও রিস্টোর (AES-256-GCM / JSON)"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Export or restore your Quran bookmarks, Hifz memorization logs, prayer tracker history, Tasbih sessions, and user settings. Cached Quran Ayahs currently stored: $cachedAyahsCount.",
                            "আপনার কুরআন বুকমার্ক, হিফয অগ্রগতি, নামাজ ট্র্যাকার, তাসবীহ সেশন ও সেটিংস ফাইল পিকারের মাধ্যমে ব্যাকআপ ও রিস্টোর করুন।"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.tr("Password-Protected Encrypted Backup (PBKDF2 + AES-256-GCM)", "পাসওয়ার্ড সুরক্ষিত এনক্রিপ্টেড ব্যাকআপ (AES-256-GCM)"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (usePasswordEncryption) {
                                    strings.tr("Authenticated encryption enabled. Keep your password safe.", "অথেনটিকেটেড এনক্রিপশন সক্রিয়। পাসওয়ার্ডটি মনে রাখুন।")
                                } else {
                                    strings.tr("Privacy Warning: Plaintext JSON can be read by anyone with access to the exported file.", "সতর্কতা: পাসওয়ার্ড ছাড়া সাধারণ JSON ফাইল যে কেউ পড়তে পারবে।")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (usePasswordEncryption) VerifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = usePasswordEncryption,
                            onCheckedChange = { usePasswordEncryption = it },
                            modifier = Modifier.testTag("toggle_encrypted_backup_switch")
                        )
                    }

                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = { backupPassword = it },
                        label = {
                            Text(
                                strings.tr(
                                    "Backup / Restore Password (required for encrypted files)",
                                    "ব্যাকআপ / রিস্টোর পাসওয়ার্ড (এনক্রিপ্টেড ফাইলের জন্য)"
                                )
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_password_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val fileName = if (usePasswordEncryption && backupPassword.isNotBlank()) {
                                    "salahmate_encrypted_backup.json"
                                } else {
                                    "salahmate_backup.json"
                                }
                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.tr("Export Backup", "ব্যাকআপ এক্সপোর্ট"), maxLines = 1, softWrap = false)
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.tr("Preview & Restore", "প্রিভিউ ও রিস্টোর"), maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }

        item {
            val themePalette = LocalIslamicThemePalette.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = themePalette.heroBannerGradient
                        )
                    )
                    .testTag("developer_attribution_card")
            ) {
                IslamicGeometricBackground(
                    modifier = Modifier.matchParentSize(),
                    patternColor = themePalette.accentGoldLight,
                    patternAlpha = 0.08f
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✦  100% FREE & OPEN-SOURCE ISLAMIC PROJECT  ✦",
                        style = MaterialTheme.typography.labelSmall,
                        color = themePalette.accentGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr("Developer: Mohammad Sami Islam", "ডেভেলপার: মোহাম্মদ সামি ইসলাম (Mohammad Sami Islam)"),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "SalahMate is a 100% free, open-source Islamic Android application (MIT License) developed by Mohammad Sami Islam. Built for the Ummah with zero ads, zero tracking, and complete offline-first reliability.",
                            "সালাহমেট সম্পূর্ণ ফ্রি ও ওপেন-সোর্স (MIT License) একটি ইসলামিক অ্যান্ড্রয়েড অ্যাপ—ডেভেলপার: Mohammad Sami Islam। কোনো বিজ্ঞাপন বা ট্র্যাকিং ছাড়াই সম্পূর্ণ অফলাইনে নির্ভরযোগ্যভাবে ব্যবহারের জন্য নির্মিত।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VerifiedEmerald.copy(alpha = 0.24f)
                        ) {
                            Text(
                                text = "MIT Open-Source License",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF80CBC4),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "com.samiislam.salahmate",
                                style = MaterialTheme.typography.labelSmall,
                                color = IlluminatedGoldLight,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = strings.tr("Privacy, Content Attribution & Licenses", "গোপনীয়তা নীতি, সূত্র ও লাইসেন্স"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Developer & Maintainer: Mohammad Sami Islam (MIT Open-Source License).\n" +
                            "• Prayer & Qibla Engine: Batoulapps Adhan Java/Kotlin Library (MIT License) + Optional AlAdhan REST API.\n" +
                            "• Quran Text, Translations & Recitation: AlQuran.cloud API (Tanzil Uthmani Arabic, Muhiuddin Khan Bengali, Saheeh International English, Mishary Rashid Alafasy Audio).\n" +
                            "• Hadith & Dua Sources: Sahih al-Bukhari, Sahih Muslim, Riyad as-Salihin, Al-Arba'in an-Nawawiyyah, Hisnul Muslim + Optional Sunnah.com API.\n" +
                            "• Privacy Guarantee: Serverless architecture. No Firebase, no advertising SDKs, no user accounts, and no third-party sharing of prayer logs, notes, or coordinates.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

