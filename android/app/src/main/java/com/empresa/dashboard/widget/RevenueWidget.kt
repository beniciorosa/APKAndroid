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
                // Header: logo + marca
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_escalada_mark_white),
                        contentDescription = null,
                        modifier = GlanceModifier.size(16.dp),
                    )
                    Spacer(GlanceModifier.size(8.dp))
                    Text(
                        "ESCALADA",
                        style = TextStyle(
                            color = ColorProvider(Color(0x99FFFFFF)),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                Spacer(GlanceModifier.height(6.dp))
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
