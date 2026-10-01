package com.example.mindful

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
