package com.example.mindful

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

/**
 * Detects device unlock via system broadcasts and keyguard polling.
 * Some OEMs do not deliver [Intent.ACTION_USER_PRESENT] reliably to background apps.
 */
class UnlockDetection(
    private val context: Context,
    private val onUnlock: () -> Unit,
) {

    private val handler = Handler(Looper.getMainLooper())
    private var polling = false
    private var lastShowAt = 0L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT,
                Intent.ACTION_USER_UNLOCKED -> scheduleShow()
                Intent.ACTION_SCREEN_ON -> onScreenOn()
                Intent.ACTION_SCREEN_OFF -> stopPolling()
            }
        }
    }

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!polling) return
            val keyguard = keyguardManager() ?: return
            if (!keyguard.isKeyguardLocked) {
                stopPolling()
                scheduleShow()
                return
            }
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addAction(Intent.ACTION_USER_UNLOCKED)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    fun unregister() {
        stopPolling()
        try {
            context.unregisterReceiver(receiver)
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun onScreenOn() {
        val keyguard = keyguardManager() ?: return
        if (!keyguard.isKeyguardLocked) {
            scheduleShow()
            return
        }
        polling = true
        handler.removeCallbacks(pollRunnable)
        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS)
    }

    private fun scheduleShow() {
        handler.removeCallbacks(showRunnable)
        handler.postDelayed(showRunnable, SHOW_DELAY_MS)
    }

    private val showRunnable = Runnable {
        val keyguard = keyguardManager() ?: return@Runnable
        if (keyguard.isKeyguardLocked) return@Runnable
        val now = System.currentTimeMillis()
        if (now - lastShowAt < MIN_INTERVAL_MS) return@Runnable
        lastShowAt = now
        onUnlock()
    }

    private fun stopPolling() {
        polling = false
        handler.removeCallbacks(pollRunnable)
    }

    private fun keyguardManager(): KeyguardManager? =
        context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

    companion object {
        private const val SHOW_DELAY_MS = 200L
        private const val POLL_INTERVAL_MS = 200L
        private const val MIN_INTERVAL_MS = 1500L
    }
}
