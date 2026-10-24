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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.CautionAmber
import com.example.ui.theme.VerifiedEmerald
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun ToolsHubScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tools_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = strings.tr("Islamic Tools, Calendars & Settings", "ইসলামিক টুলস, ক্যালেন্ডার ও সেটিংস"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
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
            OutlinedCard(
                onClick = { viewModel.navigateTo(dest) },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tool_card_${dest.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// --- 2.9 Qibla Compass Screen ---
@Composable
fun QiblaCompassScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val qibla by viewModel.qiblaInfo.collectAsStateWithLifecycle()
    val compass by viewModel.compassState.collectAsStateWithLifecycle()
    var numericModeOnly by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("qibla_compass_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${strings.qibla} • ${prefs.cityName}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("qibla_bearing_text")
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(strings.tr("Numeric Bearing Mode (No Sensor Needed)", "শুধু সংখ্যাসূচক ডিগ্রি মোড"))
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
                                CautionAmber.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sensorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (!numericModeOnly) {
                        val dialRotation = if (compass.hasSensor) -compass.deviceHeadingDegrees else 0f
                        val qiblaRelativeAngle = (qibla.bearingFromTrueNorthDegrees.toFloat() + dialRotation)

                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2f
                                val center = Offset(size.width / 2f, size.height / 2f)

                                // Outer Compass Ring
                                drawCircle(
                                    color = outlineColor,
                                    radius = radius,
                                    center = center,
                                    style = Stroke(width = 6f)
                                )

                                // North Pointer on Rotating Dial
                                rotate(degrees = dialRotation, pivot = center) {
                                    drawLine(
                                        color = outlineColor,
                                        start = center,
                                        end = Offset(center.x, center.y - radius + 16f),
                                        strokeWidth = 5f
                                    )
                                }

                                // Qibla Needle pointing toward Makkah
                                rotate(degrees = qiblaRelativeAngle, pivot = center) {
                                    drawLine(
                                        color = primaryColor,
                                        start = center,
                                        end = Offset(center.x, center.y - radius + 8f),
                                        strokeWidth = 12f
                                    )
                                    drawCircle(
                                        color = VerifiedEmerald,
                                        radius = 14f,
                                        center = Offset(center.x, center.y - radius + 18f)
                                    )
                                }

                                drawCircle(color = primaryColor, radius = 10f, center = center)
                            }
                        }
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.tr("Moon-Sighting Adjustment:", "চাঁদ দেখা সমন্বয় (দিন):"))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (-2..2).forEach { adj ->
                                FilterChip(
                                    selected = prefs.hijriAdjustmentDays == adj,
                                    onClick = { viewModel.setHijriAdjustment(adj) },
                                    label = { Text(if (adj >= 0) "+$adj" else "$adj") }
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

    val todayLog = logs.firstOrNull { it.dateIso == schedule.dateIso }
    var reflectionText by remember(todayLog?.personalReflection) { mutableStateOf(todayLog?.personalReflection ?: "") }

    val totalLoggedDays = logs.size
    val totalPrayersCompleted = logs.sumOf { it.completedCount }

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
                            "100% Private & Local: Your prayer checklist never leaves your device.",
                            "১০০% ব্যক্তিগত ও অফলাইন: আপনার নামাজের হিসাব শুধুমাত্র আপনার ফোনেই সংরক্ষিত থাকে।"
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

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.exportUserDataJson()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
                viewModel.showStatusMessage("Backup exported successfully.")
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
                    viewModel.importUserDataJson(content)
                }
            }
        }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("Appearance & Language", "থিম ও ভাষা"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.languageCode == "en",
                            onClick = { if (prefs.languageCode != "en") viewModel.toggleLanguage() },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = prefs.languageCode == "bn",
                            onClick = { if (prefs.languageCode != "bn") viewModel.toggleLanguage() },
                            label = { Text("বাংলা (Bengali)") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePreference.entries.forEach { t ->
                            FilterChip(
                                selected = prefs.themePreference == t,
                                onClick = { viewModel.setThemePreference(t) },
                                label = { Text(t.name) }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.tr("Local Data Backup & Restore (JSON)", "ব্যক্তিগত ডেটা ব্যাকআপ ও রিস্টোর (JSON)"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Export or restore your Quran bookmarks, Hifz memorization logs, prayer tracker history, and Tasbih sessions using Android's system file picker. Cached Quran Ayahs currently stored: $cachedAyahsCount.",
                            "আপনার কুরআন বুকমার্ক, হিফয অগ্রগতি, নামাজ ট্র্যাকার ও তাসবীহ সেশন ফাইল পিকারের মাধ্যমে ব্যাকআপ ও রিস্টোর করুন।"
                        ),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("salahmate_backup.json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.tr("Export Backup", "ব্যাকআপ এক্সপোর্ট"))
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.tr("Import Backup", "ব্যাকআপ ইমপোর্ট"))
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
                        text = "• Prayer & Qibla Engine: Batoulapps Adhan Java/Kotlin Library (MIT License) + Optional AlAdhan REST API.\n" +
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
