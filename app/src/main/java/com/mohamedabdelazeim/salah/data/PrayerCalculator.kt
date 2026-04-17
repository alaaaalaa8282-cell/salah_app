package com.mohamedabdelazeim.salah.data

import kotlin.math.*
import java.util.Calendar

data class PrayerTimes(
    val fajr: Long,
    val sunrise: Long,
    val dhuhr: Long,
    val asr: Long,
    val maghrib: Long,
    val isha: Long,
    val date: String
)

data class PrayerTime(
    val name: String,
    val nameAr: String,
    val timeMillis: Long,
    val index: Int
)

object PrayerCalculator {

    // حساب مواقيت الصلاة بدون إنترنت
    fun calculate(lat: Double, lng: Double, cal: Calendar = Calendar.getInstance()): PrayerTimes {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val timeZone = cal.timeZone.getOffset(cal.timeInMillis) / 3600000.0

        val jd = julianDate(year, month, day)
        val d = jd - 2451545.0

        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))

        val noon = fixHour(12.0 - eqt)
        val sunriseAngle = -0.8333
        val fajrAngle = -18.0
        val ishaAngle = -17.0

        val fajrT = noon - timeDiff(lat, decl, fajrAngle)
        val sunriseT = noon - timeDiff(lat, decl, sunriseAngle)
        val dhuhrT = noon + 0.0
        val asrT = noon + asrTime(lat, decl, 1.0)
        val maghribT = noon + timeDiff(lat, decl, sunriseAngle)
        val ishaT = noon + timeDiff(lat, decl, ishaAngle)

        fun toMillis(t: Double): Long {
            val adjusted = fixHour(t + timeZone - lng / 15.0)
            val h = adjusted.toInt()
            val m = ((adjusted - h) * 60).toInt()
            val c = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return c.timeInMillis
        }

        val dateStr = "$day/$month/$year"

        return PrayerTimes(
            fajr = toMillis(fajrT),
            sunrise = toMillis(sunriseT),
            dhuhr = toMillis(dhuhrT),
            asr = toMillis(asrT),
            maghrib = toMillis(maghribT),
            isha = toMillis(ishaT),
            date = dateStr
        )
    }

    fun getPrayerList(times: PrayerTimes): List<PrayerTime> = listOf(
        PrayerTime("Fajr", "الفجر", times.fajr, 0),
        PrayerTime("Dhuhr", "الظهر", times.dhuhr, 1),
        PrayerTime("Asr", "العصر", times.asr, 2),
        PrayerTime("Maghrib", "المغرب", times.maghrib, 3),
        PrayerTime("Isha", "العشاء", times.isha, 4)
    )

    fun getNextPrayer(times: PrayerTimes): PrayerTime? {
        val now = System.currentTimeMillis()
        return getPrayerList(times).firstOrNull { it.timeMillis > now }
    }

    private fun julianDate(y: Int, m: Int, d: Int): Double {
        var year = y; var month = m
        if (month <= 2) { year--; month += 12 }
        val a = (year / 100.0).toInt()
        val b = 2 - a + (a / 4.0).toInt()
        return (365.25 * (year + 4716)).toInt() + (30.6001 * (month + 1)).toInt() + d + b - 1524.5
    }

    private fun fixAngle(a: Double) = a - 360.0 * floor(a / 360.0)
    private fun fixHour(a: Double) = a - 24.0 * floor(a / 24.0)

    private fun timeDiff(lat: Double, decl: Double, angle: Double): Double {
        val val1 = -sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val val2 = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        return Math.toDegrees(acos(val1 / val2)) / 15.0
    }

    private fun asrTime(lat: Double, decl: Double, shadow: Double): Double {
        val target = Math.toDegrees(atan(1.0 / (shadow + tan(Math.toRadians(abs(lat - decl))))))
        return timeDiff(lat, decl, target)
    }
}
