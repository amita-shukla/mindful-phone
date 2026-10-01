package com.example.mindful

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.view.ContextThemeWrapper
import java.util.concurrent.atomic.AtomicBoolean

object UnlockPrompt {

    private const val TAG = "UnlockPrompt"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val showing = AtomicBoolean(false)
    private var overlayView: android.view.View? = null
    private var windowManager: WindowManager? = null

    fun show(context: Context) {
        mainHandler.post {
            if (!showing.compareAndSet(false, true)) return@post
            val appContext = context.applicationContext
            if (Settings.canDrawOverlays(appContext)) {
                try {
                    showOverlay(appContext)
                    return@post
                } catch (e: Exception) {
                    Log.e(TAG, "Overlay prompt failed, falling back to activity", e)
                    removeOverlay()
                }
            }
            try {
                launchActivity(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Activity prompt failed", e)
                showing.set(false)
            }
        }
    }

    fun dismiss() {
        mainHandler.post {
            removeOverlay()
            showing.set(false)
        }
    }

    private fun showOverlay(context: Context) {
        removeOverlay()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm
        val themedContext = ContextThemeWrapper(context, R.style.Theme_Mindful)
        val view = LayoutInflater.from(themedContext).inflate(R.layout.unlock_prompt, null)
        overlayView = view
        bindPrompt(view, onDismiss = {
            removeOverlay()
            showing.set(false)
        })

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT,
        ).apply {
            dimAmount = 0.6f
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        }
        wm.addView(view, params)
    }

    private fun removeOverlay() {
        val wm = windowManager
        val view = overlayView
        if (wm != null && view != null) {
            try {
                wm.removeView(view)
            } catch (_: IllegalArgumentException) {
            }
        }
        overlayView = null
        windowManager = null
    }

    private fun launchActivity(context: Context) {
        val intent = Intent(context, PopupActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
        }
        context.startActivity(intent)
    }

    fun bindPrompt(
        root: android.view.View,
        onDismiss: () -> Unit,
    ) {
        val motiveField = root.findViewById<EditText>(R.id.motiveInput)
        root.findViewById<Button>(R.id.continueButton).setOnClickListener {
            val motive = motiveField.text?.toString()?.trim().orEmpty()
            if (motive.isEmpty()) {
                motiveField.error = root.context.getString(R.string.motive_required)
                return@setOnClickListener
            }
            motiveField.error = null
            onDismiss()
        }
        root.findViewById<Button>(R.id.sosButton).setOnClickListener { onDismiss() }
        motiveField.post { motiveField.requestFocus() }
    }
}
