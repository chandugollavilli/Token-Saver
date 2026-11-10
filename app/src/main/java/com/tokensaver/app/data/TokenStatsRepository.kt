package com.tokensaver.app.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class StatsSnapshot(
    val totalSavedTokens: Long = 0,
    val dailySavedTokens: Long = 0,
    val byApp: Map<String, Long> = emptyMap()
)

class TokenStatsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("token_saver", Context.MODE_PRIVATE)
    private val mutableStats = MutableStateFlow(readStats())
    val stats: StateFlow<StatsSnapshot> = mutableStats

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        mutableStats.value = readStats()
    }

    init { preferences.registerOnSharedPreferenceChangeListener(listener) }

    fun isEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, false)
    fun setEnabled(enabled: Boolean) { preferences.edit().putBoolean(KEY_ENABLED, enabled).apply() }

    fun whitelistedPackages(): Set<String> =
        preferences.getStringSet(KEY_WHITELIST, emptySet())?.toSet()
            ?: emptySet()

    fun setWhitelisted(packageName: String, selected: Boolean) {
        val current = whitelistedPackages().toMutableSet()
        if (selected) current.add(packageName) else current.remove(packageName)
        preferences.edit().putStringSet(KEY_WHITELIST, current).apply()
    }

    fun recordSavings(packageName: String, amount: Int) {
        if (amount <= 0) return
        val today = LocalDate.now().toString()
        val previousDay = preferences.getString(KEY_DAY, null)
        val daily = if (previousDay == today) preferences.getLong(KEY_DAILY, 0) else 0
        val key = "$KEY_APP_PREFIX$packageName"
        preferences.edit()
            .putLong(KEY_TOTAL, preferences.getLong(KEY_TOTAL, 0) + amount)
            .putString(KEY_DAY, today)
            .putLong(KEY_DAILY, daily + amount)
            .putLong(key, preferences.getLong(key, 0) + amount)
            .apply()
    }

    private fun readStats(): StatsSnapshot {
        val today = LocalDate.now().toString()
        val byApp = AppCatalog.apps.mapNotNull { app ->
            val value = preferences.getLong("$KEY_APP_PREFIX${app.packageName}", 0)
            if (value > 0) app.packageName to value else null
        }.toMap()
        return StatsSnapshot(
            totalSavedTokens = preferences.getLong(KEY_TOTAL, 0),
            dailySavedTokens = if (preferences.getString(KEY_DAY, null) == today) preferences.getLong(KEY_DAILY, 0) else 0,
            byApp = byApp
        )
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_WHITELIST = "whitelist"
        const val KEY_TOTAL = "total_saved"
        const val KEY_DAY = "stats_day"
        const val KEY_DAILY = "daily_saved"
        const val KEY_APP_PREFIX = "app_saved_"
    }
}
