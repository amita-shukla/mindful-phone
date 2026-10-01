package com.example.mindful

import android.content.Context

object MindfulPrefs {
    private const val PREFS = "mindful_prefs"
    private const val KEY_MONITORING = "monitoring_enabled"
    private const val KEY_SUGGESTIONS_CACHE = "suggestions_cache"
    private const val KEY_SUGGESTIONS_CACHE_AT = "suggestions_cache_at"
    private const val CACHE_ENTRY_SEPARATOR = "\u001F"
    private const val CACHE_FIELD_SEPARATOR = "\u001E"

    fun isMonitoringEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_MONITORING, false)

    fun setMonitoringEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_MONITORING, enabled)
            .apply()
    }

    fun isSuggestionsCacheStale(context: Context): Boolean {
        val cachedAt = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_SUGGESTIONS_CACHE_AT, 0L)
        if (cachedAt == 0L) return true
        return System.currentTimeMillis() - cachedAt >= AppSuggestionsProvider.CACHE_TTL_MS
    }

    fun getCachedSuggestions(context: Context): List<AppSuggestion> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SUGGESTIONS_CACHE, null)
            ?: return emptyList()
        if (raw.isEmpty()) return emptyList()
        return raw.split(CACHE_ENTRY_SEPARATOR).mapNotNull { entry ->
            val parts = entry.split(CACHE_FIELD_SEPARATOR)
            if (parts.size != 2) return@mapNotNull null
            AppSuggestion(packageName = parts[0], label = parts[1])
        }
    }

    fun saveCachedSuggestions(context: Context, suggestions: List<AppSuggestion>) {
        val serialized = suggestions.joinToString(CACHE_ENTRY_SEPARATOR) { suggestion ->
            "${suggestion.packageName}$CACHE_FIELD_SEPARATOR${suggestion.label}"
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SUGGESTIONS_CACHE, serialized)
            .putLong(KEY_SUGGESTIONS_CACHE_AT, System.currentTimeMillis())
            .apply()
    }
}
