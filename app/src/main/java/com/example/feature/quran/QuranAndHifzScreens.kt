package com.example.feature.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.database.CachedAyahEntity
import com.example.core.utils.BilingualStrings
import com.example.data.quran.SurahCatalog
import com.example.data.quran.SurahLoadResult
import com.example.feature.SalahMateViewModel
import com.example.ui.components.AyahRosetteMedallion
import com.example.ui.components.GlassmorphicContainerCard
import com.example.ui.components.MushafSurahHeaderBanner
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.VerifiedEmerald

@Composable
fun QuranReaderScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    val context = LocalContext.current
    val prefs by viewModel.preferencesState.collectAsStateWithLifecycle()
    val loadState by viewModel.activeSurahLoadState.collectAsStateWithLifecycle()
    val audioState by viewModel.quranAudioState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.quranBookmarks.collectAsStateWithLifecycle()
    val downloads by viewModel.surahDownloads.collectAsStateWithLifecycle()
    val searchResults by viewModel.quranSearchResults.collectAsStateWithLifecycle()
    val totalCachedAyahs by viewModel.totalCachedAyahsCount.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0=Reader, 1=114 Surahs, 2=Juz 1-30, 3=Bookmarks, 4=Downloads & Search
    var searchQuery by remember { mutableStateOf("") }
    var showReaderSettings by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp
        ) {
            val tabs = listOf(
                strings.tr("Surah Reader", "তিলাওয়াত"),
                strings.tr("114 Surahs", "১১৪ সূরা"),
                strings.tr("30 Juz", "৩০ পারা"),
                strings.tr("Bookmarks (${bookmarks.size})", "বুকমার্ক (${bookmarks.size})"),
                strings.tr("Offline & Search", "অফলাইন ও অনুসন্ধান")
            )
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(title) },
                    modifier = Modifier.testTag("quran_tab_$idx")
                )
            }
        }

        // Floating Glassmorphic Audio Recitation Mini-Player when active or preparing
        if (audioState.isPlaying || audioState.isPreparing || audioState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0A2E52),
                                Color(0xFF0F4C81),
                                Color(0xFF1E3A8A)
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.dp, Color(0xFF93C5FD).copy(alpha = 0.65f)),
                        RoundedCornerShape(18.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AyahRosetteMedallion(
                            numberText = "${audioState.currentAyahNumber}",
                            size = 36.dp,
                            tint = Color(0xFFFDE68A)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val surahMeta = SurahCatalog.getSurahMeta(audioState.currentSurahNumber)
                            Text(
                                text = "${surahMeta.nameEn} • Ayah ${audioState.currentAyahNumber}/${audioState.totalAyahsInSurah}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = audioState.errorMessage ?: audioState.reciterLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (audioState.isPreparing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFFDE68A)
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.quranRepo.togglePauseResume() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                            ) {
                                Icon(
                                    imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause Audio",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { viewModel.quranRepo.stopAudio() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "Stop Audio",
                                tint = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Active Surah Reader
                when (val state = loadState) {
                    is SurahLoadResult.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(strings.tr("Loading Surah & Translations...", "সূরা ও অনুবাদ লোড হচ্ছে..."))
                            }
                        }
                    }

                    is SurahLoadResult.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.surahMeta.nameEn,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { viewModel.openSurah(state.surahMeta.number, forceRefresh = true) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.tr("Retry Online Fetch", "পুনরায় চেষ্টা করুন"))
                                }
                                OutlinedButton(onClick = { selectedTab = 1 }) {
                                    Text(strings.tr("Browse All Surahs", "সূরা তালিকা"))
                                }
                            }
                        }
                    }

                    is SurahLoadResult.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("quran_ayah_list"),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MushafSurahHeaderBanner(
                                        surahNumber = state.surahMeta.number,
                                        surahTitle = "${state.surahMeta.nameEn} (${if (strings.isBn) state.surahMeta.nameBn else state.surahMeta.meaningEn})",
                                        surahArabicName = state.surahMeta.nameArabic,
                                        revelationAndAyahs = "${state.surahMeta.revelationType} • ${state.surahMeta.ayahCount} Ayahs • Juz ${state.surahMeta.startJuz}",
                                        showBismillah = state.surahMeta.number != 9
                                    )

                                    GlassmorphicContainerCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        cornerRadius = 18.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.quranRepo.playAyahAudio(state.ayahs, 0, continuousSurah = true)
                                                    },
                                                    shape = RoundedCornerShape(50),
                                                    modifier = Modifier.testTag("play_surah_audio_btn")
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(strings.tr("Play Surah Audio", "সূরা তিলাওয়াত শুনুন"))
                                                }
                                                OutlinedButton(
                                                    onClick = { showReaderSettings = !showReaderSettings },
                                                    shape = RoundedCornerShape(50)
                                                ) {
                                                    Text(strings.tr("Font & Translations", "ফন্ট ও অনুবাদ"))
                                                }
                                            }

                                        if (showReaderSettings) {
                                            HorizontalDivider()
                                            Text(
                                                text = "Arabic Font Size: ${prefs.quranArabicFontSizeSp.toInt()} sp",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            Slider(
                                                value = prefs.quranArabicFontSizeSp,
                                                onValueChange = {
                                                    viewModel.updateQuranDisplaySettings(
                                                        it,
                                                        prefs.quranTranslationFontSizeSp,
                                                        prefs.showBengaliTranslation,
                                                        prefs.showEnglishTranslation
                                                    )
                                                },
                                                valueRange = 20f..44f
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(strings.tr("Show Bengali Translation (Muhiuddin Khan)", "বাংলা অনুবাদ প্রদর্শন (মুহিউদ্দীন খান)"))
                                                Switch(
                                                    checked = prefs.showBengaliTranslation,
                                                    onCheckedChange = {
                                                        viewModel.updateQuranDisplaySettings(
                                                            prefs.quranArabicFontSizeSp,
                                                            prefs.quranTranslationFontSizeSp,
                                                            it,
                                                            prefs.showEnglishTranslation
                                                        )
                                                    }
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(strings.tr("Show English Translation (Saheeh Int.)", "ইংরেজি অনুবাদ প্রদর্শন (সহীহ ইন্টারন্যাশনাল)"))
                                                Switch(
                                                    checked = prefs.showEnglishTranslation,
                                                    onCheckedChange = {
                                                        viewModel.updateQuranDisplaySettings(
                                                            prefs.quranArabicFontSizeSp,
                                                            prefs.quranTranslationFontSizeSp,
                                                            prefs.showBengaliTranslation,
                                                            it
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                            Text(
                                                text = "${if (state.isFromOfflineCache) "Offline Cached Copy" else "Synced from AlQuran.cloud"} • ${state.attributionNotice}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            itemsIndexed(state.ayahs, key = { _, a -> "${a.surahNumber}_${a.ayahNumberInSurah}" }) { idx, ayah ->
                                val isBookmarked = bookmarks.any { it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumberInSurah }
                                AyahStudyCard(
                                    ayah = ayah,
                                    arabicFontSizeSp = prefs.quranArabicFontSizeSp,
                                    translationFontSizeSp = prefs.quranTranslationFontSizeSp,
                                    showBengali = prefs.showBengaliTranslation,
                                    showEnglish = prefs.showEnglishTranslation,
                                    isBookmarked = isBookmarked,
                                    onPlayAudio = {
                                        viewModel.quranRepo.playAyahAudio(state.ayahs, idx, continuousSurah = true)
                                    },
                                    onToggleBookmark = {
                                        viewModel.toggleBookmarkAyah(
                                            surahNumber = ayah.surahNumber,
                                            ayahNumber = ayah.ayahNumberInSurah,
                                            arabicText = ayah.arabicUthmani,
                                            translationText = ayah.bengaliTranslation.ifBlank { ayah.englishTranslation }
                                        )
                                    },
                                    onMarkLastRead = {
                                        viewModel.markAyahAsLastRead(
                                            surahNumber = ayah.surahNumber,
                                            surahNameEn = state.surahMeta.nameEn,
                                            ayahNumber = ayah.ayahNumberInSurah
                                        )
                                    },
                                    onCopy = {
                                        copyAyahToClipboard(context, state.surahMeta.nameEn, ayah)
                                        viewModel.showStatusMessage("Copied ${state.surahMeta.nameEn} ${ayah.surahNumber}:${ayah.ayahNumberInSurah}")
                                    },
                                    onShare = {
                                        shareAyahIntent(context, state.surahMeta.nameEn, ayah)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // All 114 Surahs List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SurahCatalog.ALL_SURAHS, key = { it.number }) { meta ->
                        val isDownloaded = downloads.any { it.surahNumber == meta.number && it.status == "DOWNLOADED" }
                        OutlinedCard(
                            onClick = {
                                viewModel.openSurah(meta.number)
                                selectedTab = 0
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("surah_item_${meta.number}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AyahRosetteMedallion(
                                        numberText = "${meta.number}",
                                        size = 40.dp,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${meta.nameEn} • ${meta.nameBn}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${meta.meaningEn} (${meta.meaningBn}) • ${meta.ayahCount} Ayahs",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = meta.nameArabic,
                                        fontFamily = AmiriQuranFontFamily,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (isDownloaded) {
                                        Text(
                                            text = strings.tr("Offline Ready", "অফলাইন"),
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

            2 -> {
                // 30 Juz Navigation Index
                val juzGroups = remember {
                    (1..30).map { juz ->
                        juz to SurahCatalog.ALL_SURAHS.filter { it.startJuz == juz }
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(juzGroups, key = { it.first }) { (juzNum, surahsInJuz) ->
                        OutlinedCard(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.tr("Juz (Para) $juzNum", "পারা $juzNum"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                surahsInJuz.forEach { s ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.openSurah(s.number)
                                                selectedTab = 0
                                            }
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${s.number}. ${s.nameEn} (${s.nameBn})")
                                        Text("${s.ayahCount} Ayahs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Bookmarks List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (bookmarks.isEmpty()) {
                        item {
                            Text(
                                text = strings.tr(
                                    "No Quran bookmarks yet. Tap the bookmark icon on any Ayah while reading.",
                                    "এখনও কোনো আয়াত বুকমার্ক করা হয়নি। পড়ার সময় যেকোনো আয়াতের বুকমার্ক আইকনে ট্যাপ করুন।"
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    items(bookmarks, key = { it.id }) { bm ->
                        OutlinedCard(
                            onClick = {
                                viewModel.openSurah(bm.surahNumber)
                                selectedTab = 0
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${bm.surahNameEn} ${bm.surahNumber}:${bm.ayahNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { viewModel.deleteBookmark(bm.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Bookmark")
                                    }
                                }
                                Text(
                                    text = bm.arabicSnippet,
                                    fontFamily = AmiriQuranFontFamily,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = bm.translationSnippet,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            4 -> {
                // Search & Offline Download Manager
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchQuranOfflineCache(it)
                            },
                            label = { Text(strings.tr("Search Arabic, Bengali, or English in Cached Surahs", "ক্যাশকৃত কুরআনে আরবি, বাংলা বা ইংরেজি খুঁজুন")) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quran_search_input")
                        )
                    }

                    if (searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = strings.tr("Search Matches (${searchResults.size})", "অনুসন্ধান ফলাফল (${searchResults.size})"),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(searchResults, key = { "search_${it.surahNumber}_${it.ayahNumberInSurah}" }) { ayah ->
                            OutlinedCard(
                                onClick = {
                                    viewModel.openSurah(ayah.surahNumber)
                                    selectedTab = 0
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Surah ${ayah.surahNumber} : Ayah ${ayah.ayahNumberInSurah}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = ayah.arabicUthmani,
                                        fontFamily = AmiriQuranFontFamily,
                                        fontSize = 20.sp,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(text = ayah.bengaliTranslation, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = ayah.englishTranslation, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider()
                        val totalBytes = downloads.sumOf { it.bytesUsed }
                        val kbUsed = totalBytes / 1024
                        Text(
                            text = strings.tr(
                                "Offline Quran Storage Manager ($totalCachedAyahs Ayahs cached • ~${kbUsed} KB)",
                                "অফলাইন কুরআন ডাউনলোড ম্যানেজার ($totalCachedAyahs আয়াত সংরক্ষিত • ~${kbUsed} KB)"
                            ),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = strings.tr(
                                "Download user-selected Surahs from AlQuran.cloud for permanent offline access.",
                                "স্থায়ী অফলাইন পাঠের জন্য পছন্দের সূরাসমূহ ডাউনলোড করুন।"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(SurahCatalog.ALL_SURAHS.take(30), key = { "dl_${it.number}" }) { meta ->
                        val dl = downloads.find { it.surahNumber == meta.number }
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${meta.number}. ${meta.nameEn} (${meta.nameBn})", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "Status: ${dl?.status ?: "NOT_DOWNLOADED"} • ${meta.ayahCount} Ayahs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (dl?.status == "DOWNLOADING") {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                                    }
                                }
                                if (dl?.status == "DOWNLOADED") {
                                    IconButton(onClick = { viewModel.removeOfflineSurah(meta.number) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Offline Surah")
                                    }
                                } else {
                                    IconButton(onClick = { viewModel.downloadSurahOffline(meta.number) }) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = "Download Surah")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AyahStudyCard(
    ayah: CachedAyahEntity,
    arabicFontSizeSp: Float,
    translationFontSizeSp: Float,
    showBengali: Boolean,
    showEnglish: Boolean,
    isBookmarked: Boolean,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onMarkLastRead: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    GlassmorphicContainerCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AyahRosetteMedallion(
                        numberText = "${ayah.ayahNumberInSurah}",
                        size = 38.dp,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Ayah ${ayah.surahNumber}:${ayah.ayahNumberInSurah}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Juz ${ayah.juzNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Play Ayah Audio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Ayah",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(onClick = onMarkLastRead, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.DoneAll,
                            contentDescription = "Mark as Last Read",
                            tint = VerifiedEmerald,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy Ayah",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Ayah",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Mushaf-framed Vocalized Uthmani Arabic Text (Strictly Separated from Translation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = ayah.arabicUthmani,
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = arabicFontSizeSp.sp,
                    lineHeight = (arabicFontSizeSp * 1.72f).sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showBengali && ayah.bengaliTranslation.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = ayah.bengaliTranslation,
                        fontSize = translationFontSizeSp.sp,
                        lineHeight = (translationFontSizeSp * 1.48f).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (showEnglish && ayah.englishTranslation.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = ayah.englishTranslation,
                        fontSize = translationFontSizeSp.sp,
                        lineHeight = (translationFontSizeSp * 1.48f).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun copyAyahToClipboard(context: Context, surahName: String, ayah: CachedAyahEntity) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val text = buildString {
        append(ayah.arabicUthmani).append("\n\n")
        if (ayah.bengaliTranslation.isNotBlank()) append(ayah.bengaliTranslation).append("\n")
        if (ayah.englishTranslation.isNotBlank()) append(ayah.englishTranslation).append("\n")
        append("— Quran $surahName (${ayah.surahNumber}:${ayah.ayahNumberInSurah})")
    }
    cm.setPrimaryClip(ClipData.newPlainText("Quran Ayah", text))
}

private fun shareAyahIntent(context: Context, surahName: String, ayah: CachedAyahEntity) {
    val text = buildString {
        append(ayah.arabicUthmani).append("\n\n")
        if (ayah.bengaliTranslation.isNotBlank()) append(ayah.bengaliTranslation).append("\n\n")
        if (ayah.englishTranslation.isNotBlank()) append(ayah.englishTranslation).append("\n\n")
        append("— Holy Quran: $surahName ${ayah.surahNumber}:${ayah.ayahNumberInSurah}")
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Ayah"))
}

// --- 2.6 Quran Memorization / Hifz Tracker Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HifzTrackerScreen(
    viewModel: SalahMateViewModel,
    strings: BilingualStrings
) {
    BackHandler { viewModel.navigateBack() }
    val hifzList by viewModel.hifzProgressList.collectAsStateWithLifecycle()

    var selectedSurahNum by remember { mutableIntStateOf(114) }
    val selectedMeta = SurahCatalog.getSurahMeta(selectedSurahNum)
    var memorizedCount by remember(selectedSurahNum) { mutableIntStateOf(selectedMeta.ayahCount) }
    var selectedStatus by remember { mutableStateOf("MEMORIZED") }
    var personalNotes by remember { mutableStateOf("") }

    val totalMemorizedAyahs = hifzList.sumOf { it.memorizedAyahs }
    val memorizedSurahsCount = hifzList.count { it.status == "MEMORIZED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hifz_tracker_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = strings.tr("Hifz & Quran Memorization Tracker", "হিফয ও কুরআন মুখস্থ ট্র্যাকার"),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.tr(
                            "Stored 100% locally in Room. Progress is logged manually by you—never auto-marked by audio playback.",
                            "সম্পূর্ণ অফলাইনে সংরক্ষিত। আপনার নিজস্ব মূল্যায়নের ভিত্তিতে মুখস্থ ও রিভিশন অগ্রগতি সংরক্ষণ করুন।"
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$memorizedSurahsCount", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                            Text(strings.tr("Surahs Memorized", "মুখস্থকৃত সূরা"), style = MaterialTheme.typography.labelLarge)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalMemorizedAyahs", style = MaterialTheme.typography.displayMedium, color = VerifiedEmerald)
                            Text(strings.tr("Total Ayahs Logged", "মোট মুখস্থ আয়াত"), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

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
                        text = strings.tr("Log Surah Memorization / Revision", "নতুন সূরা বা রিভিশন যুক্ত করুন"),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = strings.tr("Select Surah (Quick Juz 30 & Common Surahs):", "সূরা নির্বাচন করুন:"),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 18, 36, 55, 67, 78, 87, 93, 94, 97, 103, 108, 109, 110, 112, 113, 114).forEach { num ->
                            val m = SurahCatalog.getSurahMeta(num)
                            FilterChip(
                                selected = selectedSurahNum == num,
                                onClick = {
                                    selectedSurahNum = num
                                    memorizedCount = m.ayahCount
                                },
                                label = { Text("${m.number}. ${m.nameEn}") }
                            )
                        }
                    }

                    Text(
                        text = "${selectedMeta.nameEn}: $memorizedCount / ${selectedMeta.ayahCount} Ayahs",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = memorizedCount.toFloat(),
                        onValueChange = { memorizedCount = it.toInt() },
                        valueRange = 0f..selectedMeta.ayahCount.toFloat()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LEARNING", "NEEDS_REVISION", "MEMORIZED").forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st.replace("_", " ")) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = personalNotes,
                        onValueChange = { personalNotes = it },
                        label = { Text(strings.tr("Personal Tajweed / Revision Notes", "ব্যক্তিগত নোট বা তাজবীদ রিমাইন্ডার")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveHifzEntry(
                                surahNumber = selectedSurahNum,
                                memorizedAyahs = memorizedCount,
                                status = selectedStatus,
                                notes = personalNotes
                            )
                            personalNotes = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_hifz_button")
                    ) {
                        Text(strings.tr("Save Hifz Progress", "হিফয অগ্রগতি সংরক্ষণ করুন"))
                    }
                }
            }
        }

        items(hifzList, key = { it.surahNumber }) { item ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.surahNumber}. ${item.surahNameEn} (${item.status})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Memorized: ${item.memorizedAyahs} / ${item.totalAyahs} Ayahs • Next Review: ${item.nextReviewDateIso}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (item.personalNotes.isNotBlank()) {
                            Text(
                                text = "Note: ${item.personalNotes}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.deleteHifzEntry(item.surahNumber) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Hifz Record")
                    }
                }
            }
        }
    }
}
