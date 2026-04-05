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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.empresa.dashboard.MainActivity
import com.empresa.dashboard.R

class RevenueWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val data = WidgetPrefs.readData(context, appWidgetId)

        provideContent {
            WidgetContent(
                total = data.total ?: "—",
                label = data.label ?: "Este mês",
                updatedAt = data.updatedAt,
            )
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
                // Header: logo Escalada
                Image(
                    provider = ImageProvider(R.drawable.escalada_wordmark),
                    contentDescription = "ESCALADA",
                    modifier = GlanceModifier.height(14.dp),
                    colorFilter = androidx.glance.ColorFilter.tint(ColorProvider(Color.White)),
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    label.uppercase(),
                    style = TextStyle(
                        color = ColorProvider(Color(0x99FFFFFF)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    total,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                updatedAt?.let {
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        it,
                        style = TextStyle(
                            color = ColorProvider(Color(0x66FFFFFF)),
                            fontSize = 9.sp,
                        ),
                    )
                }
            }
        }
    }
}
