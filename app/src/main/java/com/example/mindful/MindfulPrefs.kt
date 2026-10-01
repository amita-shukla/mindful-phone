package com.example.mindful

import android.content.Context

object MindfulPrefs {
    private const val PREFS = "mindful_prefs"
    private const val KEY_MONITORING = "monitoring_enabled"

    fun isMonitoringEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MONITORING, false)

    fun setMonitoringEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MONITORING, enabled)
            .apply()
    }
}
