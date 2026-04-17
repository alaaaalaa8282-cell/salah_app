package com.mohamedabdelazeim.salah.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mohamedabdelazeim.salah.receiver.AzanReceiver
import java.util.Calendar

object PrayerScheduler {

    fun scheduleAll(ctx: Context, lat: Double, lng: Double) {
        val times = PrayerCalculator.calculate(lat, lng)
        val prayers = PrayerCalculator.getPrayerList(times)
        val now = System.currentTimeMillis()

        prayers.forEach { prayer ->
            var timeMillis = prayer.timeMillis
            // لو الوقت فات، جدول لبكره
            if (timeMillis <= now) {
                val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tomorrowTimes = PrayerCalculator.calculate(lat, lng, tomorrow)
                val tomorrowPrayers = PrayerCalculator.getPrayerList(tomorrowTimes)
                timeMillis = tomorrowPrayers[prayer.index].timeMillis
            }
            scheduleOne(ctx, prayer.index, prayer.nameAr, timeMillis)
        }
    }

    private fun scheduleOne(ctx: Context, index: Int, name: String, timeMillis: Long) {
        val intent = Intent(ctx, AzanReceiver::class.java).apply {
            putExtra("prayer_index", index)
            putExtra("prayer_name", name)
        }
        val pi = PendingIntent.getBroadcast(
            ctx, index, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi)
        } catch (e: Exception) {
            am.set(AlarmManager.RTC_WAKEUP, timeMillis, pi)
        }
    }

    fun cancelAll(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..4) {
            val intent = Intent(ctx, AzanReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                ctx, i, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }
}
