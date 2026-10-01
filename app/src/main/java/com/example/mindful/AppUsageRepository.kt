package com.example.mindful

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo

class AppUsageRepository(private val context: Context) {

    private val packageManager = context.packageManager

    fun loadHybridSuggestions(limit: Int = 3): List<AppSuggestion> {
        if (!UsageAccess.hasUsageAccess(context)) return emptyList()

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - USAGE_WINDOW_MS
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end,
        )?.filter { isSuggestibleApp(it) }.orEmpty()

        if (stats.isEmpty()) return emptyList()

        val byRecent = stats.sortedByDescending { it.lastTimeUsed }
        val byFrequency = stats.sortedByDescending { it.totalTimeInForeground }

        val orderedPackages = LinkedHashSet<String>()
        for (stat in byRecent) {
            if (orderedPackages.size >= 2) break
            orderedPackages.add(stat.packageName)
        }
        for (stat in byFrequency) {
            if (orderedPackages.size >= limit) break
            orderedPackages.add(stat.packageName)
        }

        return orderedPackages
            .take(limit)
            .mapNotNull { packageName -> toSuggestion(packageName) }
    }

    private fun isSuggestibleApp(stat: UsageStats): Boolean {
        if (stat.packageName == context.packageName) return false
        if (stat.lastTimeUsed <= 0 && stat.totalTimeInForeground <= 0) return false
        return isUserLaunchableApp(stat.packageName)
    }

    private fun isUserLaunchableApp(packageName: String): Boolean {
        val launchIntent: Intent? = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent == null) return false
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            val isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isUpdatedSystem = info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            !isSystem || isUpdatedSystem
        } catch (_: Exception) {
            false
        }
    }

    private fun toSuggestion(packageName: String): AppSuggestion? {
        return try {
            val label = packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0),
            ).toString()
            AppSuggestion(packageName = packageName, label = label)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val USAGE_WINDOW_MS = 7L * 24L * 60L * 60L * 1000L
    }
}
