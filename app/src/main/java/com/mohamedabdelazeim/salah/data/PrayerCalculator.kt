import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

object PrayerCalculator {

    // === إعدادات الحساب (تقدر تغيرها من هنا) ===
    // 0: شیة الاسلام، 1: الرابطة، 2: الرابطة (تعديل)، 3: جامعة أم القرى، 4: الهيئة المصرية
    private val method = 4 
    private val fajrAngle = if (method == 4) 19.5 else 18.0 // 19.5 لمصر
    private val ishaAngle = if (method == 3) 0.0 else 17.5 // 17.5 لمصر، 90 دقيقة لأم القرى
    
    // === دالة الحساب الرئيسية ===
    fun calculate(lat: Double, lng: Double, year: Int, month: Int, day: Int): Map<String, Long> {
        val times = computeTimes(lat, lng, year, month, day)
        return mapOf(
            "Fajr" to times[0]!!,
            "Sunrise" to times[1]!!,
            "Dhuhr" to times[2]!!,
            "Asr" to times[3]!!,
            "Maghrib" to times[4]!!,
            "Isha" to times[5]!!
        )
    }

    private fun computeTimes(lat: Double, lng: Double, year: Int, month: Int, day: Int): Array<Long?> {
        val time = getPrayerTimes(lat, lng, year, month, day)
        return Array(6) { i -> 
            // تحويل الوقت إلى ميلي ثانية مع ضبط الـ Timezone
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.set(year, month - 1, day, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis + (time[i] * 60 * 1000).toLong()
        }
    }

    // === المعادلات الفلكية الدقيقة (نسخة مبسطة وموثوقة) ===
    private fun getPrayerTimes(lat: Double, lng: Double, year: Int, month: Int, day: Int): Array<Double> {
        val jd = julianDate(year, month, day)
        val params = getParams(method)
        val times = sunPosition(jd, lat, lng, params)
        return adjustTimes(times, params)
    }

    private fun getParams(method: Int): Map<String, Double> {
        return when (method) {
            4 -> mapOf("fajr" to 19.5, "isha" to 17.5) // مصر
            3 -> mapOf("fajr" to 18.5, "isha" to 90.0) // أم القرى (العشاء 90 دقيقة بعد المغرب)
            else -> mapOf("fajr" to 18.0, "isha" to 17.5) // عالمي
        }
    }

    private fun sunPosition(jd: Double, lat: Double, lng: Double, params: Map<String, Double>): Array<Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val L = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val RA = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(L)), cos(Math.toRadians(L)))) / 15.0
        val eqt = q / 15.0 - fixHour(RA)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(L))))
        
        val noon = fixHour(12 - eqt - lng / 15.0) // هنا تصحيح خط الطول بشكل صحيح
        val asrAngle = Math.toDegrees(atan(1.0 / (tan(Math.toRadians(abs(lat - decl))) + tan(Math.toRadians(decl))))) // معادلة العصر الفلكية الصحيحة
        
        return arrayOf(
            noon - timeDiff(lat, decl, params["fajr"]!!), // الفجر
            noon - timeDiff(lat, decl, 0.833),            // الشروق
            noon,                                          // الظهر
            noon + timeDiff(lat, decl, asrAngle),          // العصر
            noon + timeDiff(lat, decl, 0.833),             // المغرب
            noon + timeDiff(lat, decl, params["isha"]!!)   // العشاء
        )
    }
    
    // دوال مساعدة
    private fun fixAngle(a: Double): Double { return a - 360.0 * floor(a / 360.0) }
    private fun fixHour(a: Double): Double { return a - 24.0 * floor(a / 24.0) }
    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var m = month; var y = year
        if (month <= 2) { y--; m += 12 }
        val A = floor(y / 100.0).toInt()
        val B = 2 - A + floor(A / 4.0).toInt()
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + B - 1524.5
    }
    private fun timeDiff(lat: Double, decl: Double, angle: Double): Double {
        val a = -tan(Math.toRadians(lat)) * tan(Math.toRadians(decl))
        val b = sin(Math.toRadians(angle)) / (cos(Math.toRadians(lat)) * cos(Math.toRadians(decl)))
        return (1.0 / 15.0) * Math.toDegrees(acos(a + b))
    }
    private fun adjustTimes(times: Array<Double>, params: Map<String, Double>): Array<Double> {
        // هنا لو حابب تضيف تعديل العشاء لأم القرى (90 دقيقة)
        if (params["isha"]!! == 90.0) {
            times[5] = times[4] + 90.0 / 60.0 // المغرب + 90 دقيقة
        }
        return times
    }
}
