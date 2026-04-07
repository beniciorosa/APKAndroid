package com.empresa.dashboard.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "widget_prefs")

object WidgetPrefs {
    private fun periodKey(widgetId: Int) = stringPreferencesKey("widget_${widgetId}_period")
    private fun fromKey(widgetId: Int) = stringPreferencesKey("widget_${widgetId}_from")
    private fun toKey(widgetId: Int) = stringPreferencesKey("widget_${widgetId}_to")
    private fun totalKey(widgetId: Int) = stringPreferencesKey("widget_${widgetId}_total")
    private fun labelKey(widgetId: Int) = stringPreferencesKey("widget_${widgetId}_label")
    private fun updatedKey(widgetId: Int) = stringPreferencesKey("widget_${widgetId}_updated")

    suspend fun saveConfig(ctx: Context, widgetId: Int, period: String, from: String?, to: String?) {
        ctx.dataStore.edit { prefs ->
            prefs[periodKey(widgetId)] = period
            if (from != null) prefs[fromKey(widgetId)] = from else prefs.remove(fromKey(widgetId))
            if (to != null) prefs[toKey(widgetId)] = to else prefs.remove(toKey(widgetId))
        }
    }

    suspend fun readPeriod(ctx: Context, widgetId: Int): Triple<String, String?, String?> {
        val prefs = ctx.dataStore.data.first()
        val period = prefs[periodKey(widgetId)] ?: "last-30-days"
        val from = prefs[fromKey(widgetId)]
        val to = prefs[toKey(widgetId)]
        return Triple(period, from, to)
    }

    suspend fun saveData(ctx: Context, widgetId: Int, total: String, label: String, updatedAt: String) {
        ctx.dataStore.edit { prefs ->
            prefs[totalKey(widgetId)] = total
            prefs[labelKey(widgetId)] = label
            prefs[updatedKey(widgetId)] = updatedAt
        }
    }

    suspend fun readData(ctx: Context, widgetId: Int): WidgetData {
        val prefs = ctx.dataStore.data.first()
        return WidgetData(
            total = prefs[totalKey(widgetId)],
            label = prefs[labelKey(widgetId)],
            updatedAt = prefs[updatedKey(widgetId)],
        )
    }

    suspend fun clear(ctx: Context, widgetId: Int) {
        ctx.dataStore.edit { prefs ->
            listOf(
                periodKey(widgetId), fromKey(widgetId), toKey(widgetId),
                totalKey(widgetId), labelKey(widgetId), updatedKey(widgetId)
            ).forEach { prefs.remove(it) }
        }
    }
}

data class WidgetData(
    val total: String?,
    val label: String?,
    val updatedAt: String?,
)
