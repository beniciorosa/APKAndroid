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
        val today = WidgetApi.fetchRevenue("today")
        val month = WidgetApi.fetchRevenue("this-month")
        val days30 = WidgetApi.fetchRevenue("last-30-days")

        val todayVal = today?.let { CurrencyFormatter.format(it.total) } ?: "—"
        val monthVal = month?.let { CurrencyFormatter.format(it.total) } ?: "—"
        val days30Val = days30?.let { CurrencyFormatter.format(it.total) } ?: "—"
        val ts = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())

        provideContent {
            WidgetUI(todayVal, monthVal, days30Val, ts)
        }
    }

    @Composable
    private fun WidgetUI(today: String, month: String, days30: String, ts: String) {
        val bg = Color(0xFF1E3A8A)
        val white = Color.White
        val muted = Color(0x99FFFFFF)
        val sub = Color(0xCCFFFFFF)

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(24.dp)
                    .background(ColorProvider(bg))
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(18.dp),
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.escalada_mark),
                        contentDescription = null,
                        modifier = GlanceModifier.size(18.dp),
                        colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(sub)),
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(color = ColorProvider(sub), fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        ts,
                        style = TextStyle(color = ColorProvider(muted), fontSize = 9.sp),
                    )
                }

                Spacer(GlanceModifier.height(12.dp))

                // HOJE
                Text("HOJE", style = TextStyle(color = ColorProvider(muted), fontSize = 10.sp, fontWeight = FontWeight.Medium))
                Spacer(GlanceModifier.height(2.dp))
                Text(today, style = TextStyle(color = ColorProvider(white), fontSize = 30.sp, fontWeight = FontWeight.Bold))

                Spacer(GlanceModifier.defaultWeight())

                // ESTE MÊS + 30 DIAS — usar separador de texto
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    // Este Mês
                    Column {
                        Text("ESTE MÊS", style = TextStyle(color = ColorProvider(muted), fontSize = 9.sp, fontWeight = FontWeight.Medium))
                        Spacer(GlanceModifier.height(2.dp))
                        Text(month, style = TextStyle(color = ColorProvider(white), fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                    Spacer(GlanceModifier.width(24.dp))
                    // 30 Dias
                    Column {
                        Text("30 DIAS", style = TextStyle(color = ColorProvider(muted), fontSize = 9.sp, fontWeight = FontWeight.Medium))
                        Spacer(GlanceModifier.height(2.dp))
                        Text(days30, style = TextStyle(color = ColorProvider(white), fontSize = 16.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
