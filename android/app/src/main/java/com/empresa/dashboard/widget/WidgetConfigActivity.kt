package com.empresa.dashboard.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                ConfigScreen(onSave = { theme -> saveAndFinish(theme) })
            }
        }
    }

    private fun saveAndFinish(theme: String) {
        lifecycleScope.launch {
            WidgetPrefs.saveConfig(
                ctx = this@WidgetConfigActivity,
                widgetId = appWidgetId,
                period = "last-30-days",
                from = null,
                to = null,
            )
            WidgetPrefs.saveTheme(this@WidgetConfigActivity, appWidgetId, theme)
            WidgetUpdateWorker.enqueueOneTime(this@WidgetConfigActivity)

            val result = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}

@Composable
private fun ConfigScreen(onSave: (String) -> Unit) {
    var selectedTheme by remember { mutableStateOf("dark") }

    Scaffold(
        containerColor = Color(0xFF0A0A0A),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                "CONFIGURAR WIDGET",
                color = Color(0xFF737373),
                fontSize = 10.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                "Escolha o visual",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(8.dp))

            // Dark theme option
            ThemePreview(
                label = "Escuro",
                isSelected = selectedTheme == "dark",
                bgStart = Color(0xFF0A0A0A),
                bgEnd = Color(0xFF1A1A1A),
                chipColor = Color.White,
                textColor = Color.White,
                onClick = { selectedTheme = "dark" },
            )

            // Blue theme option
            ThemePreview(
                label = "Azul",
                isSelected = selectedTheme == "blue",
                bgStart = Color(0xFF1E3A8A),
                bgEnd = Color(0xFF3B82F6),
                chipColor = Color.White,
                textColor = Color.White,
                onClick = { selectedTheme = "blue" },
            )

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { onSave(selectedTheme) }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Adicionar widget",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ThemePreview(
    label: String,
    isSelected: Boolean,
    bgStart: Color,
    bgEnd: Color,
    chipColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    val borderMod = if (isSelected) {
        Modifier.border(2.dp, Color.White, RoundedCornerShape(20.dp))
    } else {
        Modifier.border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderMod)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(bgStart, bgEnd)))
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Column {
            Text(
                label.uppercase(),
                color = textColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "R$ 1.777.656,86",
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniChip("Hoje", false, chipColor, textColor, bgEnd)
                MiniChip("Mês", false, chipColor, textColor, bgEnd)
                MiniChip("30D", true, chipColor, textColor, bgEnd)
            }
        }
    }
}

@Composable
private fun MiniChip(label: String, active: Boolean, chipColor: Color, textColor: Color, bgEnd: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) chipColor else bgEnd.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            label,
            color = if (active) Color.Black else textColor.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
