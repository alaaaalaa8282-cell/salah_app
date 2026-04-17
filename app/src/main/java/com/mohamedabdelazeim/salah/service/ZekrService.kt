package com.mohamedabdelazeim.salah.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.mohamedabdelazeim.salah.MainActivity
import com.mohamedabdelazeim.salah.R
import com.mohamedabdelazeim.salah.data.AppPrefs
import com.mohamedabdelazeim.salah.data.ZekrData
import com.mohamedabdelazeim.salah.data.ZekrScheduler

class ZekrService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "zekr_channel"
        const val NOTIF_ID = 2001
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isCallActive() || isInCommunication()) {
            reschedule()
            stopSelf()
            return START_NOT_STICKY
        }

        val index = AppPrefs.nextZekrIndex(this)
        val zekr = ZekrData.zekrList[index]

        acquireWakeLock()
        val notif = buildNotification(zekr.name, zekr.text)
        startForeground(NOTIF_ID, notif)

        if (zekr.audioRes != null) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, zekr.audioRes)
            mediaPlayer?.setOnCompletionListener {
                it.release()
                reschedule()
                releaseWakeLock()
                stopSelf()
            }
            mediaPlayer?.start()
        } else {
            android.os.Handler(mainLooper).postDelayed({
                reschedule()
                releaseWakeLock()
                stopSelf()
            }, 5000)
        }
        return START_NOT_STICKY
    }

    private fun reschedule() {
        if (AppPrefs.isZekrEnabled(this)) {
            ZekrScheduler.schedule(this, AppPrefs.getZekrIntervalMinutes(this).toLong())
        }
    }

    private fun isCallActive(): Boolean = try {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.callState != TelephonyManager.CALL_STATE_IDLE
    } catch (e: Exception) { false }

    private fun isInCommunication(): Boolean = try {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode == AudioManager.MODE_IN_CALL || am.mode == AudioManager.MODE_IN_COMMUNICATION
    } catch (e: Exception) { false }

    private fun buildNotification(title: String, text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val bmp = try { BitmapFactory.decodeResource(resources, R.drawable.notification_father) } catch (e: Exception) { null }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .apply { if (bmp != null) setLargeIcon(bmp) }
            .setContentTitle("🤲 $title")
            .setContentText(text.take(80))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "أذكار", NotificationManager.IMPORTANCE_HIGH)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    private fun acquireWakeLock() {
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SalahApp::ZekrWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)
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
