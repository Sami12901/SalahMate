package com.example.domain.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.SunnahTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.core.database.CachedPrayerDayEntity
import com.example.core.database.SalahMateDao
import com.example.core.network.AlAdhanApiService
import com.example.core.network.NetworkModule
import com.example.core.preferences.AsrJuristicSchool
import com.example.core.preferences.CalculationMethodOption
import com.example.core.preferences.PrayerProviderMode
import com.example.core.preferences.UserPreferencesState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

enum class DailyPrayerType(val titleEn: String, val titleBn: String) {
    FAJR("Fajr", "ফজর"),
    SUNRISE("Sunrise", "সূর্যোদয়"),
    DHUHR("Dhuhr", "যোহর"),
    ASR("Asr", "আসর"),
    MAGHRIB("Maghrib", "মাগরিব"),
    ISHA("Isha", "এশা")
}

data class SinglePrayerEntry(
    val type: DailyPrayerType,
    val timeMillis: Long,
    val formatted12h: String,
    val formatted24h: String,
    val adjustmentMinutes: Int,
    val isObligatory: Boolean = type != DailyPrayerType.SUNRISE,
    val jamaahOffsetMinutes: Int = 0,
    val jamaahFormatted12h: String = ""
)

data class DailyPrayerSchedule(
    val dateIso: String,
    val readableGregorianDate: String,
    val hijriDate: HijriDateInfo,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val method: CalculationMethodOption,
    val asrSchool: AsrJuristicSchool,
    val sourceLabel: String, // e.g., "Adhan Kotlin Local Engine (Offline)" or "AlAdhan API (Live)" or "AlAdhan API (Cached Offline)"
    val isStaleCache: Boolean,
    val imsakFormatted: String,
    val sunriseFormatted: String,
    val sunsetFormatted: String,
    val midnightFormatted: String,
    val lastThirdFormatted: String,
    val entries: List<SinglePrayerEntry>,
    val onlineComparisonDiffMinutes: Map<DailyPrayerType, Int> = emptyMap()
) {
    fun prayerTimeFor(type: DailyPrayerType): SinglePrayerEntry? = entries.find { it.type == type }
}

data class NextPrayerStatus(
    val currentPrayer: DailyPrayerType?,
    val nextPrayer: DailyPrayerType,
    val nextPrayerTimeFormatted: String,
    val nextPrayerMillis: Long,
    val remainingHours: Long,
    val remainingMinutes: Long,
    val remainingSeconds: Long,
    val formattedCountdown: String
)

data class HijriDateInfo(
    val day: Int,
    val monthNumber: Int,
    val monthNameEn: String,
    val monthNameBn: String,
    val monthNameAr: String,
    val year: Int,
    val adjustmentDays: Int,
    val islamicEventsToday: List<IslamicEventItem>
) {
    val formattedEn: String get() = "$day $monthNameEn $year AH"
    val formattedBn: String get() = "$day $monthNameBn $year হিজরি"
}

data class IslamicEventItem(
    val hijriMonth: Int,
    val hijriDay: Int,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String
)

object LocalAdhanCalculator {

    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val HIJRI_MONTHS_BN = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    private val HIJRI_MONTHS_AR = listOf(
        "مُحَرَّم", "صَفَر", "رَبِيع الأَوَّل", "رَبِيع الثَّانِي",
        "جُمَادَىٰ الأُولَىٰ", "جُمَادَىٰ الآخِرَة", "رَجَب", "شَعْبَان",
        "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
    )

