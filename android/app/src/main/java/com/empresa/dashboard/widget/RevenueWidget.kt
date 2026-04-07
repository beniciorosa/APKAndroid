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

class RevenueWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val (period, from, to) = WidgetPrefs.readPeriod(context, appWidgetId)

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
            WidgetContent(
                total = total,
                label = label,
                updatedAt = updatedAt,
                period = period,
                widgetId = appWidgetId,
            )
        }
    }

    @Composable
    private fun WidgetContent(
        total: String,
        label: String,
        updatedAt: String,
        period: String,
        widgetId: Int,
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(ColorProvider(Color(0xFF0A0A0A)))
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
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.White)),
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(
                            color = ColorProvider(Color(0xAAFFFFFF)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }

                Spacer(GlanceModifier.height(16.dp))

                // Valor grande
                Text(
                    total,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )

                Spacer(GlanceModifier.height(8.dp))

                // Timestamp
                if (updatedAt.isNotEmpty()) {
                    Text(
                        "Atualizado $updatedAt",
                        style = TextStyle(
                            color = ColorProvider(Color(0x66FFFFFF)),
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
                    PeriodButton("Hoje", "today", period == "today", widgetId)
                    Spacer(GlanceModifier.width(6.dp))
                    PeriodButton("Mês", "this-month", period == "this-month", widgetId)
                    Spacer(GlanceModifier.width(6.dp))
                    PeriodButton("30d", "last-30-days", period == "last-30-days", widgetId)
                }
            }
        }
    }

    @Composable
    private fun PeriodButton(label: String, periodKey: String, isActive: Boolean, widgetId: Int) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(12.dp)
                .background(
                    ColorProvider(
                        if (isActive) Color.White else Color(0xFF1E1E1E)
                    )
                )
                .clickable(
                    actionRunCallback<ChangePeriodAction>(
                        actionParametersOf(
                            ActionParameters.Key<Int>("widgetId") to widgetId,
                            ActionParameters.Key<String>("period") to periodKey,
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                style = TextStyle(
                    color = ColorProvider(if (isActive) Color.Black else Color(0xAAFFFFFF)),
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
        val widgetId = parameters[ActionParameters.Key<Int>("widgetId")] ?: return
        val period = parameters[ActionParameters.Key<String>("period")] ?: return
        WidgetPrefs.saveConfig(context, widgetId, period, null, null)
        RevenueWidget().updateAll(context)
    }
}
