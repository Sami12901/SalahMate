package com.example.domain.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Qibla
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class QiblaInfo(
    val bearingFromTrueNorthDegrees: Double,
    val distanceToKaabaKm: Double,
    val cardinalDirection: String
)

enum class CompassSensorReliability {
    HIGH, MEDIUM, LOW, NO_SENSOR
}

data class CompassReadingState(
    val deviceHeadingDegrees: Float = 0f,
    val reliability: CompassSensorReliability = CompassSensorReliability.NO_SENSOR,
    val hasSensor: Boolean = false
)

object QiblaCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    fun calculateQibla(latitude: Double, longitude: Double): QiblaInfo {
        val safeLat = latitude.coerceIn(-90.0, 90.0)
        val safeLon = longitude.coerceIn(-180.0, 180.0)
        val qibla = Qibla(Coordinates(safeLat, safeLon))
        val bearing = ((qibla.direction % 360.0) + 360.0) % 360.0
        val distanceKm = haversineDistanceKm(safeLat, safeLon, KAABA_LAT, KAABA_LON)
        val cardinal = bearingToCardinal(bearing)
        return QiblaInfo(
            bearingFromTrueNorthDegrees = bearing,
            distanceToKaabaKm = distanceKm,
            cardinalDirection = cardinal
        )
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c * 10.0).roundToInt() / 10.0
    }

    private fun bearingToCardinal(deg: Double): String {
        val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val idx = (((deg + 22.5) % 360.0) / 45.0).toInt().coerceIn(0, 7)
        return dirs[idx]
    }

    fun observeCompassHeading(context: Context): Flow<CompassReadingState> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            trySend(CompassReadingState(hasSensor = false, reliability = CompassSensorReliability.NO_SENSOR))
            close()
            return@callbackFlow
        }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magSensor == null)) {
            trySend(CompassReadingState(hasSensor = false, reliability = CompassSensorReliability.NO_SENSOR))
            awaitClose { }
            return@callbackFlow
        }

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMag = false
        var currentReliability = CompassSensorReliability.MEDIUM

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
                        trySend(
                            CompassReadingState(
                                deviceHeadingDegrees = azimuth,
                                reliability = currentReliability,
                                hasSensor = true
                            )
                        )
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasMag = true
                    }
                }

                if (rotationSensor == null && hasGravity && hasMag) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
                        trySend(
                            CompassReadingState(
                                deviceHeadingDegrees = azimuth,
                                reliability = currentReliability,
                                hasSensor = true
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                currentReliability = when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassSensorReliability.HIGH
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassSensorReliability.MEDIUM
                    else -> CompassSensorReliability.LOW
                }
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}

// --- Transparent Zakat Calculator Engine ---
enum class NisabStandard(val grams: Double, val labelEn: String, val labelBn: String) {
    SILVER_612G(612.36, "Silver Standard (612.36g — Lower threshold, benefits poor more)", "রৌপ্য নিসাব (৬১২.৩৬ গ্রাম — দরিদ্রদের জন্য অধিক কল্যাণকর)"),
    GOLD_87G(87.48, "Gold Standard (87.48g / 7.5 Tola)", "স্বর্ণ নিসাব (৮৭.৪৮ গ্রাম / ৭.৫ ভরি)")
}

data class ZakatInputModel(
    val cashOnHandAndBank: Double = 0.0,
    val goldGrams24kEquivalent: Double = 0.0,
    val silverGrams: Double = 0.0,
    val tradeInventoryValue: Double = 0.0,
    val investmentAndReceivables: Double = 0.0,
    val deductibleDebtsAndLiabilities: Double = 0.0,
    val goldPricePerGram: Double = 11500.0,
    val silverPricePerGram: Double = 145.0,
    val nisabStandard: NisabStandard = NisabStandard.SILVER_612G
)

data class ZakatBreakdownResult(
    val cashTotal: Double,
    val goldTotalValue: Double,
    val silverTotalValue: Double,
    val tradeAndInvestmentsTotal: Double,
    val grossZakatableAssets: Double,
    val deductedLiabilities: Double,
    val netZakatableWealth: Double,
    val nisabThresholdAmount: Double,
    val isZakatDue: Boolean,
    val totalZakatPayable: Double
)

object ZakatCalculatorEngine {
    const val ZAKAT_RATE = 0.025 // 2.5% lunar year rate

    fun calculate(input: ZakatInputModel): ZakatBreakdownResult {
        val cash = input.cashOnHandAndBank.coerceAtLeast(0.0)
        val goldVal = (input.goldGrams24kEquivalent.coerceAtLeast(0.0)) * input.goldPricePerGram.coerceAtLeast(0.0)
        val silverVal = (input.silverGrams.coerceAtLeast(0.0)) * input.silverPricePerGram.coerceAtLeast(0.0)
        val trade = (input.tradeInventoryValue.coerceAtLeast(0.0)) + (input.investmentAndReceivables.coerceAtLeast(0.0))
        val gross = cash + goldVal + silverVal + trade
        val debts = input.deductibleDebtsAndLiabilities.coerceAtLeast(0.0)
        val net = (gross - debts).coerceAtLeast(0.0)

        val nisabAmount = when (input.nisabStandard) {
            NisabStandard.SILVER_612G -> NisabStandard.SILVER_612G.grams * input.silverPricePerGram.coerceAtLeast(0.0)
            NisabStandard.GOLD_87G -> NisabStandard.GOLD_87G.grams * input.goldPricePerGram.coerceAtLeast(0.0)
        }

        val isDue = net >= nisabAmount && nisabAmount > 0.0
        val payable = if (isDue) net * ZAKAT_RATE else 0.0

        return ZakatBreakdownResult(
            cashTotal = cash,
            goldTotalValue = goldVal,
            silverTotalValue = silverVal,
            tradeAndInvestmentsTotal = trade,
            grossZakatableAssets = gross,
            deductedLiabilities = debts,
            netZakatableWealth = net,
            nisabThresholdAmount = nisabAmount,
            isZakatDue = isDue,
            totalZakatPayable = payable
        )
    }
}
