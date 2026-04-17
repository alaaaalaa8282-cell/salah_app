package com.mohamedabdelazeim.salah.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mohamedabdelazeim.salah.data.AppPrefs
import com.mohamedabdelazeim.salah.service.AzanService

class AzanReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        val index = intent.getIntExtra("prayer_index", 0)
        val name = intent.getStringExtra("prayer_name") ?: ""

        // لو مفيش صوت (صامت) - رجدول الصلاة الجاية بس
        if (AppPrefs.isPrayerSilent(ctx, index)) {
            rescheduleNext(ctx)
            return
        }

        val serviceIntent = Intent(ctx, AzanService::class.java).apply {
            putExtra("prayer_index", index)
            putExtra("prayer_name", name)
        }
        ctx.startForegroundService(serviceIntent)
    }

    private fun rescheduleNext(ctx: Context) {
        if (AppPrefs.isLocationSaved(ctx)) {
            com.mohamedabdelazeim.salah.data.PrayerScheduler.scheduleAll(
                ctx,
                AppPrefs.getLatitude(ctx),
                AppPrefs.getLongitude(ctx)
            )
        }
    }
}
