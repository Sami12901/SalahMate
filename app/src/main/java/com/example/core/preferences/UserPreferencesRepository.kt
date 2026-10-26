package com.example.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.salahMateDataStore: DataStore<Preferences> by preferencesDataStore(name = "salahmate_preferences")

enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class IslamicColorTheme(
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String
) {
    MADINAH_EMERALD(
        titleEn = "Sacred Madinah Emerald",
        titleBn = "সবুজ মদিনা (Madinah Emerald)",
        subtitleEn = "Green Dome emerald & illuminated brass gold",
        subtitleBn = "মসজিদে নববীর সবুজ গম্বুজ ও সোনালি আভা"
    ),
    KAABA_MIDNIGHT_GOLD(
        titleEn = "Kaaba Midnight Gold",
        titleBn = "কাবা সোনালি কৃষ্ণ (Kaaba Gold)",
        subtitleEn = "Kiswah obsidian silk & burnished gold",
        subtitleBn = "পবিত্র কাবার গিলাফ ও উজ্জ্বল সোনালি কারুকাজ"
    ),
    AL_AQSA_SAPPHIRE(
        titleEn = "Al-Aqsa Sapphire",
        titleBn = "আল-আকসা নীলাভ (Al-Aqsa Sapphire)",
        subtitleEn = "Celestial cobalt blue & brushed silver",
        subtitleBn = "ডোম অব দ্য রকের নীল ও রুপালি টাইলস"
    ),
    DESERT_ROSE_PARCHMENT(
        titleEn = "Desert Rose Parchment",
        titleBn = "মরু গোলাপ ও পাণ্ডুলিপি (Desert Rose)",
        subtitleEn = "Andalusian terracotta & warm sand manuscript",
        subtitleBn = "আন্দালুসিয়ান টেরাকোটা ও প্রাচীন পাণ্ডুলিপি"
    )
}

enum class PrayerProviderMode { LOCAL_ADHAN, ONLINE_ALADHAN }
enum class AsrJuristicSchool(val alAdhanSchoolCode: Int, val labelEn: String, val labelBn: String) {
    STANDARD(0, "Standard (Shafi'i, Maliki, Hanbali)", "স্ট্যান্ডার্ড (শাফেয়ী, মালেকী, হাম্বলী)"),
    HANAFI(1, "Hanafi (Later Asr Shadow 2x)", "হানাফী (দ্বিগুণ ছায়া)")
}

enum class CalculationMethodOption(
    val alAdhanMethodId: Int,
    val titleEn: String,
    val titleBn: String,
    val fajrAngle: Double,
    val ishaAngle: Double
) {
    KARACHI(1, "Univ. of Islamic Sciences, Karachi (Method 1)", "ইসলামিক সায়েন্সেস বিশ্ববিদ্যালয়, করাচি (পদ্ধতি ১)", 18.0, 18.0),
    MWL(3, "Muslim World League (MWL)", "মুসলিম ওয়ার্ল্ড লীগ (MWL)", 18.0, 17.0),
    ISNA(2, "Islamic Society of North America (ISNA)", "ইসলামিক সোসাইটি অব নর্থ আমেরিকা (ISNA)", 15.0, 15.0),
    UMM_AL_QURA(4, "Umm al-Qura University, Makkah", "উম্মুল কুরা বিশ্ববিদ্যালয়, মক্কা", 18.5, 0.0),
    EGYPTIAN(5, "Egyptian General Authority of Survey", "মিশরীয় জেনারেল অথরিটি অব সার্ভে", 19.5, 17.5),
    DUBAI(16, "Dubai / Gulf Region", "দুবাই / উপসাগরীয় অঞ্চল", 18.2, 18.2),
    SINGAPORE(11, "MUIS Singapore / Southeast Asia", "সিঙ্গাপুর / দক্ষিণ-পূর্ব এশিয়া", 20.0, 18.0)
}

enum class NotificationSoundMode(val labelEn: String, val labelBn: String) {
    DEFAULT_TONE("System Notification Tone", "ডিফল্ট নোটিফিকেশন টোন"),
    ADHAN_CHIME("Adhan Melodic Call (Synthesized)", "আযান ধ্বনি (সিন্থেসাইজড)"),
    SILENT("Silent Visual Reminder", "নীরব রিমাইন্ডার")
}

data class PresetCity(
    val nameEn: String,
    val nameBn: String,
    val countryEn: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String,
    val recommendedMethod: CalculationMethodOption = CalculationMethodOption.KARACHI,
    val recommendedSchool: AsrJuristicSchool = AsrJuristicSchool.HANAFI
)

