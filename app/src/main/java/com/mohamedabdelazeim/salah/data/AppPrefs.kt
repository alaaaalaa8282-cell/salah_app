package com.mohamedabdelazeim.salah.data

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {
    private const val PREFS = "salah_prefs"

    // Location
    fun getLatitude(ctx: Context) = getPrefs(ctx).getFloat("lat", 0f).toDouble()
    fun getLongitude(ctx: Context) = getPrefs(ctx).getFloat("lng", 0f).toDouble()
    fun isLocationSaved(ctx: Context) = getPrefs(ctx).getBoolean("location_saved", false)
    fun saveLocation(ctx: Context, lat: Double, lng: Double) {
        getPrefs(ctx).edit()
            .putFloat("lat", lat.toFloat())
            .putFloat("lng", lng.toFloat())
            .putBoolean("location_saved", true)
            .apply()
    }

    // Prayer sounds - each prayer can have custom sound or silent
    // 0 = azan1, 1 = azan2, 2 = silent, 3+ = custom uri
    fun getPrayerSound(ctx: Context, prayerIndex: Int): String =
        getPrefs(ctx).getString("sound_$prayerIndex", "azan1") ?: "azan1"

    fun setPrayerSound(ctx: Context, prayerIndex: Int, sound: String) =
        getPrefs(ctx).edit().putString("sound_$prayerIndex", sound).apply()

    fun isPrayerSilent(ctx: Context, prayerIndex: Int): Boolean =
        getPrefs(ctx).getBoolean("silent_$prayerIndex", false)

    fun setPrayerSilent(ctx: Context, prayerIndex: Int, silent: Boolean) =
        getPrefs(ctx).edit().putBoolean("silent_$prayerIndex", silent).apply()

    // Custom sound URI per prayer
    fun getCustomSoundUri(ctx: Context, prayerIndex: Int): String? =
        getPrefs(ctx).getString("custom_uri_$prayerIndex", null)

    fun setCustomSoundUri(ctx: Context, prayerIndex: Int, uri: String) =
        getPrefs(ctx).edit().putString("custom_uri_$prayerIndex", uri).apply()

        // Volume settings (0.0 to 1.0)
fun getAzanVolume(ctx: Context): Float = getPrefs(ctx).getFloat("azan_volume", 1.0f)
fun setAzanVolume(ctx: Context, v: Float) = getPrefs(ctx).edit().putFloat("azan_volume", v).apply()

fun getZekrVolume(ctx: Context): Float = getPrefs(ctx).getFloat("zekr_volume", 1.0f)
fun setZekrVolume(ctx: Context, v: Float) = getPrefs(ctx).edit().putFloat("zekr_volume", v).apply()

// Azan background image
fun getAzanBgUri(ctx: Context): String? = getPrefs(ctx).getString("azan_bg_uri", null)
fun setAzanBgUri(ctx: Context, uri: String) = getPrefs(ctx).edit().putString("azan_bg_uri", uri).apply()
    // Zekr settings
    fun getZekrIntervalMinutes(ctx: Context) = getPrefs(ctx).getInt("zekr_interval", 30)
    fun setZekrIntervalMinutes(ctx: Context, v: Int) = getPrefs(ctx).edit().putInt("zekr_interval", v).apply()
    fun isZekrEnabled(ctx: Context) = getPrefs(ctx).getBoolean("zekr_enabled", false)
    fun setZekrEnabled(ctx: Context, v: Boolean) = getPrefs(ctx).edit().putBoolean("zekr_enabled", v).apply()
    fun nextZekrIndex(ctx: Context): Int {
        val prefs = getPrefs(ctx)
        val current = prefs.getInt("zekr_index", 0)
        val next = (current + 1) % ZekrData.zekrList.size
        prefs.edit().putInt("zekr_index", next).apply()
        return current
    }

    private fun getPrefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
