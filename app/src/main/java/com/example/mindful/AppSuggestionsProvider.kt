package com.example.mindful

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

object AppSuggestionsProvider {

    const val CACHE_TTL_MS = 6L * 60L * 60L * 1000L

    private val worker = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun refreshIfStale(context: Context) {
        val appContext = context.applicationContext
        worker.execute {
            if (!UsageAccess.hasUsageAccess(appContext)) return@execute
            if (!MindfulPrefs.isSuggestionsCacheStale(appContext)) return@execute
            refreshCache(appContext)
        }
    }

    fun loadSuggestions(
        context: Context,
        onResult: (List<AppSuggestion>) -> Unit,
    ) {
        val appContext = context.applicationContext
        worker.execute {
            val suggestions = if (!UsageAccess.hasUsageAccess(appContext)) {
                emptyList()
            } else {
                if (MindfulPrefs.isSuggestionsCacheStale(appContext)) {
                    refreshCache(appContext)
                } else {
                    MindfulPrefs.getCachedSuggestions(appContext)
                }
            }
            mainHandler.post { onResult(suggestions) }
        }
    }

    private fun refreshCache(context: Context): List<AppSuggestion> {
        val suggestions = AppUsageRepository(context).loadHybridSuggestions(limit = 3)
        MindfulPrefs.saveCachedSuggestions(context, suggestions)
        return suggestions
    }
}
