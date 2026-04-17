package com.mohamedabdelazeim.salah.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mohamedabdelazeim.salah.data.AppPrefs
import com.mohamedabdelazeim.salah.data.PrayerScheduler
import com.mohamedabdelazeim.salah.data.ZekrScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (AppPrefs.isLocationSaved(ctx)) {
                PrayerScheduler.scheduleAll(ctx, AppPrefs.getLatitude(ctx), AppPrefs.getLongitude(ctx))
            }
            if (AppPrefs.isZekrEnabled(ctx)) {
                ZekrScheduler.schedule(ctx, AppPrefs.getZekrIntervalMinutes(ctx).toLong())
            }
        }
    }
}
