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
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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

private val PERIOD_KEY = ActionParameters.Key<String>("period")

class RevenueWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val (period, from, to) = WidgetPrefs.readPeriod(context, appWidgetId)
        val theme = WidgetPrefs.readTheme(context, appWidgetId)

        val revenue = WidgetApi.fetchRevenue(period, from, to)

        val total: String
        val label: String
        val updatedAt: String

        if (revenue != null) {
            total = CurrencyFormatter.format(revenue.total)
            label = revenue.label
            updatedAt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())
            WidgetPrefs.saveData(context, appWidgetId, total, label, updatedAt)
        } else {
            val cached = WidgetPrefs.readData(context, appWidgetId)
            total = cached.total ?: "R$ —"
            label = cached.label ?: "Últimos 30 dias"
            updatedAt = cached.updatedAt ?: ""
        }

        provideContent {
            if (theme == "blue") {
                BlueWidget(total, updatedAt, period)
            } else {
                DarkWidget(total, updatedAt, period)
            }
        }
    }

    @Composable
    private fun DarkWidget(total: String, updatedAt: String, period: String) {
        val bg = Color(0xFF0A0A0A)
        val chipActive = Color.White
        val chipInactive = Color(0xFF1E1E1E)
        val textOnActive = Color.Black
        val textOnInactive = Color(0xAAFFFFFF)
        val logoTint = Color.White

        WidgetLayout(total, updatedAt, period, bg, chipActive, chipInactive, textOnActive, textOnInactive, logoTint)
    }

    @Composable
    private fun BlueWidget(total: String, updatedAt: String, period: String) {
        val bg = Color(0xFF1E3A8A)
        val chipActive = Color.White
        val chipInactive = Color(0xFF2D4EA0)
        val textOnActive = Color(0xFF1E3A8A)
        val textOnInactive = Color(0xCCFFFFFF)
        val logoTint = Color.White

        WidgetLayout(total, updatedAt, period, bg, chipActive, chipInactive, textOnActive, textOnInactive, logoTint)
    }

    @Composable
    private fun WidgetLayout(
        total: String,
        updatedAt: String,
        period: String,
        bg: Color,
        chipActive: Color,
        chipInactive: Color,
        textOnActive: Color,
        textOnInactive: Color,
        logoTint: Color,
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(ColorProvider(bg))
                    .padding(20.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // Logo
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.escalada_mark),
                        contentDescription = "Escalada",
                        modifier = GlanceModifier.size(22.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(logoTint)),
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(
                            color = ColorProvider(logoTint.copy(alpha = 0.7f)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(GlanceModifier.height(16.dp))

                // Valor
                Text(
                    total,
                    style = TextStyle(
                        color = ColorProvider(logoTint),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(GlanceModifier.height(8.dp))

                if (updatedAt.isNotEmpty()) {
                    Text(
                        "Atualizado $updatedAt",
                        style = TextStyle(
                            color = ColorProvider(logoTint.copy(alpha = 0.4f)),
                            fontSize = 10.sp,
                        ),
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                // Botões de período
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PeriodChip("Hoje", "today", period, chipActive, chipInactive, textOnActive, textOnInactive)
                    Spacer(GlanceModifier.width(6.dp))
                    PeriodChip("Mês", "this-month", period, chipActive, chipInactive, textOnActive, textOnInactive)
                    Spacer(GlanceModifier.width(6.dp))
                    PeriodChip("30D", "last-30-days", period, chipActive, chipInactive, textOnActive, textOnInactive)
                }
            }
        }
    }

    @Composable
    private fun PeriodChip(
        label: String,
        periodKey: String,
        currentPeriod: String,
        chipActive: Color,
        chipInactive: Color,
        textOnActive: Color,
        textOnInactive: Color,
    ) {
        val isActive = currentPeriod == periodKey
        Box(
            modifier = GlanceModifier
                .cornerRadius(12.dp)
                .background(ColorProvider(if (isActive) chipActive else chipInactive))
                .clickable(
                    actionRunCallback<ChangePeriodAction>(
                        actionParametersOf(PERIOD_KEY to periodKey)
                    )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = TextStyle(
                    color = ColorProvider(if (isActive) textOnActive else textOnInactive),
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                ),
            )
        }
    }
}

class ChangePeriodAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val period = parameters[PERIOD_KEY] ?: return
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        WidgetPrefs.saveConfig(context, widgetId, period, null, null)
        RevenueWidget().updateAll(context)
    }
}
