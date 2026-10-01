package com.example.mindful

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class UnlockMonitorService : Service() {

    private var unlockDetection: UnlockDetection? = null

    override fun onCreate() {
        super.onCreate()
        MindfulNotifications.ensureChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                MindfulNotifications.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(MindfulNotifications.NOTIFICATION_ID, notification)
        }
        startUnlockDetection()
        return START_STICKY
    }

    override fun onDestroy() {
        stopUnlockDetection()
        UnlockPrompt.dismiss()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startUnlockDetection() {
        if (unlockDetection != null) return
        unlockDetection = UnlockDetection(this) {
            UnlockPrompt.show(applicationContext)
        }.also { it.register() }
    }

    private fun stopUnlockDetection() {
        unlockDetection?.unregister()
        unlockDetection = null
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, MindfulNotifications.CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_stat_mindful)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, UnlockMonitorService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            MindfulPrefs.setMonitoringEnabled(context, false)
            context.stopService(Intent(context, UnlockMonitorService::class.java))
        }
    }
}
