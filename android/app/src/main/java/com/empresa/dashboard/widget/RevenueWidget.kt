package com.empresa.dashboard.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.empresa.dashboard.MainActivity
import com.empresa.dashboard.R
import com.empresa.dashboard.ui.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RevenueWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val theme = WidgetPrefs.readTheme(context, appWidgetId)

        // Buscar os 3 períodos
        val today = WidgetApi.fetchRevenue("today")
        val month = WidgetApi.fetchRevenue("this-month")
        val days30 = WidgetApi.fetchRevenue("last-30-days")

        val todayVal = today?.let { CurrencyFormatter.format(it.total) } ?: "—"
        val monthVal = month?.let { CurrencyFormatter.format(it.total) } ?: "—"
        val days30Val = days30?.let { CurrencyFormatter.format(it.total) } ?: "—"
        val updatedAt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())

        // Salvar cache
        WidgetPrefs.saveData(context, appWidgetId, days30Val, "Últimos 30 dias", updatedAt)

        provideContent {
            val c = if (theme == "blue") blueColors() else darkColors()
            WidgetLayout(todayVal, monthVal, days30Val, updatedAt, c)
        }
    }

    private data class WColors(
        val bg: Color,
        val card: Color,
        val textMain: Color,
        val textLabel: Color,
        val textMuted: Color,
    )

    private fun darkColors() = WColors(
        bg = Color(0xFF0A0A0A),
        card = Color(0xFF1A1A1A),
        textMain = Color.White,
        textLabel = Color(0xAAFFFFFF),
        textMuted = Color(0x66FFFFFF),
    )

    private fun blueColors() = WColors(
        bg = Color(0xFF1E3A8A),
        card = Color(0xFF264FAD),
        textMain = Color.White,
        textLabel = Color(0xCCFFFFFF),
        textMuted = Color(0x88FFFFFF),
    )

    @Composable
    private fun WidgetLayout(
        todayVal: String,
        monthVal: String,
        days30Val: String,
        updatedAt: String,
        c: WColors,
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(ColorProvider(c.bg))
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(16.dp),
            ) {
                // Header: logo + timestamp
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.escalada_mark),
                        contentDescription = null,
                        modifier = GlanceModifier.size(18.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(c.textLabel)),
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(
                            color = ColorProvider(c.textLabel),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        updatedAt,
                        style = TextStyle(
                            color = ColorProvider(c.textMuted),
                            fontSize = 9.sp,
                        ),
                    )
                }

                Spacer(GlanceModifier.height(14.dp))

                // HOJE — valor grande, destaque
                Text(
                    "HOJE",
                    style = TextStyle(
                        color = ColorProvider(c.textLabel),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    todayVal,
                    style = TextStyle(
                        color = ColorProvider(c.textMain),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(GlanceModifier.defaultWeight())

                // Mês e 30D lado a lado
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    // Este mês
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            "ESTE MÊS",
                            style = TextStyle(
                                color = ColorProvider(c.textMuted),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            monthVal,
                            style = TextStyle(
                                color = ColorProvider(c.textMain),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    // 30 dias
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            "30 DIAS",
                            style = TextStyle(
                                color = ColorProvider(c.textMuted),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            days30Val,
                            style = TextStyle(
                                color = ColorProvider(c.textMain),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }
        }
    }
}
