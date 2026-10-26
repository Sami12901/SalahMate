package com.example

import com.example.core.preferences.AsrJuristicSchool
import com.example.core.preferences.CalculationMethodOption
import com.example.core.preferences.PRESET_CITIES
import com.example.core.preferences.UserPreferencesState
import com.example.data.hadith.VerifiedIslamicContent
import com.example.domain.prayer.DailyPrayerType
import com.example.domain.prayer.LocalAdhanCalculator
import com.example.domain.qibla.NisabStandard
import com.example.domain.qibla.QiblaCalculator
import com.example.domain.qibla.ZakatCalculatorEngine
import com.example.domain.qibla.ZakatInputModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {

    @Test
    fun `bangladesh cities prayer times follow strict chronological order`() {
        val bdCities = PRESET_CITIES.filter { it.countryEn == "Bangladesh" }
        assertTrue(bdCities.size >= 8)

        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val testCal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.OCTOBER, 24, 12, 0, 0)
        }

        bdCities.forEach { city ->
            val prefs = UserPreferencesState(
                cityName = city.nameEn,
                countryName = city.countryEn,
                latitude = city.latitude,
                longitude = city.longitude,
                timeZoneId = city.timeZoneId,
                calculationMethod = CalculationMethodOption.KARACHI,
                asrSchool = AsrJuristicSchool.HANAFI
            )
            val schedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, testCal)
            val fajr = schedule.prayerTimeFor(DailyPrayerType.FAJR)!!.timeMillis
            val sunrise = schedule.prayerTimeFor(DailyPrayerType.SUNRISE)!!.timeMillis
            val dhuhr = schedule.prayerTimeFor(DailyPrayerType.DHUHR)!!.timeMillis
            val asr = schedule.prayerTimeFor(DailyPrayerType.ASR)!!.timeMillis
            val maghrib = schedule.prayerTimeFor(DailyPrayerType.MAGHRIB)!!.timeMillis
            val isha = schedule.prayerTimeFor(DailyPrayerType.ISHA)!!.timeMillis

            assertTrue("Fajr < Sunrise for ${city.nameEn}", fajr < sunrise)
            assertTrue("Sunrise < Dhuhr for ${city.nameEn}", sunrise < dhuhr)
            assertTrue("Dhuhr < Asr for ${city.nameEn}", dhuhr < asr)
            assertTrue("Asr < Maghrib for ${city.nameEn}", asr < maghrib)
            assertTrue("Maghrib < Isha for ${city.nameEn}", maghrib < isha)
        }
    }

    @Test
    fun `asr juristic school hanafi is later than standard shafi while other prayers remain unchanged`() {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val cal = Calendar.getInstance(tz).apply { set(2026, Calendar.OCTOBER, 24, 9, 0, 0) }

        val standardPrefs = UserPreferencesState(
            calculationMethod = CalculationMethodOption.KARACHI,
            asrSchool = AsrJuristicSchool.STANDARD
        )
        val hanafiPrefs = standardPrefs.copy(asrSchool = AsrJuristicSchool.HANAFI)

        val stdSched = LocalAdhanCalculator.calculateLocalSchedule(standardPrefs, cal)
        val hanSched = LocalAdhanCalculator.calculateLocalSchedule(hanafiPrefs, cal)

        val stdAsr = stdSched.prayerTimeFor(DailyPrayerType.ASR)!!.timeMillis
        val hanAsr = hanSched.prayerTimeFor(DailyPrayerType.ASR)!!.timeMillis
        assertTrue("Hanafi Asr must be later than Standard Asr", hanAsr > stdAsr)

        assertEquals(
            stdSched.prayerTimeFor(DailyPrayerType.FAJR)!!.timeMillis,
            hanSched.prayerTimeFor(DailyPrayerType.FAJR)!!.timeMillis
        )
        assertEquals(
            stdSched.prayerTimeFor(DailyPrayerType.MAGHRIB)!!.timeMillis,
            hanSched.prayerTimeFor(DailyPrayerType.MAGHRIB)!!.timeMillis
        )
    }

    @Test
    fun `user minute adjustments shift prayer times accurately`() {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val cal = Calendar.getInstance(tz).apply { set(2026, Calendar.OCTOBER, 24, 9, 0, 0) }

        val basePrefs = UserPreferencesState()
        val adjustedPrefs = basePrefs.copy(fajrOffsetMin = 5, maghribOffsetMin = -3)

        val baseSched = LocalAdhanCalculator.calculateLocalSchedule(basePrefs, cal)
        val adjSched = LocalAdhanCalculator.calculateLocalSchedule(adjustedPrefs, cal)

        val fajrDiffMin = (adjSched.prayerTimeFor(DailyPrayerType.FAJR)!!.timeMillis -
            baseSched.prayerTimeFor(DailyPrayerType.FAJR)!!.timeMillis) / 60_000L
        val maghribDiffMin = (adjSched.prayerTimeFor(DailyPrayerType.MAGHRIB)!!.timeMillis -
            baseSched.prayerTimeFor(DailyPrayerType.MAGHRIB)!!.timeMillis) / 60_000L

        assertEquals(5L, fajrDiffMin)
        assertEquals(-3L, maghribDiffMin)
    }

    @Test
    fun `mosque jamaah time is computed separately from solar waqt and imsak`() {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val cal = Calendar.getInstance(tz).apply { set(2026, Calendar.OCTOBER, 24, 9, 0, 0) }

        val prefs = UserPreferencesState(
            showMosqueJamaah = true,
            fajrJamaahOffsetMin = 25,
            dhuhrJamaahOffsetMin = 75,
            asrJamaahOffsetMin = 20,
            maghribJamaahOffsetMin = 10,
            ishaJamaahOffsetMin = 30
        )

        val schedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, cal)
        val fajrEntry = schedule.prayerTimeFor(DailyPrayerType.FAJR)!!
        val sunriseEntry = schedule.prayerTimeFor(DailyPrayerType.SUNRISE)!!

        assertEquals(25, fajrEntry.jamaahOffsetMinutes)
        assertTrue(fajrEntry.jamaahFormatted12h.isNotBlank())
        assertNotEquals(fajrEntry.formatted12h, fajrEntry.jamaahFormatted12h)
        // Sunrise has no Jama'ah congregation time
        assertEquals(0, sunriseEntry.jamaahOffsetMinutes)
        assertEquals("", sunriseEntry.jamaahFormatted12h)
        // Imsak is strictly 10 minutes before Fajr solar Waqt
        assertNotEquals(schedule.imsakFormatted, fajrEntry.formatted12h)
    }

    @Test
    fun `countdown wraps around to next day fajr after isha and across midnight rollover`() {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val day1Cal = Calendar.getInstance(tz).apply { set(2026, Calendar.OCTOBER, 24, 22, 30, 0) }
        val prefs = UserPreferencesState()
        val staleSchedule = LocalAdhanCalculator.calculateLocalSchedule(prefs, day1Cal)

        val postIshaNow = staleSchedule.prayerTimeFor(DailyPrayerType.ISHA)!!.timeMillis + 30 * 60_000L
        val status = LocalAdhanCalculator.computeNextPrayerStatus(staleSchedule, prefs, postIshaNow)

        assertEquals(DailyPrayerType.ISHA, status.currentPrayer)
        assertEquals(DailyPrayerType.FAJR, status.nextPrayer)
        assertTrue(status.nextPrayerMillis > postIshaNow)
        assertTrue(status.formattedCountdown.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))

        // Simulate 00:15 AM on Day 2 (2026-10-25) while holding staleSchedule from Day 1 (2026-10-24)
        val afterMidnightCal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.OCTOBER, 25, 0, 15, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val afterMidnightStatus = LocalAdhanCalculator.computeNextPrayerStatus(
            todaySchedule = staleSchedule,
            prefs = prefs,
            nowMillis = afterMidnightCal.timeInMillis
        )
        assertEquals(DailyPrayerType.FAJR, afterMidnightStatus.nextPrayer)
        assertTrue(
            "Next prayer after midnight must be in the future on Day 2",
            afterMidnightStatus.nextPrayerMillis > afterMidnightCal.timeInMillis
        )
    }

    @Test
    fun `qibla bearing for dhaka points west-northwest toward makkah`() {
        val dhakaQibla = QiblaCalculator.calculateQibla(23.8103, 90.4125)
        assertTrue("Dhaka Qibla should be ~277.6 degrees", dhakaQibla.bearingFromTrueNorthDegrees in 275.0..280.0)
        assertEquals("W", dhakaQibla.cardinalDirection)
        assertTrue(dhakaQibla.distanceToKaabaKm > 5000.0)
    }

    @Test
    fun `zakat calculator computes nisab threshold and 2_5 percent accurately`() {
        val aboveNisabInput = ZakatInputModel(
            cashOnHandAndBank = 200_000.0,
            goldGrams24kEquivalent = 10.0,
            silverGrams = 0.0,
            tradeInventoryValue = 50_000.0,
            investmentAndReceivables = 0.0,
            deductibleDebtsAndLiabilities = 30_000.0,
            goldPricePerGram = 10_000.0,
            silverPricePerGram = 100.0,
            nisabStandard = NisabStandard.SILVER_612G
        )
        val res = ZakatCalculatorEngine.calculate(aboveNisabInput)
        assertTrue(res.isZakatDue)
        assertEquals(320_000.0, res.netZakatableWealth, 0.01)
        assertEquals(8_000.0, res.totalZakatPayable, 0.01)

        val belowNisabInput = aboveNisabInput.copy(
            cashOnHandAndBank = 10_000.0,
            goldGrams24kEquivalent = 0.0,
            tradeInventoryValue = 0.0,
            deductibleDebtsAndLiabilities = 0.0
        )
        val belowRes = ZakatCalculatorEngine.calculate(belowNisabInput)
        assertFalse(belowRes.isZakatDue)
        assertEquals(0.0, belowRes.totalZakatPayable, 0.01)
    }

    @Test
    fun `offline quranic vocabulary flashcards include arabic bengali and english meanings`() {
        val cards = VerifiedIslamicContent.QURANIC_VOCAB_FLASHCARDS
        assertTrue(cards.size >= 15)
        cards.forEach { card ->
            assertTrue(card.arabicWord.isNotBlank())
            assertTrue(card.meaningEn.isNotBlank())
            assertTrue(card.meaningBn.isNotBlank())
            assertTrue(card.quranReference.isNotBlank())
        }
    }
}
