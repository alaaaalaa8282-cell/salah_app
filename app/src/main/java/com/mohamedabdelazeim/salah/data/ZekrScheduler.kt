package com.mohamedabdelazeim.salah.data

import android.content.Context
import android.content.Intent
import com.mohamedabdelazeim.salah.service.ZekrService
import android.app.AlarmManager
import android.app.PendingIntent

object ZekrScheduler {
    private const val ZEKR_REQUEST = 9999

    fun schedule(ctx: Context, intervalMinutes: Long) {
        val intent = Intent(ctx, ZekrService::class.java)
        val pi = PendingIntent.getService(
            ctx, ZEKR_REQUEST, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + intervalMinutes * 60 * 1000
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } catch (e: Exception) {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(ctx: Context) {
        val intent = Intent(ctx, ZekrService::class.java)
        val pi = PendingIntent.getService(
            ctx, ZEKR_REQUEST, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)
    }
}