    val ISLAMIC_EVENTS = listOf(
        IslamicEventItem(1, 1, "Islamic New Year (1st Muharram)", "হিজরি নববর্ষ (১ মুহাররম)", "Beginning of the new Hijri lunar calendar year.", "হিজরি চান্দ্র বছরের প্রথম দিন।"),
        IslamicEventItem(1, 10, "Day of Ashura (10th Muharram)", "পবিত্র আশুরা (১০ মুহাররম)", "Recommended day of voluntary fasting (with 9th or 11th Muharram).", "নফল রোজা রাখার ফজিলতপূর্ণ দিন (৯ বা ১১ তারিখের সাথে মিলিয়ে)।"),
        IslamicEventItem(3, 12, "12th Rabi' al-Awwal (Sirah Remembrance)", "১২ রবিউল আউয়াল (সীরাতুন্নবী স্মরণ)", "Historical month of the birth and passing of Prophet Muhammad (ﷺ).", "মহানবী মুহাম্মদ (ﷺ)-এর জন্ম ও ওফাতের ঐতিহাসিক মাস।"),
        IslamicEventItem(7, 27, "27th Rajab (Isra and Mi'raj Reminder)", "২৭ রজব (ইসরা ও মিরাজ স্মরণ)", "Traditional remembrance of the Night Journey and Ascension.", "ইসরা ও মিরাজের ঐতিহাসিক ঘটনার স্মরণ।"),
        IslamicEventItem(8, 15, "Mid-Sha'ban (15th Sha'ban)", "মধ্য শাবান (১৫ শাবান)", "Midpoint of Sha'ban preceding the blessed month of Ramadan.", "পবিত্র রমজানের পূর্ববর্তী শাবান মাসের মধ্যভাগ।"),
        IslamicEventItem(9, 1, "First Day of Ramadan", "পবিত্র রমজান শুরু (১ রমজান)", "Beginning of obligatory fasting, subject to local moon sighting.", "ফরজ রোজার মাস শুরু (স্থানীয় চাঁদ দেখার ওপর নির্ভরশীল)।"),
        IslamicEventItem(9, 27, "Last 10 Nights / Odd Nights of Ramadan", "লাইলাতুল কদর অনুসন্ধান (শেষ দশকের বেজোড় রাত)", "Seek Laylatul Qadr in the odd nights of the final ten days of Ramadan.", "রমজানের শেষ দশকের বেজোড় রাতগুলোতে লাইলাতুল কদর অনুসন্ধান করুন।"),
        IslamicEventItem(10, 1, "Eid al-Fitr (1st Shawwal)", "ঈদুল ফিতর (১ শাওয়াল)", "Festival of breaking the fast after completing Ramadan.", "রমজান শেষে শাওয়ালের প্রথম দিন পবিত্র ঈদুল ফিতর।"),
        IslamicEventItem(12, 9, "Day of Arafah (9th Dhu al-Hijjah)", "আরাফার দিন (৯ জিলহজ)", "Culmination of Hajj; highly recommended day of fasting for non-pilgrims.", "হজের মূল দিন; হাজী ছাড়া অন্যদের জন্য অত্যন্ত ফজিলতপূর্ণ নফল রোজার দিন।"),
        IslamicEventItem(12, 10, "Eid al-Adha (10th Dhu al-Hijjah)", "ঈদুল আযহা (১০ জিলহজ)", "Festival of sacrifice commemorating Prophet Ibrahim's (AS) devotion.", "ইবরাহীম (আ.)-এর ত্যাগের স্মরণে পবিত্র ঈদুল আযহা।")
    )

    fun buildCalculationParameters(
        method: CalculationMethodOption,
        school: AsrJuristicSchool,
        fajrOffset: Int = 0,
        sunriseOffset: Int = 0,
        dhuhrOffset: Int = 0,
        asrOffset: Int = 0,
        maghribOffset: Int = 0,
        ishaOffset: Int = 0
    ): CalculationParameters {
        val baseParams = when (method) {
            CalculationMethodOption.KARACHI -> CalculationMethod.KARACHI.parameters
            CalculationMethodOption.MWL -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
            CalculationMethodOption.ISNA -> CalculationMethod.NORTH_AMERICA.parameters
            CalculationMethodOption.UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters
            CalculationMethodOption.EGYPTIAN -> CalculationMethod.EGYPTIAN.parameters
            CalculationMethodOption.DUBAI -> CalculationMethod.DUBAI.parameters
            CalculationMethodOption.SINGAPORE -> CalculationMethod.SINGAPORE.parameters
        }
        // Explicitly keep Asr juristic school separate from the calculation method
        baseParams.madhab = when (school) {
            AsrJuristicSchool.STANDARD -> Madhab.SHAFI
            AsrJuristicSchool.HANAFI -> Madhab.HANAFI
        }
        baseParams.adjustments.fajr = fajrOffset
        baseParams.adjustments.sunrise = sunriseOffset
        baseParams.adjustments.dhuhr = dhuhrOffset
        baseParams.adjustments.asr = asrOffset
        baseParams.adjustments.maghrib = maghribOffset
        baseParams.adjustments.isha = ishaOffset
        return baseParams
    }

