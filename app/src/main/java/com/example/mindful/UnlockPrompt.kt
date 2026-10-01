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
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper

object UnlockPrompt {

    private const val TAG = "UnlockPrompt"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val showing = java.util.concurrent.atomic.AtomicBoolean(false)
    private var overlayView: View? = null
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
        root: View,
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
        loadSuggestions(root, motiveField, onDismiss)
        motiveField.post { motiveField.requestFocus() }
    }

    private fun loadSuggestions(root: View, motiveField: EditText, onDismiss: () -> Unit) {
        val container = root.findViewById<LinearLayout>(R.id.suggestionsContainer)
        val scroll = root.findViewById<HorizontalScrollView>(R.id.suggestionsScroll)
        val suggestionsTitle = root.findViewById<TextView>(R.id.suggestionsLabel)

        AppSuggestionsProvider.loadSuggestions(root.context) { suggestions ->
            if (suggestions.isEmpty()) return@loadSuggestions
            val inflater = LayoutInflater.from(root.context)
            val pm = root.context.packageManager
            container.removeAllViews()
            for (suggestion in suggestions) {
                val item = inflater.inflate(R.layout.item_app_suggestion, container, false)
                item.findViewById<TextView>(R.id.suggestionLabel).text = suggestion.label
                try {
                    val icon = pm.getApplicationIcon(suggestion.packageName)
                    item.findViewById<ImageView>(R.id.suggestionIcon).setImageDrawable(icon)
                } catch (_: Exception) {
                }
                item.setOnClickListener {
                    motiveField.setText(suggestion.label)
                    onDismiss()
                }
                container.addView(item)
            }
            suggestionsTitle.visibility = View.VISIBLE
            scroll.visibility = View.VISIBLE
        }
    }
}
