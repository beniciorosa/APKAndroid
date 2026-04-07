package com.empresa.dashboard.widget

import com.empresa.dashboard.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WidgetRevenue(
    val total: Double,
    val label: String,
    val dealCount: Int,
)

// Chamada HTTP direta — sem Retrofit/Hilt, funciona em qualquer contexto
object WidgetApi {
    suspend fun fetchRevenue(
        period: String,
        from: String? = null,
        to: String? = null,
    ): WidgetRevenue? = withContext(Dispatchers.IO) {
        try {
            val base = BuildConfig.BASE_URL + "api/revenue"
            val params = buildString {
                append("?period=$period")
                if (from != null) append("&from=$from")
                if (to != null) append("&to=$to")
            }
            val url = URL(base + params)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(body)
                val periodObj = json.optJSONObject("period")
                WidgetRevenue(
                    total = json.optDouble("total", 0.0),
                    label = periodObj?.optString("label", period) ?: period,
                    dealCount = json.optInt("dealCount", 0),
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
