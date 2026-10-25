package com.example.feature.worship

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.utils.BilingualStrings
import com.example.data.hadith.VerifiedIslamicContent
import com.example.feature.AppDestination
import com.example.feature.SalahMateViewModel
import com.example.ui.components.AyahRosetteMedallion
import com.example.ui.components.GlassmorphicContainerCard
import com.example.ui.components.GlassmorphicModalDialog
import com.example.ui.components.IslamicGeometricBackground
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.LocalIslamicThemePalette
import com.example.ui.theme.VerifiedEmerald
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WorshipHubScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    val themePalette = LocalIslamicThemePalette.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("worship_hub_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = themePalette.heroBannerGradient
                        )
                    )
                    .border(
                        BorderStroke(1.dp, themePalette.accentGoldLight.copy(alpha = 0.6f)),
                        RoundedCornerShape(22.dp)
                    )
            ) {
                IslamicGeometricBackground(
                    modifier = Modifier.matchParentSize(),
                    patternColor = themePalette.accentGoldLight,
                    patternAlpha = 0.08f
                )
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.tr("Daily Worship, Remembrance & Study", "দৈনিক ইবাদত, যিকির ও ইসলামিক অধ্যয়ন"),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "All modules operate offline with verified references and local progress persistence.",
                            "সকল মডিউল যাচাইকৃত সূত্রসহ সম্পূর্ণ অফলাইনে কাজ করে।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        val items = listOf(
            Triple(strings.dua, strings.tr("Morning/Evening Adhkar, Sleep, Prayer, Travel & Protection", "সকাল-সন্ধ্যার যিকির, ঘুম, নামাজ, সফর ও হেফাজতের দোয়া"), AppDestination.DUA),
            Triple(strings.hadith, strings.tr("Sahih Bukhari, Muslim, Riyad as-Salihin & 40 Nawawi", "সহীহ বুখারী, মুসলিম, রিয়াদুস সালেহীন ও ৪০ হাদিস"), AppDestination.HADITH),
            Triple(strings.tasbih, strings.tr("Tap-to-count 3D Digital Tasbih with haptics & saved sessions", "হ্যাপটিক ফিডব্যাক ও সেভড সেশনসহ থ্রিডি ডিজিটাল তাসবীহ"), AppDestination.TASBIH),
            Triple(strings.hifz, strings.tr("Quran Memorization goals, revision states & notes", "কুরআন মুখস্থ লক্ষ্য, রিভিশন ও ব্যক্তিগত নোট"), AppDestination.HIFZ),
            Triple(strings.tracker, strings.tr("Private daily 5-prayer completion checklist & stats", "দৈনিক ৫ ওয়াক্ত নামাজের ব্যক্তিগত চেকলিস্ট ও পরিসংখ্যান"), AppDestination.PRAYER_TRACKER),
            Triple(strings.learning, strings.tr("Asma ul Husna, Kalimas, Ayatul Kursi & Wudu Guide", "আসমাউল হুসনা, কালিমা, আয়াতুল কুরসি ও ওযু শিক্ষা"), AppDestination.LEARNING)
        )

        items(items) { (title, subtitle, dest) ->
            GlassmorphicContainerCard(
                onClick = { viewModel.navigateTo(dest) },
                cornerRadius = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worship_card_${dest.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AyahRosetteMedallion(
                            numberText = "۞",
                            size = 42.dp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// --- 2.7 Hadith Library Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HadithLibraryScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val favorites by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val remoteHadiths by viewModel.remoteHadiths.collectAsStateWithLifecycle()

    var selectedCollection by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val combinedList = remember(selectedCollection, searchQuery, remoteHadiths) {
        (VerifiedIslamicContent.VERIFIED_HADITHS + remoteHadiths).filter { h ->
            val matchesCollection = selectedCollection == "All" || h.collectionNameEn.contains(selectedCollection, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                h.englishTranslation.contains(searchQuery, ignoreCase = true) ||
                h.bengaliTranslation.contains(searchQuery, ignoreCase = true) ||
                h.sourceReference.contains(searchQuery, ignoreCase = true)
            matchesCollection && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hadith_library_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.hadith,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { viewModel.fetchOptionalSunnahApiHadiths("bukhari") },
                        modifier = Modifier.testTag("sunnah_api_sync_button")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.tr("Sunnah.com Sync", "সুন্নাহ এপিআই"))
                    }
                }
                Text(
                    text = strings.tr(
                        "Verified primary Hadith texts with explicit collection, book, number, and grading attribution.",
                        "গ্রন্থ, অধ্যায়, হাদিস নম্বর এবং তাহকীক/মান উল্লেখসহ বিশুদ্ধ হাদিস সংকলন।"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(strings.tr("Search Hadith text or reference...", "হাদিস বা সূত্র অনুসন্ধান করুন...")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VerifiedIslamicContent.HADITH_COLLECTIONS.forEach { col ->
                        FilterChip(
                            selected = selectedCollection == col,
                            onClick = { selectedCollection = col },
                            label = { Text(col) }
                        )
                    }
                }
            }
        }

        items(combinedList, key = { it.id }) { hadith ->
            val isSaved = favorites.any { it.itemKey == hadith.id }
            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${if (strings.isBn) hadith.collectionNameBn else hadith.collectionNameEn} • #${hadith.hadithNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Book ${hadith.bookNumber}: ${hadith.bookNameEn} • Narrator: ${hadith.narratorEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.toggleFavoriteItem(hadith.id, "HADITH", "${hadith.collectionNameEn} #${hadith.hadithNumber}")
                            }
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bookmark Hadith",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hadith.arabicText.isNotBlank()) {
                        Text(
                            text = hadith.arabicText,
                            fontFamily = AmiriQuranFontFamily,
                            fontSize = 24.sp,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = hadith.bengaliTranslation,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = hadith.englishTranslation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Grading: ${hadith.grading}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedEmerald
                        )
                        Text(
                            text = hadith.sourceReference,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

// --- 2.8 Dua and Daily Adhkar Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuaAndAdhkarScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val favorites by viewModel.favoriteItems.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredDuas = remember(selectedCategory) {
        VerifiedIslamicContent.VERIFIED_DUAS.filter {
            selectedCategory == "All" || it.categoryEn.equals(selectedCategory, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dua_adhkar_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = strings.dua,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.tr(
                    "Authentic Quranic and Prophetic supplications (Hisnul Muslim / Sahihain) with offline completion tracking.",
                    "কুরআন ও সহীহ হাদিসে বর্ণিত মাসনূন দোয়া ও সকাল-সন্ধ্যার যিকির।"
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VerifiedIslamicContent.DUA_CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filteredDuas, key = { it.id }) { dua ->
            val isCompleted = favorites.any { it.itemKey == dua.id }
            OutlinedCard(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.isBn) dua.titleBn else dua.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (strings.isBn) dua.categoryBn else dua.categoryEn} • Recommended: ${dua.recommendedCount}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleFavoriteItem(dua.id, "DUA", dua.titleEn) }
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.FavoriteBorder,
                                contentDescription = "Mark Adhkar Completed / Favorite",
                                tint = if (isCompleted) VerifiedEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = dua.arabicText,
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 26.sp,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = dua.bengaliMeaning,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = dua.englishMeaning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Source: ${dua.sourceReference}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- 2.10 Digital Tasbih Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DigitalTasbihScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val haptic = LocalHapticFeedback.current
    val sessions by viewModel.tasbihSessions.collectAsStateWithLifecycle()

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showAddPhraseDialog by remember { mutableStateOf(false) }

    val activeSession = sessions.getOrNull(selectedIndex) ?: sessions.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("digital_tasbih_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.tasbih,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = { showAddPhraseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.tr("Custom Dhikr", "নতুন যিকির"))
                }
            }
        }

        if (sessions.isNotEmpty() && activeSession != null) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sessions.forEachIndexed { idx, s ->
                        FilterChip(
                            selected = idx == selectedIndex,
                            onClick = { selectedIndex = idx },
                            label = { Text("${s.phraseTransliteration} (${s.currentCount}/${s.targetCount})") }
                        )
                    }
                }
            }

            item {
                GlassmorphicContainerCard(
                    cornerRadius = 28.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val themePalette = LocalIslamicThemePalette.current
                        // Ornate Dhikr Header Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = themePalette.heroBannerGradient
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, themePalette.accentGoldLight.copy(alpha = 0.55f)),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = activeSession.phraseArabic,
                                    fontFamily = AmiriQuranFontFamily,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themePalette.accentGoldLight,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = activeSession.phraseTransliteration,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (strings.isBn) activeSession.phraseMeaningBn else activeSession.phraseMeaningEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Interactive 3D-Shaded 33-Bead Ring + Center Tactile Medallion
                        val beadCountOnRing = 33
                        val activeBeadInCycle = activeSession.currentCount % beadCountOnRing
                        val animatedPhase by animateFloatAsState(
                            targetValue = activeSession.currentCount.toFloat(),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "tasbih_bead_phase"
                        )
                        val outlineColor = MaterialTheme.colorScheme.outline

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            val ringBoxSize = (maxWidth * 0.80f).coerceIn(220.dp, 320.dp)
                            val centerMedallionSize = ringBoxSize * 0.66f

                            Box(
                                modifier = Modifier.size(ringBoxSize),
                                contentAlignment = Alignment.Center
                            ) {
                                // Outer 3D-Shaded Bead Ring Canvas
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val ringRadius = size.minDimension * 0.43f

                                    // Tasbih cord / string ring
                                    drawCircle(
                                        color = outlineColor.copy(alpha = 0.35f),
                                        radius = ringRadius,
                                        center = center,
                                        style = Stroke(width = 3.dp.toPx())
                                    )

                                    // Draw 33 3D-shaded beads around the ring
                                    for (i in 0 until beadCountOnRing) {
                                        val angleDeg = (i * (360f / beadCountOnRing)) - 90f + (animatedPhase * (360f / beadCountOnRing) * 0.15f)
                                        val angleRad = angleDeg * (PI / 180.0)
                                        val bx = center.x + (ringRadius * cos(angleRad)).toFloat()
                                        val by = center.y + (ringRadius * sin(angleRad)).toFloat()
                                        val beadCenter = Offset(bx, by)

                                        val isPassed = i < activeBeadInCycle || (activeSession.currentCount > 0 && activeBeadInCycle == 0)
                                        val isCurrentHead = i == activeBeadInCycle
                                        val beadRadius = when {
                                            isCurrentHead -> 9.5.dp.toPx()
                                            i % 11 == 0 -> 8.2.dp.toPx() // Imam / divider bead every 11 beads
                                            else -> 6.8.dp.toPx()
                                        }

                                        if (isCurrentHead) {
                                            drawCircle(
                                                color = themePalette.accentGoldLight.copy(alpha = 0.42f),
                                                radius = beadRadius * 1.65f,
                                                center = beadCenter
                                            )
                                        }

                                        val beadBrush = when {
                                            isCurrentHead -> Brush.radialGradient(
                                                colors = themePalette.tasbihHeadBeadColors,
                                                center = Offset(bx - beadRadius * 0.3f, by - beadRadius * 0.3f),
                                                radius = beadRadius * 1.3f
                                            )
                                            isPassed -> Brush.radialGradient(
                                                colors = themePalette.tasbihActiveBeadColors,
                                                center = Offset(bx - beadRadius * 0.3f, by - beadRadius * 0.3f),
                                                radius = beadRadius * 1.3f
                                            )
                                            else -> Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFE2E8F0),
                                                    Color(0xFF64748B),
                                                    Color(0xFF334155)
                                                ),
                                                center = Offset(bx - beadRadius * 0.3f, by - beadRadius * 0.3f),
                                                radius = beadRadius * 1.3f
                                            )
                                        }

                                        drawCircle(
                                            brush = beadBrush,
                                            radius = beadRadius,
                                            center = beadCenter
                                        )
                                    }
                                }

                                // Center Tactile Tap Medallion
                                Box(
                                    modifier = Modifier
                                        .size(centerMedallionSize)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = themePalette.tasbihMedallionGradient
                                            )
                                        )
                                        .border(
                                            BorderStroke(
                                                2.dp,
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        themePalette.accentGoldLight,
                                                        themePalette.swatchPrimary,
                                                        themePalette.accentGold
                                                    )
                                                )
                                            ),
                                            CircleShape
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.incrementTasbih(activeSession)
                                        }
                                        .testTag("tasbih_tap_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${activeSession.currentCount}",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontSize = if (ringBoxSize < 250.dp) 34.sp else 42.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            modifier = Modifier.testTag("tasbih_count_text")
                                        )
                                        Text(
                                            text = "/ ${activeSession.targetCount}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = themePalette.accentGoldLight,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = strings.tr("TAP TO COUNT", "গণনা করতে ট্যাপ করুন"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        val progress = (activeSession.currentCount.toFloat() / activeSession.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        // Target Presets & Reset Button (FlowRow prevents crowding on compact phones)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(33, 34, 100, 1000).forEach { target ->
                                    FilterChip(
                                        selected = activeSession.targetCount == target,
                                        onClick = {
                                            viewModel.updateTasbihTargetAndNote(activeSession, target, activeSession.personalNote)
                                        },
                                        label = {
                                            Text(
                                                text = "$target",
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { showResetConfirmation = true },
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("tasbih_reset_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.tr("Reset", "রিসেট"),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Text(
                            text = strings.tr(
                                "Lifetime Total for ${activeSession.phraseTransliteration}: ${activeSession.totalLifetimeCount}",
                                "মোট পাঠ সংখ্যা: ${activeSession.totalLifetimeCount}"
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = VerifiedEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirmation && activeSession != null) {
        GlassmorphicModalDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = strings.tr("Reset Current Session?", "বর্তমান গণনা শূন্য করবেন?"),
            subtitle = activeSession.phraseTransliteration,
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text(strings.tr("Cancel", "বাতিল"), maxLines = 1)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetTasbihCurrentCount(activeSession)
                        showResetConfirmation = false
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("confirm_tasbih_reset_btn")
                ) {
                    Text(strings.tr("Confirm Reset", "হ্যাঁ, রিসেট করুন"), maxLines = 1)
                }
            }
        ) {
            Text(
                text = strings.tr(
                    "Reset ${activeSession.phraseTransliteration} from ${activeSession.currentCount} back to 0? Your lifetime total (${activeSession.totalLifetimeCount}) will be preserved.",
                    "বর্তমান গণনা (${activeSession.currentCount}) শূন্য করা হবে, তবে মোট সংরক্ষিত গণনা (${activeSession.totalLifetimeCount}) অক্ষুণ্ণ থাকবে।"
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showAddPhraseDialog) {
        var arText by remember { mutableStateOf("") }
        var trText by remember { mutableStateOf("") }
        var enText by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("100") }

        GlassmorphicModalDialog(
            onDismissRequest = { showAddPhraseDialog = false },
            title = strings.tr("Add Custom Dhikr Phrase", "নতুন যিকির যুক্ত করুন"),
            subtitle = strings.tr("Saved locally to your Tasbih presets", "আপনার তাসবীহ তালিকায় সংরক্ষিত থাকবে"),
            dismissButton = {
                TextButton(onClick = { showAddPhraseDialog = false }) {
                    Text(strings.tr("Cancel", "বাতিল"), maxLines = 1)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (trText.isNotBlank()) {
                            viewModel.addCustomTasbihPhrase(
                                arText,
                                trText,
                                enText.ifBlank { trText },
                                targetText.toIntOrNull() ?: 100
                            )
                            showAddPhraseDialog = false
                        }
                    },
                    shape = RoundedCornerShape(50)
                ) {
                    Text(strings.tr("Save", "সংরক্ষণ"), maxLines = 1)
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = arText,
                    onValueChange = { arText = it },
                    label = { Text("Arabic Text (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = trText,
                    onValueChange = { trText = it },
                    label = { Text("Transliteration (e.g. SubhanAllahi wa bihamdihi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = enText,
                    onValueChange = { enText = it },
                    label = { Text("Meaning") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Count") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// --- 2.15 Islamic Learning Screen ---
@Composable
fun IslamicLearningScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Asma ul Husna, 1=Kalimas, 2=Ayatul Kursi, 3=Wudu Guide

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("islamic_learning_screen")
    ) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
            listOf(
                strings.tr("Asma ul Husna", "আসমাউল হুসনা"),
                strings.tr("Kalimas", "কালিমাসমূহ"),
                strings.tr("Ayatul Kursi", "আয়াতুল কুরসি"),
                strings.tr("Wudu Guide", "ওযু শিক্ষা")
            ).forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(VerifiedIslamicContent.ASMA_UL_HUSNA, key = { it.number }) { item ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${item.number}. ${item.transliteration}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(item.meaningBn, style = MaterialTheme.typography.bodyLarge)
                                    Text(item.meaningEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = item.arabic,
                                    fontFamily = AmiriQuranFontFamily,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(VerifiedIslamicContent.KALIMAS, key = { it.number }) { k ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (strings.isBn) k.titleBn else k.titleEn,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = k.arabic,
                                    fontFamily = AmiriQuranFontFamily,
                                    fontSize = 26.sp,
                                    lineHeight = 40.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(text = k.transliteration, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                HorizontalDivider()
                                Text(text = k.meaningBn, style = MaterialTheme.typography.bodyLarge)
                                Text(text = k.meaningEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = strings.tr("Ayatul Kursi — Surah Al-Baqarah 2:255", "আয়াতুল কুরসি — সূরা আল-বাকারা ২:২৫৫"),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = VerifiedIslamicContent.AYATUL_KURSI_ARABIC,
                                    fontFamily = AmiriQuranFontFamily,
                                    fontSize = 28.sp,
                                    lineHeight = 46.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = VerifiedIslamicContent.AYATUL_KURSI_TRANSLITERATION,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                HorizontalDivider()
                                Text(
                                    text = VerifiedIslamicContent.AYATUL_KURSI_MEANING_BN,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = VerifiedIslamicContent.AYATUL_KURSI_MEANING_EN,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            3 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(VerifiedIslamicContent.WUDU_STEPS, key = { it.stepNumber }) { step ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Step ${step.stepNumber}: ${if (strings.isBn) step.titleBn else step.titleEn}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(text = step.descriptionBn, style = MaterialTheme.typography.bodyLarge)
                                Text(text = step.descriptionEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "Reference: ${step.referenceNote}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedEmerald
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
