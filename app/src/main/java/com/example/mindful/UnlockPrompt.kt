package com.example.mindful

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
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
import java.util.concurrent.atomic.AtomicBoolean

object UnlockPrompt {

    private const val TAG = "UnlockPrompt"

    private val mainHandler = Handler(Looper.getMainLooper())
    private val showing = AtomicBoolean(false)

    fun show(context: Context) {
        mainHandler.post {
            if (!showing.compareAndSet(false, true)) return@post
            try {
                launchActivity(context.applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "Prompt activity failed", e)
                showing.set(false)
            }
        }
    }

    fun dismiss() {
        mainHandler.post {
            showing.set(false)
        }
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
