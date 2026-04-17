package com.mohamedabdelazeim.salah.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.mohamedabdelazeim.salah.R
import com.mohamedabdelazeim.salah.data.AppPrefs
import com.mohamedabdelazeim.salah.data.PrayerScheduler
import com.mohamedabdelazeim.salah.ui.screens.AzanActivity

class AzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "azan_channel"
        const val NOTIF_ID = 3001
        const val ACTION_STOP = "com.mohamedabdelazeim.salah.STOP_AZAN"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAzan()
            return START_NOT_STICKY
        }

        val index = intent?.getIntExtra("prayer_index", 0) ?: 0
        val name = intent?.getStringExtra("prayer_name") ?: ""

        // Show azan screen
        val azanIntent = Intent(this, AzanActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("prayer_index", index)
            putExtra("prayer_name", name)
        }
        startActivity(azanIntent)

        // Show notification
        val notif = buildNotification(name)
        startForeground(NOTIF_ID, notif)

        // Play azan sound
        playAzan(index)

      
        return START_NOT_STICKY
    }

    private fun playAzan(index: Int) {
        val soundKey = AppPrefs.getPrayerSound(this, index)
        val customUri = AppPrefs.getCustomSoundUri(this, index)

        mediaPlayer?.release()
        mediaPlayer = try {
            when {
                customUri != null -> MediaPlayer().apply {
                    setDataSource(this@AzanService, Uri.parse(customUri))
                    prepare()
                }
                soundKey == "azan2" -> MediaPlayer.create(this, R.raw.azan2)
                else -> MediaPlayer.create(this, R.raw.azan1)
            }
        } catch (e: Exception) {
            try { MediaPlayer.create(this, R.raw.azan1) } catch (e2: Exception) { null }
        }

        mediaPlayer?.setOnCompletionListener { stopAzan() }
        mediaPlayer?.start()
    }

    fun stopAzan() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(prayerName: String): Notification {
        val stopIntent = Intent(this, AzanService::class.java).apply { action = ACTION_STOP }
        val stopPi = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val bmp = try { BitmapFactory.decodeResource(resources, R.drawable.notification_father) } catch (e: Exception) { null }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .apply { if (bmp != null) setLargeIcon(bmp) }
            .setContentTitle("🕌 حان وقت $prayerName")
            .setContentText("اضغط لإيقاف الأذان")
            .addAction(R.mipmap.ic_launcher, "إيقاف الأذان", stopPi)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "أذان", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "أذان وقت الصلاة"
            enableVibration(true)
        }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    private fun acquireWakeLock() {
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SalahApp::AzanWakeLock")
        wakeLock?.acquire(15 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