val PRESET_CITIES = listOf(
    PresetCity("Dhaka", "ঢাকা", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Chattogram", "চট্টগ্রাম", "Bangladesh", 22.3569, 91.7832, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Sylhet", "সিলেট", "Bangladesh", 24.8949, 91.8687, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Rajshahi", "রাজশাহী", "Bangladesh", 24.3745, 88.6042, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Khulna", "খুলনা", "Bangladesh", 22.8456, 89.5403, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Barishal", "বরিশাল", "Bangladesh", 22.7010, 90.3535, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Rangpur", "রংপুর", "Bangladesh", 25.7439, 89.2752, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Mymensingh", "ময়মনসিংহ", "Bangladesh", 24.7471, 90.4203, "Asia/Dhaka", CalculationMethodOption.KARACHI, AsrJuristicSchool.HANAFI),
    PresetCity("Makkah", "মক্কা", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh", CalculationMethodOption.UMM_AL_QURA, AsrJuristicSchool.STANDARD),
    PresetCity("Madinah", "মদিনা", "Saudi Arabia", 24.4672, 39.6112, "Asia/Riyadh", CalculationMethodOption.UMM_AL_QURA, AsrJuristicSchool.STANDARD),
    PresetCity("London", "লন্ডন", "United Kingdom", 51.5074, -0.1278, "Europe/London", CalculationMethodOption.MWL, AsrJuristicSchool.HANAFI),
    PresetCity("New York", "নিউ ইয়র্ক", "United States", 40.7128, -74.0060, "America/New_York", CalculationMethodOption.ISNA, AsrJuristicSchool.STANDARD),
    PresetCity("Kuala Lumpur", "কুয়ালালামপুর", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur", CalculationMethodOption.SINGAPORE, AsrJuristicSchool.STANDARD)
)

data class UserPreferencesState(
    val onboardingCompleted: Boolean = false,
    val languageCode: String = "en", // "en" default, instant switch to "bn"
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val colorTheme: IslamicColorTheme = IslamicColorTheme.MADINAH_EMERALD,
    val cityName: String = "Dhaka",
    val countryName: String = "Bangladesh",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val timeZoneId: String = "Asia/Dhaka",
    val isManualCoordinates: Boolean = false,
    val calculationMethod: CalculationMethodOption = CalculationMethodOption.KARACHI,
    val asrSchool: AsrJuristicSchool = AsrJuristicSchool.HANAFI,
    val providerMode: PrayerProviderMode = PrayerProviderMode.LOCAL_ADHAN,
    val fajrOffsetMin: Int = 0,
    val sunriseOffsetMin: Int = 0,
    val dhuhrOffsetMin: Int = 0,
    val asrOffsetMin: Int = 0,
    val maghribOffsetMin: Int = 0,
    val ishaOffsetMin: Int = 0,
    val imsakOffsetMin: Int = -10, // Default Imsak precautionary margin before Fajr
    // Mosque Jama'ah (Congregation) timetable offsets kept separate from calculated solar Waqt
    val showMosqueJamaah: Boolean = true,
    val fajrJamaahOffsetMin: Int = 20,
    val dhuhrJamaahOffsetMin: Int = 15,
    val asrJamaahOffsetMin: Int = 15,
    val maghribJamaahOffsetMin: Int = 5,
    val ishaJamaahOffsetMin: Int = 15,
    val notifyFajr: Boolean = true,
    val notifyDhuhr: Boolean = true,
    val notifyAsr: Boolean = true,
    val notifyMaghrib: Boolean = true,
    val notifyIsha: Boolean = true,
    val advanceReminderMinutes: Int = 0,
    val notificationSoundMode: NotificationSoundMode = NotificationSoundMode.ADHAN_CHIME,
    // Independent optional worship reminders
    val morningAdhkarReminderEnabled: Boolean = false,
    val eveningAdhkarReminderEnabled: Boolean = false,
    val jumuahKahfReminderEnabled: Boolean = true,
    val hijriAdjustmentDays: Int = 0,
    val quranArabicFontSizeSp: Float = 28f,
    val quranTranslationFontSizeSp: Float = 16f,
    val showBengaliTranslation: Boolean = true,
    val showEnglishTranslation: Boolean = true,
    val lastReadSurahNumber: Int = 1,
    val lastReadSurahName: String = "Al-Fatihah",
    val lastReadAyahNumber: Int = 1,
    val dailyQuranGoalAyahs: Int = 20,
    val dailyHifzRevisionGoalAyahs: Int = 10,
    val hifzReviewIntervalDays: Int = 7, // Spaced-revision interval: 1, 3, 7, 14, or 30 days
    val goldPricePerGram: Double = 11500.0, // Editable BDT/USD market value per gram
    val silverPricePerGram: Double = 145.0,
    val zakatCurrencySymbol: String = "৳"
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val THEME_PREF = stringPreferencesKey("theme_pref")
        val COLOR_THEME = stringPreferencesKey("color_theme")
        val CITY_NAME = stringPreferencesKey("city_name")
        val COUNTRY_NAME = stringPreferencesKey("country_name")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val TIME_ZONE_ID = stringPreferencesKey("time_zone_id")
        val IS_MANUAL_COORDS = booleanPreferencesKey("is_manual_coords")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val ASR_SCHOOL = stringPreferencesKey("asr_school")
        val PROVIDER_MODE = stringPreferencesKey("provider_mode")
        val FAJR_OFFSET = intPreferencesKey("fajr_offset")
        val SUNRISE_OFFSET = intPreferencesKey("sunrise_offset")
        val DHUHR_OFFSET = intPreferencesKey("dhuhr_offset")
        val ASR_OFFSET = intPreferencesKey("asr_offset")
        val MAGHRIB_OFFSET = intPreferencesKey("maghrib_offset")
        val ISHA_OFFSET = intPreferencesKey("isha_offset")
        val IMSAK_OFFSET = intPreferencesKey("imsak_offset")
        val SHOW_MOSQUE_JAMAAH = booleanPreferencesKey("show_mosque_jamaah")
        val FAJR_JAMAAH_OFFSET = intPreferencesKey("fajr_jamaah_offset")
        val DHUHR_JAMAAH_OFFSET = intPreferencesKey("dhuhr_jamaah_offset")
        val ASR_JAMAAH_OFFSET = intPreferencesKey("asr_jamaah_offset")
        val MAGHRIB_JAMAAH_OFFSET = intPreferencesKey("maghrib_jamaah_offset")
        val ISHA_JAMAAH_OFFSET = intPreferencesKey("isha_jamaah_offset")
        val NOTIFY_FAJR = booleanPreferencesKey("notify_fajr")
        val NOTIFY_DHUHR = booleanPreferencesKey("notify_dhuhr")
        val NOTIFY_ASR = booleanPreferencesKey("notify_asr")
        val NOTIFY_MAGHRIB = booleanPreferencesKey("notify_maghrib")
        val NOTIFY_ISHA = booleanPreferencesKey("notify_isha")
        val ADVANCE_REMINDER_MIN = intPreferencesKey("advance_reminder_min")
        val SOUND_MODE = stringPreferencesKey("sound_mode")
        val MORNING_ADHKAR_REMINDER = booleanPreferencesKey("morning_adhkar_reminder")
        val EVENING_ADHKAR_REMINDER = booleanPreferencesKey("evening_adhkar_reminder")
        val JUMUAH_KAHF_REMINDER = booleanPreferencesKey("jumuah_kahf_reminder")
        val HIJRI_ADJUST = intPreferencesKey("hijri_adjust")
        val QURAN_ARABIC_SIZE = floatPreferencesKey("quran_arabic_size")
        val QURAN_TRANS_SIZE = floatPreferencesKey("quran_trans_size")
        val SHOW_BN_TRANS = booleanPreferencesKey("show_bn_trans")
        val SHOW_EN_TRANS = booleanPreferencesKey("show_en_trans")
        val LAST_SURAH_NUM = intPreferencesKey("last_surah_num")
        val LAST_SURAH_NAME = stringPreferencesKey("last_surah_name")
        val LAST_AYAH_NUM = intPreferencesKey("last_ayah_num")
        val DAILY_QURAN_GOAL = intPreferencesKey("daily_quran_goal")
        val DAILY_HIFZ_GOAL = intPreferencesKey("daily_hifz_goal")
        val HIFZ_REVIEW_INTERVAL_DAYS = intPreferencesKey("hifz_review_interval_days")
        val GOLD_PRICE_GRAM = doublePreferencesKey("gold_price_gram")
        val SILVER_PRICE_GRAM = doublePreferencesKey("silver_price_gram")
        val ZAKAT_CURRENCY = stringPreferencesKey("zakat_currency")
    }

    val preferencesFlow: Flow<UserPreferencesState> = dataStore.data.map { prefs ->
        UserPreferencesState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            themePreference = runCatching { ThemePreference.valueOf(prefs[Keys.THEME_PREF] ?: "SYSTEM") }.getOrDefault(ThemePreference.SYSTEM),
            colorTheme = runCatching { IslamicColorTheme.valueOf(prefs[Keys.COLOR_THEME] ?: "MADINAH_EMERALD") }.getOrDefault(IslamicColorTheme.MADINAH_EMERALD),
            cityName = prefs[Keys.CITY_NAME] ?: "Dhaka",
            countryName = prefs[Keys.COUNTRY_NAME] ?: "Bangladesh",
            latitude = prefs[Keys.LATITUDE] ?: 23.8103,
            longitude = prefs[Keys.LONGITUDE] ?: 90.4125,
            timeZoneId = prefs[Keys.TIME_ZONE_ID] ?: "Asia/Dhaka",
            isManualCoordinates = prefs[Keys.IS_MANUAL_COORDS] ?: false,
            calculationMethod = runCatching { CalculationMethodOption.valueOf(prefs[Keys.CALC_METHOD] ?: "KARACHI") }.getOrDefault(CalculationMethodOption.KARACHI),
            asrSchool = runCatching { AsrJuristicSchool.valueOf(prefs[Keys.ASR_SCHOOL] ?: "HANAFI") }.getOrDefault(AsrJuristicSchool.HANAFI),
            providerMode = runCatching { PrayerProviderMode.valueOf(prefs[Keys.PROVIDER_MODE] ?: "LOCAL_ADHAN") }.getOrDefault(PrayerProviderMode.LOCAL_ADHAN),
            fajrOffsetMin = prefs[Keys.FAJR_OFFSET] ?: 0,
            sunriseOffsetMin = prefs[Keys.SUNRISE_OFFSET] ?: 0,
            dhuhrOffsetMin = prefs[Keys.DHUHR_OFFSET] ?: 0,
            asrOffsetMin = prefs[Keys.ASR_OFFSET] ?: 0,
            maghribOffsetMin = prefs[Keys.MAGHRIB_OFFSET] ?: 0,
            ishaOffsetMin = prefs[Keys.ISHA_OFFSET] ?: 0,
            imsakOffsetMin = prefs[Keys.IMSAK_OFFSET] ?: -10,
            showMosqueJamaah = prefs[Keys.SHOW_MOSQUE_JAMAAH] ?: true,
            fajrJamaahOffsetMin = prefs[Keys.FAJR_JAMAAH_OFFSET] ?: 20,
            dhuhrJamaahOffsetMin = prefs[Keys.DHUHR_JAMAAH_OFFSET] ?: 15,
            asrJamaahOffsetMin = prefs[Keys.ASR_JAMAAH_OFFSET] ?: 15,
            maghribJamaahOffsetMin = prefs[Keys.MAGHRIB_JAMAAH_OFFSET] ?: 5,
            ishaJamaahOffsetMin = prefs[Keys.ISHA_JAMAAH_OFFSET] ?: 15,
            notifyFajr = prefs[Keys.NOTIFY_FAJR] ?: true,
            notifyDhuhr = prefs[Keys.NOTIFY_DHUHR] ?: true,
            notifyAsr = prefs[Keys.NOTIFY_ASR] ?: true,
            notifyMaghrib = prefs[Keys.NOTIFY_MAGHRIB] ?: true,
            notifyIsha = prefs[Keys.NOTIFY_ISHA] ?: true,
            advanceReminderMinutes = prefs[Keys.ADVANCE_REMINDER_MIN] ?: 0,
            notificationSoundMode = runCatching { NotificationSoundMode.valueOf(prefs[Keys.SOUND_MODE] ?: "ADHAN_CHIME") }.getOrDefault(NotificationSoundMode.ADHAN_CHIME),
            morningAdhkarReminderEnabled = prefs[Keys.MORNING_ADHKAR_REMINDER] ?: false,
            eveningAdhkarReminderEnabled = prefs[Keys.EVENING_ADHKAR_REMINDER] ?: false,
            jumuahKahfReminderEnabled = prefs[Keys.JUMUAH_KAHF_REMINDER] ?: true,
            hijriAdjustmentDays = prefs[Keys.HIJRI_ADJUST] ?: 0,
            quranArabicFontSizeSp = prefs[Keys.QURAN_ARABIC_SIZE] ?: 28f,
            quranTranslationFontSizeSp = prefs[Keys.QURAN_TRANS_SIZE] ?: 16f,
            showBengaliTranslation = prefs[Keys.SHOW_BN_TRANS] ?: true,
            showEnglishTranslation = prefs[Keys.SHOW_EN_TRANS] ?: true,
            lastReadSurahNumber = prefs[Keys.LAST_SURAH_NUM] ?: 1,
            lastReadSurahName = prefs[Keys.LAST_SURAH_NAME] ?: "Al-Fatihah",
            lastReadAyahNumber = prefs[Keys.LAST_AYAH_NUM] ?: 1,
            dailyQuranGoalAyahs = prefs[Keys.DAILY_QURAN_GOAL] ?: 20,
            dailyHifzRevisionGoalAyahs = prefs[Keys.DAILY_HIFZ_GOAL] ?: 10,
            hifzReviewIntervalDays = prefs[Keys.HIFZ_REVIEW_INTERVAL_DAYS] ?: 7,
            goldPricePerGram = prefs[Keys.GOLD_PRICE_GRAM] ?: 11500.0,
            silverPricePerGram = prefs[Keys.SILVER_PRICE_GRAM] ?: 145.0,
            zakatCurrencySymbol = prefs[Keys.ZAKAT_CURRENCY] ?: "৳"
        )
    }

    suspend fun completeOnboarding(
        city: String,
        country: String,
        lat: Double,
        lon: Double,
        tzId: String,
        method: CalculationMethodOption,
        school: AsrJuristicSchool
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
            prefs[Keys.CITY_NAME] = city
            prefs[Keys.COUNTRY_NAME] = country
            prefs[Keys.LATITUDE] = lat.coerceIn(-90.0, 90.0)
            prefs[Keys.LONGITUDE] = lon.coerceIn(-180.0, 180.0)
            prefs[Keys.TIME_ZONE_ID] = tzId
            prefs[Keys.CALC_METHOD] = method.name
            prefs[Keys.ASR_SCHOOL] = school.name
        }
    }

    suspend fun setLanguage(languageCode: String) {
        dataStore.edit { it[Keys.LANGUAGE_CODE] = languageCode }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        dataStore.edit { it[Keys.THEME_PREF] = theme.name }
    }

    suspend fun setColorTheme(colorTheme: IslamicColorTheme) {
        dataStore.edit { it[Keys.COLOR_THEME] = colorTheme.name }
    }

    suspend fun updateLocation(
        city: String,
        country: String,
        lat: Double,
        lon: Double,
        tzId: String,
        isManual: Boolean = false
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.CITY_NAME] = city
            prefs[Keys.COUNTRY_NAME] = country
            prefs[Keys.LATITUDE] = lat.coerceIn(-90.0, 90.0)
            prefs[Keys.LONGITUDE] = lon.coerceIn(-180.0, 180.0)
            prefs[Keys.TIME_ZONE_ID] = tzId
            prefs[Keys.IS_MANUAL_COORDS] = isManual
        }
    }

    suspend fun updateCalculationSettings(
        method: CalculationMethodOption,
        school: AsrJuristicSchool,
        providerMode: PrayerProviderMode
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.CALC_METHOD] = method.name
            prefs[Keys.ASR_SCHOOL] = school.name
            prefs[Keys.PROVIDER_MODE] = providerMode.name
        }
    }

    suspend fun updatePrayerOffsets(
        fajr: Int,
        sunrise: Int,
        dhuhr: Int,
        asr: Int,
        maghrib: Int,
        isha: Int,
        imsak: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.FAJR_OFFSET] = fajr.coerceIn(-60, 60)
            prefs[Keys.SUNRISE_OFFSET] = sunrise.coerceIn(-60, 60)
            prefs[Keys.DHUHR_OFFSET] = dhuhr.coerceIn(-60, 60)
            prefs[Keys.ASR_OFFSET] = asr.coerceIn(-60, 60)
            prefs[Keys.MAGHRIB_OFFSET] = maghrib.coerceIn(-60, 60)
            prefs[Keys.ISHA_OFFSET] = isha.coerceIn(-60, 60)
            prefs[Keys.IMSAK_OFFSET] = imsak.coerceIn(-60, 0)
        }
    }

    suspend fun updateMosqueJamaahSettings(
        showJamaah: Boolean,
        fajrJamaahMin: Int,
        dhuhrJamaahMin: Int,
        asrJamaahMin: Int,
        maghribJamaahMin: Int,
        ishaJamaahMin: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.SHOW_MOSQUE_JAMAAH] = showJamaah
            prefs[Keys.FAJR_JAMAAH_OFFSET] = fajrJamaahMin.coerceIn(0, 90)
            prefs[Keys.DHUHR_JAMAAH_OFFSET] = dhuhrJamaahMin.coerceIn(0, 90)
            prefs[Keys.ASR_JAMAAH_OFFSET] = asrJamaahMin.coerceIn(0, 90)
            prefs[Keys.MAGHRIB_JAMAAH_OFFSET] = maghribJamaahMin.coerceIn(0, 60)
            prefs[Keys.ISHA_JAMAAH_OFFSET] = ishaJamaahMin.coerceIn(0, 90)
        }
    }

    suspend fun updateNotificationSettings(
        fajr: Boolean,
        dhuhr: Boolean,
        asr: Boolean,
        maghrib: Boolean,
        isha: Boolean,
        advanceMinutes: Int,
        soundMode: NotificationSoundMode
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.NOTIFY_FAJR] = fajr
            prefs[Keys.NOTIFY_DHUHR] = dhuhr
            prefs[Keys.NOTIFY_ASR] = asr
            prefs[Keys.NOTIFY_MAGHRIB] = maghrib
            prefs[Keys.NOTIFY_ISHA] = isha
            prefs[Keys.ADVANCE_REMINDER_MIN] = advanceMinutes.coerceIn(0, 60)
            prefs[Keys.SOUND_MODE] = soundMode.name
        }
    }

    suspend fun updateWorshipReminders(
        morningAdhkar: Boolean,
        eveningAdhkar: Boolean,
        jumuahKahf: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.MORNING_ADHKAR_REMINDER] = morningAdhkar
            prefs[Keys.EVENING_ADHKAR_REMINDER] = eveningAdhkar
            prefs[Keys.JUMUAH_KAHF_REMINDER] = jumuahKahf
        }
    }

    suspend fun setHijriAdjustment(days: Int) {
        dataStore.edit { it[Keys.HIJRI_ADJUST] = days.coerceIn(-3, 3) }
    }

    suspend fun updateQuranSettings(
        arabicFontSize: Float,
        translationFontSize: Float,
        showBn: Boolean,
        showEn: Boolean
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.QURAN_ARABIC_SIZE] = arabicFontSize.coerceIn(20f, 44f)
            prefs[Keys.QURAN_TRANS_SIZE] = translationFontSize.coerceIn(13f, 26f)
            prefs[Keys.SHOW_BN_TRANS] = showBn
            prefs[Keys.SHOW_EN_TRANS] = showEn
        }
    }

    suspend fun updateDailyGoalsAndHifzInterval(
        dailyQuranAyahs: Int,
        dailyHifzAyahs: Int,
        hifzIntervalDays: Int
    ) {
        dataStore.edit { prefs ->
            prefs[Keys.DAILY_QURAN_GOAL] = dailyQuranAyahs.coerceIn(1, 300)
            prefs[Keys.DAILY_HIFZ_GOAL] = dailyHifzAyahs.coerceIn(1, 200)
            prefs[Keys.HIFZ_REVIEW_INTERVAL_DAYS] = hifzIntervalDays.coerceIn(1, 90)
        }
    }

    suspend fun updateLastReadQuran(surahNumber: Int, surahName: String, ayahNumber: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_SURAH_NUM] = surahNumber
            prefs[Keys.LAST_SURAH_NAME] = surahName
            prefs[Keys.LAST_AYAH_NUM] = ayahNumber
        }
    }

    suspend fun updateZakatMarketPrices(goldPerGram: Double, silverPerGram: Double, currency: String) {
        dataStore.edit { prefs ->
            prefs[Keys.GOLD_PRICE_GRAM] = goldPerGram.coerceAtLeast(1.0)
            prefs[Keys.SILVER_PRICE_GRAM] = silverPerGram.coerceAtLeast(0.1)
            prefs[Keys.ZAKAT_CURRENCY] = currency.ifBlank { "৳" }
        }
    }

    suspend fun exportPreferencesToJson(): JSONObject {
        val state = preferencesFlow.first()
        return JSONObject().apply {
            put("languageCode", state.languageCode)
            put("themePreference", state.themePreference.name)
            put("colorTheme", state.colorTheme.name)
            put("cityName", state.cityName)
            put("countryName", state.countryName)
            put("latitude", state.latitude)
            put("longitude", state.longitude)
            put("timeZoneId", state.timeZoneId)
            put("isManualCoordinates", state.isManualCoordinates)
            put("calculationMethod", state.calculationMethod.name)
            put("asrSchool", state.asrSchool.name)
            put("providerMode", state.providerMode.name)
            put("fajrOffsetMin", state.fajrOffsetMin)
            put("sunriseOffsetMin", state.sunriseOffsetMin)
            put("dhuhrOffsetMin", state.dhuhrOffsetMin)
            put("asrOffsetMin", state.asrOffsetMin)
            put("maghribOffsetMin", state.maghribOffsetMin)
            put("ishaOffsetMin", state.ishaOffsetMin)
            put("imsakOffsetMin", state.imsakOffsetMin)
            put("showMosqueJamaah", state.showMosqueJamaah)
            put("fajrJamaahOffsetMin", state.fajrJamaahOffsetMin)
            put("dhuhrJamaahOffsetMin", state.dhuhrJamaahOffsetMin)
            put("asrJamaahOffsetMin", state.asrJamaahOffsetMin)
            put("maghribJamaahOffsetMin", state.maghribJamaahOffsetMin)
            put("ishaJamaahOffsetMin", state.ishaJamaahOffsetMin)
            put("notifyFajr", state.notifyFajr)
            put("notifyDhuhr", state.notifyDhuhr)
            put("notifyAsr", state.notifyAsr)
            put("notifyMaghrib", state.notifyMaghrib)
            put("notifyIsha", state.notifyIsha)
            put("advanceReminderMinutes", state.advanceReminderMinutes)
            put("notificationSoundMode", state.notificationSoundMode.name)
            put("morningAdhkarReminderEnabled", state.morningAdhkarReminderEnabled)
            put("eveningAdhkarReminderEnabled", state.eveningAdhkarReminderEnabled)
            put("jumuahKahfReminderEnabled", state.jumuahKahfReminderEnabled)
            put("hijriAdjustmentDays", state.hijriAdjustmentDays)
            put("quranArabicFontSizeSp", state.quranArabicFontSizeSp.toDouble())
            put("quranTranslationFontSizeSp", state.quranTranslationFontSizeSp.toDouble())
            put("showBengaliTranslation", state.showBengaliTranslation)
            put("showEnglishTranslation", state.showEnglishTranslation)
            put("dailyQuranGoalAyahs", state.dailyQuranGoalAyahs)
            put("dailyHifzRevisionGoalAyahs", state.dailyHifzRevisionGoalAyahs)
            put("hifzReviewIntervalDays", state.hifzReviewIntervalDays)
            put("goldPricePerGram", state.goldPricePerGram)
            put("silverPricePerGram", state.silverPricePerGram)
            put("zakatCurrencySymbol", state.zakatCurrencySymbol)
        }
    }

    suspend fun importPreferencesFromJson(obj: JSONObject) {
        dataStore.edit { prefs ->
            obj.optString("languageCode").takeIf { it == "en" || it == "bn" }?.let { prefs[Keys.LANGUAGE_CODE] = it }
            runCatching { ThemePreference.valueOf(obj.optString("themePreference")) }.getOrNull()?.let { prefs[Keys.THEME_PREF] = it.name }
            runCatching { IslamicColorTheme.valueOf(obj.optString("colorTheme")) }.getOrNull()?.let { prefs[Keys.COLOR_THEME] = it.name }
            obj.optString("cityName").takeIf { it.isNotBlank() }?.let { prefs[Keys.CITY_NAME] = it }
            obj.optString("countryName").takeIf { it.isNotBlank() }?.let { prefs[Keys.COUNTRY_NAME] = it }
            if (obj.has("latitude")) prefs[Keys.LATITUDE] = obj.optDouble("latitude", 23.8103).coerceIn(-90.0, 90.0)
            if (obj.has("longitude")) prefs[Keys.LONGITUDE] = obj.optDouble("longitude", 90.4125).coerceIn(-180.0, 180.0)
            obj.optString("timeZoneId").takeIf { it.isNotBlank() }?.let { prefs[Keys.TIME_ZONE_ID] = it }
            if (obj.has("isManualCoordinates")) prefs[Keys.IS_MANUAL_COORDS] = obj.optBoolean("isManualCoordinates")
            runCatching { CalculationMethodOption.valueOf(obj.optString("calculationMethod")) }.getOrNull()?.let { prefs[Keys.CALC_METHOD] = it.name }
            runCatching { AsrJuristicSchool.valueOf(obj.optString("asrSchool")) }.getOrNull()?.let { prefs[Keys.ASR_SCHOOL] = it.name }
            runCatching { PrayerProviderMode.valueOf(obj.optString("providerMode")) }.getOrNull()?.let { prefs[Keys.PROVIDER_MODE] = it.name }
            if (obj.has("fajrOffsetMin")) prefs[Keys.FAJR_OFFSET] = obj.optInt("fajrOffsetMin", 0).coerceIn(-60, 60)
            if (obj.has("sunriseOffsetMin")) prefs[Keys.SUNRISE_OFFSET] = obj.optInt("sunriseOffsetMin", 0).coerceIn(-60, 60)
            if (obj.has("dhuhrOffsetMin")) prefs[Keys.DHUHR_OFFSET] = obj.optInt("dhuhrOffsetMin", 0).coerceIn(-60, 60)
            if (obj.has("asrOffsetMin")) prefs[Keys.ASR_OFFSET] = obj.optInt("asrOffsetMin", 0).coerceIn(-60, 60)
            if (obj.has("maghribOffsetMin")) prefs[Keys.MAGHRIB_OFFSET] = obj.optInt("maghribOffsetMin", 0).coerceIn(-60, 60)
            if (obj.has("ishaOffsetMin")) prefs[Keys.ISHA_OFFSET] = obj.optInt("ishaOffsetMin", 0).coerceIn(-60, 60)
            if (obj.has("imsakOffsetMin")) prefs[Keys.IMSAK_OFFSET] = obj.optInt("imsakOffsetMin", -10).coerceIn(-60, 0)
            if (obj.has("showMosqueJamaah")) prefs[Keys.SHOW_MOSQUE_JAMAAH] = obj.optBoolean("showMosqueJamaah", true)
            if (obj.has("fajrJamaahOffsetMin")) prefs[Keys.FAJR_JAMAAH_OFFSET] = obj.optInt("fajrJamaahOffsetMin", 20).coerceIn(0, 90)
            if (obj.has("dhuhrJamaahOffsetMin")) prefs[Keys.DHUHR_JAMAAH_OFFSET] = obj.optInt("dhuhrJamaahOffsetMin", 15).coerceIn(0, 90)
            if (obj.has("asrJamaahOffsetMin")) prefs[Keys.ASR_JAMAAH_OFFSET] = obj.optInt("asrJamaahOffsetMin", 15).coerceIn(0, 90)
            if (obj.has("maghribJamaahOffsetMin")) prefs[Keys.MAGHRIB_JAMAAH_OFFSET] = obj.optInt("maghribJamaahOffsetMin", 5).coerceIn(0, 60)
            if (obj.has("ishaJamaahOffsetMin")) prefs[Keys.ISHA_JAMAAH_OFFSET] = obj.optInt("ishaJamaahOffsetMin", 15).coerceIn(0, 90)
            if (obj.has("notifyFajr")) prefs[Keys.NOTIFY_FAJR] = obj.optBoolean("notifyFajr", true)
            if (obj.has("notifyDhuhr")) prefs[Keys.NOTIFY_DHUHR] = obj.optBoolean("notifyDhuhr", true)
            if (obj.has("notifyAsr")) prefs[Keys.NOTIFY_ASR] = obj.optBoolean("notifyAsr", true)
            if (obj.has("notifyMaghrib")) prefs[Keys.NOTIFY_MAGHRIB] = obj.optBoolean("notifyMaghrib", true)
            if (obj.has("notifyIsha")) prefs[Keys.NOTIFY_ISHA] = obj.optBoolean("notifyIsha", true)
            if (obj.has("advanceReminderMinutes")) prefs[Keys.ADVANCE_REMINDER_MIN] = obj.optInt("advanceReminderMinutes", 0).coerceIn(0, 60)
            runCatching { NotificationSoundMode.valueOf(obj.optString("notificationSoundMode")) }.getOrNull()?.let { prefs[Keys.SOUND_MODE] = it.name }
            if (obj.has("morningAdhkarReminderEnabled")) prefs[Keys.MORNING_ADHKAR_REMINDER] = obj.optBoolean("morningAdhkarReminderEnabled", false)
            if (obj.has("eveningAdhkarReminderEnabled")) prefs[Keys.EVENING_ADHKAR_REMINDER] = obj.optBoolean("eveningAdhkarReminderEnabled", false)
            if (obj.has("jumuahKahfReminderEnabled")) prefs[Keys.JUMUAH_KAHF_REMINDER] = obj.optBoolean("jumuahKahfReminderEnabled", true)
            if (obj.has("hijriAdjustmentDays")) prefs[Keys.HIJRI_ADJUST] = obj.optInt("hijriAdjustmentDays", 0).coerceIn(-3, 3)
            if (obj.has("quranArabicFontSizeSp")) prefs[Keys.QURAN_ARABIC_SIZE] = obj.optDouble("quranArabicFontSizeSp", 28.0).toFloat().coerceIn(20f, 44f)
            if (obj.has("quranTranslationFontSizeSp")) prefs[Keys.QURAN_TRANS_SIZE] = obj.optDouble("quranTranslationFontSizeSp", 16.0).toFloat().coerceIn(13f, 26f)
            if (obj.has("showBengaliTranslation")) prefs[Keys.SHOW_BN_TRANS] = obj.optBoolean("showBengaliTranslation", true)
            if (obj.has("showEnglishTranslation")) prefs[Keys.SHOW_EN_TRANS] = obj.optBoolean("showEnglishTranslation", true)
            if (obj.has("dailyQuranGoalAyahs")) prefs[Keys.DAILY_QURAN_GOAL] = obj.optInt("dailyQuranGoalAyahs", 20).coerceIn(1, 300)
            if (obj.has("dailyHifzRevisionGoalAyahs")) prefs[Keys.DAILY_HIFZ_GOAL] = obj.optInt("dailyHifzRevisionGoalAyahs", 10).coerceIn(1, 200)
            if (obj.has("hifzReviewIntervalDays")) prefs[Keys.HIFZ_REVIEW_INTERVAL_DAYS] = obj.optInt("hifzReviewIntervalDays", 7).coerceIn(1, 90)
            if (obj.has("goldPricePerGram")) prefs[Keys.GOLD_PRICE_GRAM] = obj.optDouble("goldPricePerGram", 11500.0).coerceAtLeast(1.0)
            if (obj.has("silverPricePerGram")) prefs[Keys.SILVER_PRICE_GRAM] = obj.optDouble("silverPricePerGram", 145.0).coerceAtLeast(0.1)
            obj.optString("zakatCurrencySymbol").takeIf { it.isNotBlank() }?.let { prefs[Keys.ZAKAT_CURRENCY] = it }
        }
    }
}
