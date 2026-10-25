package com.example.feature

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.core.database.CachedAyahEntity
import com.example.core.database.FavoriteItemEntity
import com.example.core.database.HifzProgressEntity
import com.example.core.database.PrayerLogEntity
import com.example.core.database.QuranBookmarkEntity
import com.example.core.database.ReadingHistoryEntity
import com.example.core.database.SalahMateDatabase
import com.example.core.database.SurahDownloadEntity
import com.example.core.database.TasbihSessionEntity
import com.example.core.database.UserDataBackupManager
import com.example.core.network.NetworkModule
import com.example.core.notifications.NotificationDiagnostics
import com.example.core.notifications.PrayerNotificationScheduler
import com.example.core.notifications.ScheduledPrayerAlarmInfo
import com.example.core.preferences.AsrJuristicSchool
import com.example.core.preferences.CalculationMethodOption
import com.example.core.preferences.NotificationSoundMode
import com.example.core.preferences.PrayerProviderMode
import com.example.core.preferences.PresetCity
import com.example.core.preferences.ThemePreference
import com.example.core.preferences.UserPreferencesRepository
import com.example.core.preferences.UserPreferencesState
import com.example.core.preferences.salahMateDataStore
import com.example.data.hadith.VerifiedHadithItem
import com.example.data.hadith.VerifiedIslamicContent
import com.example.data.quran.QuranAudioPlaybackState
import com.example.data.quran.QuranRepository
import com.example.data.quran.SurahCatalog
import com.example.data.quran.SurahLoadResult
import com.example.domain.prayer.DailyPrayerSchedule
import com.example.domain.prayer.DailyPrayerType
import com.example.domain.prayer.LocalAdhanCalculator
import com.example.domain.prayer.NextPrayerStatus
import com.example.domain.prayer.PrayerRepository
import com.example.domain.qibla.CompassReadingState
import com.example.domain.qibla.QiblaCalculator
import com.example.domain.qibla.QiblaInfo
import com.example.widget.SalahMateWidgetProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class AppDestination {
    HOME,
    PRAYER,
    QURAN,
    WORSHIP,
    TOOLS,
    // Sub-screens
    QIBLA,
    TASBIH,
    DUA,
    HADITH,
    HIFZ,
    RAMADAN,
    CALENDAR,
    ZAKAT,
    PRAYER_TRACKER,
    LEARNING,
    MOSQUE_FINDER,
    SETTINGS
}

class SalahMateViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val db = SalahMateDatabase.getInstance(appContext)
    private val dao = db.salahMateDao()
    private val prefsRepo = UserPreferencesRepository(appContext.salahMateDataStore)
    private val prayerRepo = PrayerRepository(dao)
    val quranRepo = QuranRepository(appContext, dao)

    val preferencesState: StateFlow<UserPreferencesState> = prefsRepo.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesState(onboardingCompleted = true))

    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _navigationBackStack = ArrayDeque<AppDestination>()

    // --- Prayer Schedule & Isolated Countdown Ticker ---
    private val _todaySchedule = MutableStateFlow(
        LocalAdhanCalculator.calculateLocalSchedule(UserPreferencesState())
    )
    val todaySchedule: StateFlow<DailyPrayerSchedule> = _todaySchedule.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow(
        LocalAdhanCalculator.computeNextPrayerStatus(_todaySchedule.value, UserPreferencesState())
    )
    val nextPrayerCountdown: StateFlow<NextPrayerStatus> = _nextPrayerCountdown.asStateFlow()

    private val _qiblaInfo = MutableStateFlow(
        QiblaCalculator.calculateQibla(23.8103, 90.4125)
    )
    val qiblaInfo: StateFlow<QiblaInfo> = _qiblaInfo.asStateFlow()

    val compassState: StateFlow<CompassReadingState> = QiblaCalculator.observeCompassHeading(appContext)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompassReadingState())

    private val _notificationDiagnostics = MutableStateFlow(
        PrayerNotificationScheduler.getDiagnostics(appContext)
    )
    val notificationDiagnostics: StateFlow<NotificationDiagnostics> = _notificationDiagnostics.asStateFlow()

    private val _scheduledAlarmsTable = MutableStateFlow(
        PrayerNotificationScheduler.computeScheduledAlarmsTable(UserPreferencesState())
    )
    val scheduledAlarmsTable: StateFlow<List<ScheduledPrayerAlarmInfo>> = _scheduledAlarmsTable.asStateFlow()

    // --- Quran Reader State ---
    private val _activeSurahLoadState = MutableStateFlow<SurahLoadResult>(SurahLoadResult.Loading)
    val activeSurahLoadState: StateFlow<SurahLoadResult> = _activeSurahLoadState.asStateFlow()

    private val _quranSearchResults = MutableStateFlow<List<CachedAyahEntity>>(emptyList())
    val quranSearchResults: StateFlow<List<CachedAyahEntity>> = _quranSearchResults.asStateFlow()

    val quranAudioState: StateFlow<QuranAudioPlaybackState> = quranRepo.audioState

    val quranBookmarks: StateFlow<List<QuranBookmarkEntity>> = dao.observeQuranBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingHistory: StateFlow<List<ReadingHistoryEntity>> = dao.observeReadingHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val surahDownloads: StateFlow<List<SurahDownloadEntity>> = dao.observeSurahDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCachedAyahsCount: StateFlow<Int> = dao.observeTotalCachedAyahsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Hifz, Prayer Logs, Tasbih & Favorites ---
    val hifzProgressList: StateFlow<List<HifzProgressEntity>> = dao.observeHifzProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prayerLogsList: StateFlow<List<PrayerLogEntity>> = dao.observePrayerLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasbihSessions: StateFlow<List<TasbihSessionEntity>> = dao.observeTasbihSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteItems: StateFlow<List<FavoriteItemEntity>> = dao.observeFavoriteItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Optional Sunnah.com Live Hadith State ---
    private val _remoteHadiths = MutableStateFlow<List<VerifiedHadithItem>>(emptyList())
    val remoteHadiths: StateFlow<List<VerifiedHadithItem>> = _remoteHadiths.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    init {
        viewModelScope.launch {
            quranRepo.ensureBundledSurahsSeeded()
            ensureDefaultTasbihSeeded()
            openSurah(1, forceRefresh = false)
        }

        // Reactively recalculate prayer schedule, Qibla, alarms, and widget when preferences change
        viewModelScope.launch {
            prefsRepo.preferencesFlow.collect { prefs ->
                _qiblaInfo.value = QiblaCalculator.calculateQibla(prefs.latitude, prefs.longitude)
                val schedule = prayerRepo.getScheduleForDate(prefs)
                _todaySchedule.value = schedule
                _nextPrayerCountdown.value = LocalAdhanCalculator.computeNextPrayerStatus(schedule, prefs)
                PrayerNotificationScheduler.scheduleDailyPrayerAlarms(appContext, prefs)
                _scheduledAlarmsTable.value = PrayerNotificationScheduler.computeScheduledAlarmsTable(prefs)
                _notificationDiagnostics.value = PrayerNotificationScheduler.getDiagnostics(appContext)
                SalahMateWidgetProvider.updateAllWidgets(appContext)
            }
        }

        // Isolated 1-second local clock ticker for countdown
        viewModelScope.launch {
            while (isActive) {
                val prefs = preferencesState.value
                val schedule = _todaySchedule.value
                _nextPrayerCountdown.value = LocalAdhanCalculator.computeNextPrayerStatus(
                    schedule,
                    prefs,
                    System.currentTimeMillis()
                )
                delay(1000L)
            }
        }
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun showStatusMessage(msg: String) {
        _statusBannerMessage.value = msg
    }

    // --- Navigation with Backstack ---
    fun navigateTo(destination: AppDestination) {
        if (_currentDestination.value != destination) {
            _navigationBackStack.addLast(_currentDestination.value)
            _currentDestination.value = destination
        }
    }

    fun navigateBack(): Boolean {
        if (_navigationBackStack.isNotEmpty()) {
            _currentDestination.value = _navigationBackStack.removeLast()
            return true
        }
        if (_currentDestination.value != AppDestination.HOME) {
            _currentDestination.value = AppDestination.HOME
            return true
        }
        return false
    }

    // --- Onboarding & Preferences ---
    fun completeOnboardingSetup(
        city: String,
        country: String,
        lat: Double,
        lon: Double,
        tzId: String,
        method: CalculationMethodOption,
        school: AsrJuristicSchool
    ) {
        viewModelScope.launch {
            prefsRepo.completeOnboarding(city, country, lat, lon, tzId, method, school)
            showStatusMessage("Location & calculation settings saved for $city.")
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = preferencesState.value.languageCode
            val next = if (current == "bn") "en" else "bn"
            prefsRepo.setLanguage(next)
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            prefsRepo.setThemePreference(theme)
        }
    }

    fun selectPresetCity(city: PresetCity) {
        viewModelScope.launch {
            prefsRepo.updateLocation(
                city = city.nameEn,
                country = city.countryEn,
                lat = city.latitude,
                lon = city.longitude,
                tzId = city.timeZoneId,
                isManual = false
            )
            showStatusMessage("Location updated to ${city.nameEn}, ${city.countryEn}.")
        }
    }

    fun setCustomCoordinates(cityLabel: String, countryLabel: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            val tzId = TimeZone.getDefault().id
            prefsRepo.updateLocation(
                city = cityLabel.ifBlank { "Custom Coordinates" },
                country = countryLabel.ifBlank { "Manual" },
                lat = lat,
                lon = lon,
                tzId = tzId,
                isManual = true
            )
            showStatusMessage("Manual coordinates (${String.format(Locale.US, "%.4f, %.4f", lat, lon)}) saved.")
        }
    }

    @SuppressLint("MissingPermission")
    fun detectGpsLocationOnce() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        var cityName = "GPS Location"
                        var countryName = ""
                        try {
                            @Suppress("DEPRECATION")
                            val addresses = Geocoder(appContext, Locale.ENGLISH)
                                .getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: "GPS Location"
                                countryName = addresses[0].countryName ?: ""
                            }
                        } catch (_: Exception) { }

                        prefsRepo.updateLocation(
                            city = cityName,
                            country = countryName.ifBlank { "Detected" },
                            lat = location.latitude,
                            lon = location.longitude,
                            tzId = TimeZone.getDefault().id,
                            isManual = true
                        )
                        showStatusMessage("GPS coordinates detected: $cityName (${String.format(Locale.US, "%.3f, %.3f", location.latitude, location.longitude)})")
                    }
                } else {
                    showStatusMessage("Could not acquire a GPS fix right now. You can select any city manually.")
                }
            }
            .addOnFailureListener {
                showStatusMessage("GPS location unavailable. Use manual city or coordinate selection.")
            }
    }

    fun updateCalculationSettings(
        method: CalculationMethodOption,
        school: AsrJuristicSchool,
        providerMode: PrayerProviderMode
    ) {
        viewModelScope.launch {
            prefsRepo.updateCalculationSettings(method, school, providerMode)
        }
    }

    fun updatePrayerOffsets(
        fajr: Int,
        sunrise: Int,
        dhuhr: Int,
        asr: Int,
        maghrib: Int,
        isha: Int,
        imsak: Int
    ) {
        viewModelScope.launch {
            prefsRepo.updatePrayerOffsets(fajr, sunrise, dhuhr, asr, maghrib, isha, imsak)
            showStatusMessage("Prayer minute adjustments saved and schedule updated.")
        }
    }

    fun updateNotificationSettings(
        fajr: Boolean,
        dhuhr: Boolean,
        asr: Boolean,
        maghrib: Boolean,
        isha: Boolean,
        advanceMinutes: Int,
        soundMode: NotificationSoundMode
    ) {
        viewModelScope.launch {
            prefsRepo.updateNotificationSettings(fajr, dhuhr, asr, maghrib, isha, advanceMinutes, soundMode)
            val updatedPrefs = prefsRepo.preferencesFlow.first()
            PrayerNotificationScheduler.scheduleDailyPrayerAlarms(appContext, updatedPrefs)
            _scheduledAlarmsTable.value = PrayerNotificationScheduler.computeScheduledAlarmsTable(updatedPrefs)
            _notificationDiagnostics.value = PrayerNotificationScheduler.getDiagnostics(appContext)
        }
    }

    fun syncAndVerifyPrayerAlarmsNow() {
        viewModelScope.launch {
            val prefs = preferencesState.value
            PrayerNotificationScheduler.scheduleDailyPrayerAlarms(appContext, prefs)
            _scheduledAlarmsTable.value = PrayerNotificationScheduler.computeScheduledAlarmsTable(prefs)
            _notificationDiagnostics.value = PrayerNotificationScheduler.getDiagnostics(appContext)
            showStatusMessage("Offline prayer alarms (AlarmManager + WorkManager) synced for ${prefs.cityName}.")
        }
    }

    fun refreshNotificationDiagnostics() {
        val prefs = preferencesState.value
        _scheduledAlarmsTable.value = PrayerNotificationScheduler.computeScheduledAlarmsTable(prefs)
        _notificationDiagnostics.value = PrayerNotificationScheduler.getDiagnostics(appContext)
    }

    fun triggerTestPrayerNotification() {
        val prefs = preferencesState.value
        val next = _nextPrayerCountdown.value
        PrayerNotificationScheduler.showPrayerNotification(
            context = appContext,
            prayerName = next.nextPrayer.titleEn,
            prayerTimeFormatted = next.nextPrayerTimeFormatted,
            soundMode = prefs.notificationSoundMode,
            isAdvanceReminder = false,
            isTestPreview = true,
            prayerType = next.nextPrayer
        )
        _notificationDiagnostics.value = PrayerNotificationScheduler.getDiagnostics(appContext)
        showStatusMessage("Test notification sent (${prefs.notificationSoundMode.labelEn}).")
    }

    fun setHijriAdjustment(days: Int) {
        viewModelScope.launch {
            prefsRepo.setHijriAdjustment(days)
        }
    }

    // --- Quran Reader & Offline Downloads ---
    fun openSurah(surahNumber: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _activeSurahLoadState.value = SurahLoadResult.Loading
            val res = quranRepo.loadSurah(surahNumber, forceRefresh)
            _activeSurahLoadState.value = res
            if (res is SurahLoadResult.Success) {
                prefsRepo.updateLastReadQuran(res.surahMeta.number, res.surahMeta.nameEn, 1)
                recordDailyReadingProgress(res.surahMeta.number, 1, ayahsIncrement = 5)
            }
        }
    }

    fun markAyahAsLastRead(surahNumber: Int, surahNameEn: String, ayahNumber: Int) {
        viewModelScope.launch {
            prefsRepo.updateLastReadQuran(surahNumber, surahNameEn, ayahNumber)
            recordDailyReadingProgress(surahNumber, ayahNumber, ayahsIncrement = 3)
            showStatusMessage("Saved last-read position: $surahNameEn, Ayah $ayahNumber")
        }
    }

    private suspend fun recordDailyReadingProgress(surahNum: Int, ayahNum: Int, ayahsIncrement: Int) {
        val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        val existing = dao.getReadingHistoryForDate(todayIso)
        val updated = ReadingHistoryEntity(
            dateIso = todayIso,
            ayahsReadCount = (existing?.ayahsReadCount ?: 0) + ayahsIncrement,
            minutesSpent = (existing?.minutesSpent ?: 0) + 2,
            lastSurahNumber = surahNum,
            lastAyahNumber = ayahNum
        )
        dao.upsertReadingHistory(updated)
    }

    fun toggleBookmarkAyah(surahNumber: Int, ayahNumber: Int, arabicText: String, translationText: String, note: String = "") {
        viewModelScope.launch {
            val meta = SurahCatalog.getSurahMeta(surahNumber)
            val existing = quranBookmarks.value.find { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
            if (existing != null) {
                dao.deleteQuranBookmark(existing.id)
                showStatusMessage("Removed bookmark for ${meta.nameEn} $surahNumber:$ayahNumber")
            } else {
                dao.insertQuranBookmark(
                    QuranBookmarkEntity(
                        surahNumber = surahNumber,
                        surahNameEn = meta.nameEn,
                        surahNameAr = meta.nameArabic,
                        ayahNumber = ayahNumber,
                        arabicSnippet = arabicText.take(120),
                        translationSnippet = translationText.take(160),
                        personalNote = note
                    )
                )
                showStatusMessage("Bookmarked ${meta.nameEn} $surahNumber:$ayahNumber")
            }
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            dao.deleteQuranBookmark(id)
        }
    }

    fun searchQuranOfflineCache(query: String) {
        viewModelScope.launch {
            if (query.trim().length < 2) {
                _quranSearchResults.value = emptyList()
            } else {
                _quranSearchResults.value = dao.searchCachedAyahs(query.trim())
            }
        }
    }

    fun updateQuranDisplaySettings(arabicFontSize: Float, transFontSize: Float, showBn: Boolean, showEn: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateQuranSettings(arabicFontSize, transFontSize, showBn, showEn)
        }
    }

    fun downloadSurahOffline(surahNumber: Int) {
        quranRepo.startOfflineSurahDownload(viewModelScope, surahNumber)
        showStatusMessage("Downloading Surah #${surahNumber} for offline reading...")
    }

    fun removeOfflineSurah(surahNumber: Int) {
        viewModelScope.launch {
            quranRepo.cancelOrDeleteOfflineSurah(surahNumber)
            showStatusMessage("Cleared cached data for Surah #$surahNumber.")
        }
    }

    // --- Hifz Memorization Tracker (Strictly User-Controlled, Never Auto-Marked by Audio) ---
    fun saveHifzEntry(
        surahNumber: Int,
        memorizedAyahs: Int,
        status: String,
        notes: String
    ) {
        viewModelScope.launch {
            val meta = SurahCatalog.getSurahMeta(surahNumber)
            val nextReviewCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, if (status == "MEMORIZED") 7 else 2)
            }
            val nextDateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(nextReviewCal.time)
            dao.upsertHifzProgress(
                HifzProgressEntity(
                    surahNumber = surahNumber,
                    surahNameEn = meta.nameEn,
                    totalAyahs = meta.ayahCount,
                    memorizedAyahs = memorizedAyahs.coerceIn(0, meta.ayahCount),
                    startAyah = 1,
                    targetAyah = meta.ayahCount,
                    status = status,
                    personalNotes = notes,
                    nextReviewDateIso = nextDateIso,
                    lastReviewedMillis = System.currentTimeMillis()
                )
            )
            showStatusMessage("Updated Hifz status for ${meta.nameEn} ($status).")
        }
    }

    fun deleteHifzEntry(surahNumber: Int) {
        viewModelScope.launch {
            dao.deleteHifzProgress(surahNumber)
        }
    }

    // --- Daily Prayer Tracking ---
    fun togglePrayerCompleted(dateIso: String, prayerType: DailyPrayerType, reflection: String? = null) {
        viewModelScope.launch {
            val existing = dao.getPrayerLogForDate(dateIso) ?: PrayerLogEntity(dateIso = dateIso)
            val updated = when (prayerType) {
                DailyPrayerType.FAJR -> existing.copy(fajrCompleted = !existing.fajrCompleted)
                DailyPrayerType.DHUHR -> existing.copy(dhuhrCompleted = !existing.dhuhrCompleted)
                DailyPrayerType.ASR -> existing.copy(asrCompleted = !existing.asrCompleted)
                DailyPrayerType.MAGHRIB -> existing.copy(maghribCompleted = !existing.maghribCompleted)
                DailyPrayerType.ISHA -> existing.copy(ishaCompleted = !existing.ishaCompleted)
                DailyPrayerType.SUNRISE -> existing
            }.let {
                if (reflection != null) it.copy(personalReflection = reflection, updatedAtMillis = System.currentTimeMillis())
                else it.copy(updatedAtMillis = System.currentTimeMillis())
            }
            dao.upsertPrayerLog(updated)
        }
    }

    fun savePrayerNote(dateIso: String, note: String) {
        viewModelScope.launch {
            val existing = dao.getPrayerLogForDate(dateIso) ?: PrayerLogEntity(dateIso = dateIso)
            dao.upsertPrayerLog(existing.copy(personalReflection = note, updatedAtMillis = System.currentTimeMillis()))
            showStatusMessage("Saved personal prayer reflection for $dateIso.")
        }
    }

    // --- Digital Tasbih ---
    private suspend fun ensureDefaultTasbihSeeded() {
        val current = dao.getAllTasbihSessionsOnce()
        if (current.isEmpty()) {
            val defaults = listOf(
                TasbihSessionEntity(
                    phraseArabic = "سُبْحَانَ ٱللَّٰهِ",
                    phraseTransliteration = "SubhanAllah",
                    phraseMeaningEn = "Glory be to Allah",
                    phraseMeaningBn = "আল্লাহ পবিত্র ও মহিমাময়",
                    targetCount = 33
                ),
                TasbihSessionEntity(
                    phraseArabic = "ٱلْحَمْدُ لِلَّٰهِ",
                    phraseTransliteration = "Alhamdulillah",
                    phraseMeaningEn = "All praise is due to Allah",
                    phraseMeaningBn = "সকল প্রশংসা আল্লাহর জন্য",
                    targetCount = 33
                ),
                TasbihSessionEntity(
                    phraseArabic = "ٱللَّٰهُ أَكْبَرُ",
                    phraseTransliteration = "Allahu Akbar",
                    phraseMeaningEn = "Allah is the Greatest",
                    phraseMeaningBn = "আল্লাহ সর্বশ্রেষ্ঠ",
                    targetCount = 34
                ),
                TasbihSessionEntity(
                    phraseArabic = "أَسْتَغْفِرُ ٱللَّٰهَ",
                    phraseTransliteration = "Astaghfirullah",
                    phraseMeaningEn = "I seek forgiveness from Allah",
                    phraseMeaningBn = "আমি আল্লাহর কাছে ক্ষমা প্রার্থনা করছি",
                    targetCount = 100
                ),
                TasbihSessionEntity(
                    phraseArabic = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ",
                    phraseTransliteration = "La ilaha illallah",
                    phraseMeaningEn = "There is no deity worthy of worship except Allah",
                    phraseMeaningBn = "আল্লাহ ছাড়া কোনো সত্য উপাস্য নেই",
                    targetCount = 100
                )
            )
            defaults.forEach { dao.upsertTasbihSession(it) }
        }
    }

    fun incrementTasbih(session: TasbihSessionEntity) {
        viewModelScope.launch {
            dao.upsertTasbihSession(
                session.copy(
                    currentCount = session.currentCount + 1,
                    totalLifetimeCount = session.totalLifetimeCount + 1,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun resetTasbihCurrentCount(session: TasbihSessionEntity) {
        viewModelScope.launch {
            dao.upsertTasbihSession(
                session.copy(
                    currentCount = 0,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            showStatusMessage("Reset counter for ${session.phraseTransliteration}.")
        }
    }

    fun updateTasbihTargetAndNote(session: TasbihSessionEntity, target: Int, note: String) {
        viewModelScope.launch {
            dao.upsertTasbihSession(
                session.copy(
                    targetCount = target.coerceAtLeast(1),
                    personalNote = note,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun addCustomTasbihPhrase(arabic: String, transliteration: String, meaningEn: String, target: Int) {
        viewModelScope.launch {
            dao.upsertTasbihSession(
                TasbihSessionEntity(
                    phraseArabic = arabic.ifBlank { "ذِكْرُ ٱللَّٰهِ" },
                    phraseTransliteration = transliteration,
                    phraseMeaningEn = meaningEn,
                    phraseMeaningBn = meaningEn,
                    targetCount = target.coerceAtLeast(1)
                )
            )
            showStatusMessage("Added custom Dhikr: $transliteration")
        }
    }

    // --- Dua & Hadith Favorites + Optional Sunnah.com Live Sync ---
    fun toggleFavoriteItem(itemKey: String, itemType: String, title: String) {
        viewModelScope.launch {
            val existing = favoriteItems.value.find { it.itemKey == itemKey }
            if (existing != null) {
                dao.deleteFavoriteItem(itemKey)
            } else {
                val todayIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
                dao.upsertFavoriteItem(
                    FavoriteItemEntity(
                        itemKey = itemKey,
                        itemType = itemType,
                        title = title,
                        isCompletedToday = true,
                        lastCompletedDateIso = todayIso
                    )
                )
            }
        }
    }

    fun fetchOptionalSunnahApiHadiths(collectionSlug: String = "bukhari") {
        val configuredKey = BuildConfig.SUNNAH_API_KEY
        if (configuredKey.isBlank() || configuredKey == "OPTIONAL_SUNNAH_API_KEY") {
            showStatusMessage("Using verified offline Hadith collection. Add SUNNAH_API_KEY in AI Studio Secrets for live Sunnah.com API queries.")
            return
        }
        viewModelScope.launch {
            val result = NetworkModule.withRetry {
                NetworkModule.sunnahApi.getCollectionHadiths(configuredKey, collectionSlug, limit = 10, page = 1)
            }
            val list = result.getOrNull()?.data
            if (!list.isNullOrEmpty()) {
                _remoteHadiths.value = list.mapIndexed { idx, item ->
                    val arBody = item.hadith.find { it.lang == "ar" }?.body?.replace(Regex("<[^>]*>"), "") ?: ""
                    val enBody = item.hadith.find { it.lang == "en" }?.body?.replace(Regex("<[^>]*>"), "") ?: ""
                    val bnBody = item.hadith.find { it.lang == "bn" }?.body?.replace(Regex("<[^>]*>"), "") ?: enBody
                    VerifiedHadithItem(
                        id = "sunnah_api_${item.collection}_${item.hadithNumber}_$idx",
                        collectionNameEn = "Sunnah.com API (${item.collection})",
                        collectionNameBn = "সুন্নাহ.কম এপিআই (${item.collection})",
                        bookNumber = 1,
                        bookNameEn = "Live Sunnah.com Collection",
                        hadithNumber = item.hadithNumber,
                        arabicText = arBody,
                        bengaliTranslation = bnBody,
                        englishTranslation = enBody,
                        narratorEn = "Sunnah.com Verified Feed",
                        grading = "Verified via Sunnah.com API",
                        sourceReference = "${item.collection} #${item.hadithNumber}"
                    )
                }
                showStatusMessage("Loaded ${_remoteHadiths.value.size} Hadiths from Sunnah.com API.")
            } else {
                showStatusMessage("Sunnah.com API unreachable or key invalid. Displaying verified offline Hadith library.")
            }
        }
    }

    // --- Zakat Market Prices ---
    fun updateZakatMarketPrices(goldPerGram: Double, silverPerGram: Double, currencySymbol: String) {
        viewModelScope.launch {
            prefsRepo.updateZakatMarketPrices(goldPerGram, silverPerGram, currencySymbol)
            showStatusMessage("Updated Zakat market rates: Gold $currencySymbol$goldPerGram/g, Silver $currencySymbol$silverPerGram/g.")
        }
    }

    // --- Backup Export & Import ---
    suspend fun exportUserDataJson(): String = withContext(Dispatchers.IO) {
        UserDataBackupManager.exportToJsonString(dao)
    }

    fun importUserDataJson(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = UserDataBackupManager.importFromJsonString(dao, jsonString)
                showStatusMessage("Successfully imported $count user records from backup.")
            } catch (e: Exception) {
                showStatusMessage("Failed to import backup JSON: ${e.localizedMessage ?: "Invalid format"}")
            }
        }
    }
}
