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
import kotlinx.coroutines.flow.map

val Context.salahMateDataStore: DataStore<Preferences> by preferencesDataStore(name = "salahmate_preferences")

enum class ThemePreference { SYSTEM, LIGHT, DARK }
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
    val notifyFajr: Boolean = true,
    val notifyDhuhr: Boolean = true,
    val notifyAsr: Boolean = true,
    val notifyMaghrib: Boolean = true,
    val notifyIsha: Boolean = true,
    val advanceReminderMinutes: Int = 0,
    val notificationSoundMode: NotificationSoundMode = NotificationSoundMode.ADHAN_CHIME,
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
    val goldPricePerGram: Double = 11500.0, // Editable BDT/USD market value per gram
    val silverPricePerGram: Double = 145.0,
    val zakatCurrencySymbol: String = "৳"
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val THEME_PREF = stringPreferencesKey("theme_pref")
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
        val NOTIFY_FAJR = booleanPreferencesKey("notify_fajr")
        val NOTIFY_DHUHR = booleanPreferencesKey("notify_dhuhr")
        val NOTIFY_ASR = booleanPreferencesKey("notify_asr")
        val NOTIFY_MAGHRIB = booleanPreferencesKey("notify_maghrib")
        val NOTIFY_ISHA = booleanPreferencesKey("notify_isha")
        val ADVANCE_REMINDER_MIN = intPreferencesKey("advance_reminder_min")
        val SOUND_MODE = stringPreferencesKey("sound_mode")
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
        val GOLD_PRICE_GRAM = doublePreferencesKey("gold_price_gram")
        val SILVER_PRICE_GRAM = doublePreferencesKey("silver_price_gram")
        val ZAKAT_CURRENCY = stringPreferencesKey("zakat_currency")
    }

    val preferencesFlow: Flow<UserPreferencesState> = dataStore.data.map { prefs ->
        UserPreferencesState(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            themePreference = runCatching { ThemePreference.valueOf(prefs[Keys.THEME_PREF] ?: "SYSTEM") }.getOrDefault(ThemePreference.SYSTEM),
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
            notifyFajr = prefs[Keys.NOTIFY_FAJR] ?: true,
            notifyDhuhr = prefs[Keys.NOTIFY_DHUHR] ?: true,
            notifyAsr = prefs[Keys.NOTIFY_ASR] ?: true,
            notifyMaghrib = prefs[Keys.NOTIFY_MAGHRIB] ?: true,
            notifyIsha = prefs[Keys.NOTIFY_ISHA] ?: true,
            advanceReminderMinutes = prefs[Keys.ADVANCE_REMINDER_MIN] ?: 0,
            notificationSoundMode = runCatching { NotificationSoundMode.valueOf(prefs[Keys.SOUND_MODE] ?: "ADHAN_CHIME") }.getOrDefault(NotificationSoundMode.ADHAN_CHIME),
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
}