    fun calculateLocalSchedule(
        prefs: UserPreferencesState,
        targetCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(prefs.timeZoneId))
    ): DailyPrayerSchedule {
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val cal = (targetCalendar.clone() as Calendar).apply { timeZone = tz }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val coordinates = Coordinates(
            prefs.latitude.coerceIn(-90.0, 90.0),
            prefs.longitude.coerceIn(-180.0, 180.0)
        )
        val dateComponents = DateComponents(year, month, day)
        val params = buildCalculationParameters(
            method = prefs.calculationMethod,
            school = prefs.asrSchool,
            fajrOffset = prefs.fajrOffsetMin,
            sunriseOffset = prefs.sunriseOffsetMin,
            dhuhrOffset = prefs.dhuhrOffsetMin,
            asrOffset = prefs.asrOffsetMin,
            maghribOffset = prefs.maghribOffsetMin,
            ishaOffset = prefs.ishaOffsetMin
        )

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        val sunnahTimes = SunnahTimes(prayerTimes)

        fun normalizeSec(date: Date): Long = (date.time / 1000L) * 1000L

        val fmt12 = SimpleDateFormat("hh:mm a", Locale.US).apply { timeZone = tz }
        val fmt24 = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = tz }
        val fmtIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }
        val fmtReadable = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).apply { timeZone = tz }

        val fajrMs = normalizeSec(prayerTimes.fajr)
        val sunriseMs = normalizeSec(prayerTimes.sunrise)
        val dhuhrMs = normalizeSec(prayerTimes.dhuhr)
        val asrMs = normalizeSec(prayerTimes.asr)
        val maghribMs = normalizeSec(prayerTimes.maghrib)
        val ishaMs = normalizeSec(prayerTimes.isha)

        // Separate Imsak clearly from Fajr (precautionary margin before Fajr, default -10m)
        val imsakMillis = fajrMs + (prefs.imsakOffsetMin * 60_000L)

        fun formatJamaah(waqtMillis: Long, jamaahOffsetMin: Int): String {
            if (!prefs.showMosqueJamaah || jamaahOffsetMin <= 0) return ""
            return fmt12.format(Date(waqtMillis + jamaahOffsetMin * 60_000L))
        }

        val entries = listOf(
            SinglePrayerEntry(
                type = DailyPrayerType.FAJR,
                timeMillis = fajrMs,
                formatted12h = fmt12.format(Date(fajrMs)),
                formatted24h = fmt24.format(Date(fajrMs)),
                adjustmentMinutes = prefs.fajrOffsetMin,
                jamaahOffsetMinutes = prefs.fajrJamaahOffsetMin,
                jamaahFormatted12h = formatJamaah(fajrMs, prefs.fajrJamaahOffsetMin)
            ),
            SinglePrayerEntry(
                type = DailyPrayerType.SUNRISE,
                timeMillis = sunriseMs,
                formatted12h = fmt12.format(Date(sunriseMs)),
                formatted24h = fmt24.format(Date(sunriseMs)),
                adjustmentMinutes = prefs.sunriseOffsetMin,
                isObligatory = false,
                jamaahOffsetMinutes = 0,
                jamaahFormatted12h = ""
            ),
            SinglePrayerEntry(
                type = DailyPrayerType.DHUHR,
                timeMillis = dhuhrMs,
                formatted12h = fmt12.format(Date(dhuhrMs)),
                formatted24h = fmt24.format(Date(dhuhrMs)),
                adjustmentMinutes = prefs.dhuhrOffsetMin,
                jamaahOffsetMinutes = prefs.dhuhrJamaahOffsetMin,
                jamaahFormatted12h = formatJamaah(dhuhrMs, prefs.dhuhrJamaahOffsetMin)
            ),
            SinglePrayerEntry(
                type = DailyPrayerType.ASR,
                timeMillis = asrMs,
                formatted12h = fmt12.format(Date(asrMs)),
                formatted24h = fmt24.format(Date(asrMs)),
                adjustmentMinutes = prefs.asrOffsetMin,
                jamaahOffsetMinutes = prefs.asrJamaahOffsetMin,
                jamaahFormatted12h = formatJamaah(asrMs, prefs.asrJamaahOffsetMin)
            ),
            SinglePrayerEntry(
                type = DailyPrayerType.MAGHRIB,
                timeMillis = maghribMs,
                formatted12h = fmt12.format(Date(maghribMs)),
                formatted24h = fmt24.format(Date(maghribMs)),
                adjustmentMinutes = prefs.maghribOffsetMin,
                jamaahOffsetMinutes = prefs.maghribJamaahOffsetMin,
                jamaahFormatted12h = formatJamaah(maghribMs, prefs.maghribJamaahOffsetMin)
            ),
            SinglePrayerEntry(
                type = DailyPrayerType.ISHA,
                timeMillis = ishaMs,
                formatted12h = fmt12.format(Date(ishaMs)),
                formatted24h = fmt24.format(Date(ishaMs)),
                adjustmentMinutes = prefs.ishaOffsetMin,
                jamaahOffsetMinutes = prefs.ishaJamaahOffsetMin,
                jamaahFormatted12h = formatJamaah(ishaMs, prefs.ishaJamaahOffsetMin)
            )
        )

        val hijri = calculateHijriDate(cal, prefs.hijriAdjustmentDays)

        return DailyPrayerSchedule(
            dateIso = fmtIso.format(cal.time),
            readableGregorianDate = fmtReadable.format(cal.time),
            hijriDate = hijri,
            cityName = prefs.cityName,
            countryName = prefs.countryName,
            timeZoneId = prefs.timeZoneId,
            method = prefs.calculationMethod,
            asrSchool = prefs.asrSchool,
            sourceLabel = "Adhan Kotlin Local Engine (Offline)",
            isStaleCache = false,
            imsakFormatted = fmt12.format(Date(imsakMillis)),
            sunriseFormatted = fmt12.format(prayerTimes.sunrise),
            sunsetFormatted = fmt12.format(prayerTimes.maghrib),
            midnightFormatted = fmt12.format(sunnahTimes.middleOfTheNight),
            lastThirdFormatted = fmt12.format(sunnahTimes.lastThirdOfTheNight),
            entries = entries
        )
    }

    /**
     * Computes the active prayer and countdown to the next obligatory prayer.
     * Handles post-Isha midnight wrap-around and cross-midnight date transitions deterministically.
     */
    fun computeNextPrayerStatus(
        todaySchedule: DailyPrayerSchedule,
        prefs: UserPreferencesState,
        nowMillis: Long = System.currentTimeMillis()
    ): NextPrayerStatus {
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val nowIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = tz }.format(Date(nowMillis))
        val effectiveSchedule = if (todaySchedule.dateIso == nowIso) {
            todaySchedule
        } else {
            val nowCal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }
            calculateLocalSchedule(prefs, nowCal)
        }

        val obligatoryEntries = effectiveSchedule.entries.filter { it.isObligatory }
        val nextToday = obligatoryEntries.firstOrNull { it.timeMillis > nowMillis }

        val currentPrayer: DailyPrayerType? = when {
            nowMillis < (effectiveSchedule.prayerTimeFor(DailyPrayerType.FAJR)?.timeMillis ?: 0L) -> DailyPrayerType.ISHA
            nowMillis < (effectiveSchedule.prayerTimeFor(DailyPrayerType.SUNRISE)?.timeMillis ?: 0L) -> DailyPrayerType.FAJR
            nowMillis < (effectiveSchedule.prayerTimeFor(DailyPrayerType.DHUHR)?.timeMillis ?: 0L) -> null // Duha / Post-sunrise
            nowMillis < (effectiveSchedule.prayerTimeFor(DailyPrayerType.ASR)?.timeMillis ?: 0L) -> DailyPrayerType.DHUHR
            nowMillis < (effectiveSchedule.prayerTimeFor(DailyPrayerType.MAGHRIB)?.timeMillis ?: 0L) -> DailyPrayerType.ASR
            nowMillis < (effectiveSchedule.prayerTimeFor(DailyPrayerType.ISHA)?.timeMillis ?: 0L) -> DailyPrayerType.MAGHRIB
            else -> DailyPrayerType.ISHA
        }

        val targetEntry: SinglePrayerEntry = if (nextToday != null) {
            nextToday
        } else {
            val tomorrowCal = Calendar.getInstance(tz).apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowSchedule = calculateLocalSchedule(prefs, tomorrowCal)
            tomorrowSchedule.prayerTimeFor(DailyPrayerType.FAJR) ?: obligatoryEntries.first()
        }

        val diffMillis = (targetEntry.timeMillis - nowMillis).coerceAtLeast(0L)
        val totalSeconds = diffMillis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L

        return NextPrayerStatus(
            currentPrayer = currentPrayer,
            nextPrayer = targetEntry.type,
            nextPrayerTimeFormatted = targetEntry.formatted12h,
            nextPrayerMillis = targetEntry.timeMillis,
            remainingHours = hours,
            remainingMinutes = minutes,
            remainingSeconds = seconds,
            formattedCountdown = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        )
    }

    /**
     * Kuwaiti / Tabular Islamic Calendar conversion with user-defined moon-sighting day offset.
     */
    fun calculateHijriDate(gregorianCalendar: Calendar, adjustmentDays: Int = 0): HijriDateInfo {
        val adjCal = (gregorianCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, adjustmentDays)
        }
        val day = adjCal.get(Calendar.DAY_OF_MONTH).toDouble()
        val month = (adjCal.get(Calendar.MONTH) + 1).toDouble()
        val year = adjCal.get(Calendar.YEAR).toDouble()

        var m = month
        var y = year
        if (m < 3) {
            y -= 1.0
            m += 12.0
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716.0)) + floor(30.6001 * (m + 1.0)) + day + b - 1524.0

        val iyear = 10631.0 / 30.0
        val epochAstro = 1948084.0
        val shift1 = 8.01 / 60.0

        var z = jd - epochAstro
        val cyc = floor(z / 10631.0)
        z -= 10631.0 * cyc
        val j = floor((z - shift1) / iyear)
        val iy = (30.0 * cyc + j).toInt()
        z -= floor(j * iyear + shift1)
        var im = floor((z + 28.5001) / 29.5).toInt()
        if (im == 13) im = 12
        val id = (z - floor(29.5001 * im - 29.0)).toInt().coerceIn(1, 30)
        val safeMonth = im.coerceIn(1, 12)

        val eventsToday = ISLAMIC_EVENTS.filter { it.hijriMonth == safeMonth && it.hijriDay == id }

        return HijriDateInfo(
            day = id,
            monthNumber = safeMonth,
            monthNameEn = HIJRI_MONTHS_EN[safeMonth - 1],
            monthNameBn = HIJRI_MONTHS_BN[safeMonth - 1],
            monthNameAr = HIJRI_MONTHS_AR[safeMonth - 1],
            year = iy,
            adjustmentDays = adjustmentDays,
            islamicEventsToday = eventsToday
        )
    }
}

