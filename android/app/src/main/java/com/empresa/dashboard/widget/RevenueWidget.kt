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
        val (period, from, to) = WidgetPrefs.readPeriod(context, appWidgetId)

        // Buscar dados frescos direto da API
        val revenue = WidgetApi.fetchRevenue(period, from, to)

        val total: String
        val label: String
        val updatedAt: String?

        if (revenue != null) {
            total = CurrencyFormatter.format(revenue.total)
            label = revenue.label
            updatedAt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())
            // Salvar pra cache offline
            WidgetPrefs.saveData(context, appWidgetId, total, label, updatedAt)
        } else {
            // Fallback: usa dados salvos anteriormente
            val cached = WidgetPrefs.readData(context, appWidgetId)
            total = cached.total ?: "—"
            label = cached.label ?: "Este mês"
            updatedAt = cached.updatedAt
        }

        provideContent {
            WidgetContent(total = total, label = label, updatedAt = updatedAt)
        }
    }

    @Composable
    private fun WidgetContent(total: String, label: String, updatedAt: String?) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(20.dp)
                    .background(ColorProvider(Color(0xFF000000)))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // Header: logo
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.escalada_mark),
                        contentDescription = "Escalada",
                        modifier = GlanceModifier.size(14.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.White)),
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(
                            color = ColorProvider(Color(0x99FFFFFF)),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                Spacer(GlanceModifier.height(8.dp))
                // Período
                Text(
                    label.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(Color(0x99FFFFFF)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(GlanceModifier.height(4.dp))
                // Valor
                Text(
                    total,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.height(6.dp))
                // Atualizado
                updatedAt?.let {
                    Text(
                        "Atualizado $it",
                        style = TextStyle(
                            color = ColorProvider(Color(0x66FFFFFF)),
                            fontSize = 8.sp,
                        ),
                    )
                }
            }
        }
    }
}
