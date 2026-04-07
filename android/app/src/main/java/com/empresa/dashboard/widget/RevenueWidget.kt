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
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
        val (period, from, to) = WidgetPrefs.readPeriod(context, appWidgetId)
        val theme = WidgetPrefs.readTheme(context, appWidgetId)

        // Tentar buscar dados frescos
        val revenue = WidgetApi.fetchRevenue(period, from, to)

        val total: String
        val updatedAt: String

        if (revenue != null) {
            total = CurrencyFormatter.format(revenue.total)
            updatedAt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())
            WidgetPrefs.saveData(context, appWidgetId, total, revenue.label, updatedAt)
        } else {
            val cached = WidgetPrefs.readData(context, appWidgetId)
            total = cached.total ?: "R$ —"
            updatedAt = cached.updatedAt ?: ""
        }

        provideContent {
            val c = if (theme == "blue") blueColors() else darkColors()
            WidgetLayout(context, total, updatedAt, period, appWidgetId, c)
        }
    }

    private data class WColors(
        val bg: Color,
        val chipActive: Color,
        val chipInactive: Color,
        val textOnActive: Color,
        val textOnInactive: Color,
        val textMain: Color,
        val textMuted: Color,
    )

    private fun darkColors() = WColors(
        bg = Color(0xFF0A0A0A),
        chipActive = Color.White,
        chipInactive = Color(0xFF1E1E1E),
        textOnActive = Color.Black,
        textOnInactive = Color(0xAAFFFFFF),
        textMain = Color.White,
        textMuted = Color(0x66FFFFFF),
    )

    private fun blueColors() = WColors(
        bg = Color(0xFF1E3A8A),
        chipActive = Color.White,
        chipInactive = Color(0xFF2D4EA0),
        textOnActive = Color(0xFF1E3A8A),
        textOnInactive = Color(0xCCFFFFFF),
        textMain = Color.White,
        textMuted = Color(0x88FFFFFF),
    )

    @Composable
    private fun WidgetLayout(
        context: Context,
        total: String,
        updatedAt: String,
        period: String,
        widgetId: Int,
        c: WColors,
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(ColorProvider(c.bg))
                    .padding(24.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                // Logo
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.escalada_mark),
                        contentDescription = "Escalada",
                        modifier = GlanceModifier.size(26.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(c.textMain)),
                    )
                    Spacer(GlanceModifier.width(10.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(
                            color = ColorProvider(c.textMain.copy(alpha = 0.7f)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(GlanceModifier.height(20.dp))

                // Valor grande
                Text(
                    total,
                    style = TextStyle(
                        color = ColorProvider(c.textMain),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(GlanceModifier.height(8.dp))

                if (updatedAt.isNotEmpty()) {
                    Text(
                        "Atualizado $updatedAt",
                        style = TextStyle(
                            color = ColorProvider(c.textMuted),
                            fontSize = 11.sp,
                        ),
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                // Botões de período — grandes
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PeriodChip(context, "Hoje", "today", period, widgetId, c)
                    Spacer(GlanceModifier.width(8.dp))
                    PeriodChip(context, "Mês", "this-month", period, widgetId, c)
                    Spacer(GlanceModifier.width(8.dp))
                    PeriodChip(context, "30D", "last-30-days", period, widgetId, c)
                }
            }
        }
    }

    @Composable
    private fun PeriodChip(
        context: Context,
        label: String,
        periodKey: String,
        currentPeriod: String,
        widgetId: Int,
        c: WColors,
    ) {
        val isActive = currentPeriod == periodKey
        val intent = ChangePeriodReceiver.createIntent(context, widgetId, periodKey)

        Box(
            modifier = GlanceModifier
                .cornerRadius(14.dp)
                .background(ColorProvider(if (isActive) c.chipActive else c.chipInactive))
                .clickable(actionSendBroadcast(intent))
                .padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = TextStyle(
                    color = ColorProvider(if (isActive) c.textOnActive else c.textOnInactive),
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                ),
            )
        }
    }
}