/**
 * Provider abstraction combining Local Adhan Engine with optional AlAdhan online API
 * fetching, local Room caching, and side-by-side comparison.
 */
class PrayerRepository(
    private val dao: SalahMateDao,
    private val alAdhanApi: AlAdhanApiService = NetworkModule.alAdhanApi
) {
    suspend fun getScheduleForDate(
        prefs: UserPreferencesState,
        calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(prefs.timeZoneId))
    ): DailyPrayerSchedule {
        val localSchedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, calendar)
        if (prefs.providerMode == PrayerProviderMode.LOCAL_ADHAN) {
            return localSchedule
        }

        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val dateDdMmYyyy = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply { timeZone = tz }.format(calendar.time)
        val cacheKey = "${localSchedule.dateIso}_${prefs.cityName}_${prefs.calculationMethod.alAdhanMethodId}_${prefs.asrSchool.alAdhanSchoolCode}"

        val networkResult = NetworkModule.withRetry {
            if (prefs.isManualCoordinates) {
                alAdhanApi.getTimingsByCoordinates(
                    dateDdMmYyyy = dateDdMmYyyy,
                    latitude = prefs.latitude,
                    longitude = prefs.longitude,
                    method = prefs.calculationMethod.alAdhanMethodId,
                    school = prefs.asrSchool.alAdhanSchoolCode
                )
            } else {
                alAdhanApi.getTimingsByCity(
                    dateDdMmYyyy = dateDdMmYyyy,
                    city = prefs.cityName,
                    country = prefs.countryName,
                    method = prefs.calculationMethod.alAdhanMethodId,
                    school = prefs.asrSchool.alAdhanSchoolCode
                )
            }
        }

        val liveData = networkResult.getOrNull()?.data
        val timings = liveData?.timings
        if (timings != null && timings.fajr.isNotBlank()) {
            val hijriText = liveData.date?.hijri?.let { "${it.day} ${it.month?.en ?: ""} ${it.year} AH" } ?: localSchedule.hijriDate.formattedEn
            val entity = CachedPrayerDayEntity(
                cacheKey = cacheKey,
                dateIso = localSchedule.dateIso,
                city = prefs.cityName,
                country = prefs.countryName,
                methodId = prefs.calculationMethod.alAdhanMethodId,
                schoolId = prefs.asrSchool.alAdhanSchoolCode,
                fajr = cleanAlAdhanTime(timings.fajr),
                sunrise = cleanAlAdhanTime(timings.sunrise),
                dhuhr = cleanAlAdhanTime(timings.dhuhr),
                asr = cleanAlAdhanTime(timings.asr),
                sunset = cleanAlAdhanTime(timings.sunset),
                maghrib = cleanAlAdhanTime(timings.maghrib),
                isha = cleanAlAdhanTime(timings.isha),
                imsak = cleanAlAdhanTime(timings.imsak),
                midnight = cleanAlAdhanTime(timings.midnight),
                hijriDateText = hijriText,
                fetchedAtMillis = System.currentTimeMillis()
            )
            dao.insertCachedPrayerDay(entity)
            return mergeOnlineEntityWithLocal(localSchedule, entity, prefs, isCachedFallback = false)
        }

        // Network failed -> check Room cache before falling back to local Adhan calculation
        val cached = dao.getCachedPrayerDay(cacheKey)
        if (cached != null) {
            return mergeOnlineEntityWithLocal(localSchedule, cached, prefs, isCachedFallback = true)
        }

        return localSchedule.copy(
            sourceLabel = "Adhan Local Fallback (AlAdhan Offline)",
            isStaleCache = true
        )
    }

    private fun cleanAlAdhanTime(raw: String): String {
        return raw.substringBefore(" ").trim()
    }

    private fun mergeOnlineEntityWithLocal(
        localSchedule: DailyPrayerSchedule,
        entity: CachedPrayerDayEntity,
        prefs: UserPreferencesState,
        isCachedFallback: Boolean
    ): DailyPrayerSchedule {
        val tz = TimeZone.getTimeZone(prefs.timeZoneId)
        val fmt12 = SimpleDateFormat("hh:mm a", Locale.US).apply { timeZone = tz }
        val fmt24 = SimpleDateFormat("HH:mm", Locale.US).apply { timeZone = tz }

        fun parseOnlineMillis(hhMm: String, offsetMin: Int, fallbackMillis: Long): Long {
            val parts = hhMm.split(":")
            if (parts.size < 2) return fallbackMillis
            val h = parts[0].toIntOrNull() ?: return fallbackMillis
            val m = parts[1].toIntOrNull() ?: return fallbackMillis
            val cal = Calendar.getInstance(tz).apply {
                val dateParts = localSchedule.dateIso.split("-")
                if (dateParts.size == 3) {
                    set(Calendar.YEAR, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                }
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, offsetMin)
            }
            return cal.timeInMillis
        }

        val updatedEntries = localSchedule.entries.map { localEntry ->
            val rawTime = when (localEntry.type) {
                DailyPrayerType.FAJR -> entity.fajr
                DailyPrayerType.SUNRISE -> entity.sunrise
                DailyPrayerType.DHUHR -> entity.dhuhr
                DailyPrayerType.ASR -> entity.asr
                DailyPrayerType.MAGHRIB -> entity.maghrib
                DailyPrayerType.ISHA -> entity.isha
            }
            val onlineMillis = parseOnlineMillis(rawTime, localEntry.adjustmentMinutes, localEntry.timeMillis)
            val jamaahStr = if (prefs.showMosqueJamaah && localEntry.isObligatory && localEntry.jamaahOffsetMinutes > 0) {
                fmt12.format(Date(onlineMillis + localEntry.jamaahOffsetMinutes * 60_000L))
            } else {
                ""
            }
            localEntry.copy(
                timeMillis = onlineMillis,
                formatted12h = fmt12.format(Date(onlineMillis)),
                formatted24h = fmt24.format(Date(onlineMillis)),
                jamaahFormatted12h = jamaahStr
            )
        }

        val diffs = mutableMapOf<DailyPrayerType, Int>()
        localSchedule.entries.forEachIndexed { idx, local ->
            val online = updatedEntries[idx]
            diffs[local.type] = ((online.timeMillis - local.timeMillis) / 60_000L).toInt()
        }

        return localSchedule.copy(
            sourceLabel = if (isCachedFallback) "AlAdhan API (Cached Offline Copy)" else "AlAdhan API (Live Synced)",
            isStaleCache = isCachedFallback,
            entries = updatedEntries,
            onlineComparisonDiffMinutes = diffs
        )
    }
}
