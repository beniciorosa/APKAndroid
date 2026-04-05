package com.empresa.dashboard.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.lifecycleScope
import com.empresa.dashboard.ui.model.Period
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Resultado padrão: cancelado
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
                ConfigScreen(
                    onSave = { period, from, to ->
                        saveAndFinish(period, from, to)
                    }
                )
            }
        }
    }

    private fun saveAndFinish(period: Period, from: String?, to: String?) {
        lifecycleScope.launch {
            WidgetPrefs.saveConfig(
                ctx = this@WidgetConfigActivity,
                widgetId = appWidgetId,
                period = period.apiKey,
                from = from,
                to = to,
            )
            WidgetUpdateWorker.enqueueOneTime(this@WidgetConfigActivity)

            val result = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    onSave: (Period, String?, String?) -> Unit,
) {
    var selected by remember { mutableStateOf(Period.THIS_MONTH) }
    var showDatePicker by remember { mutableStateOf(false) }
    var customFrom by remember { mutableStateOf<String?>(null) }
    var customTo by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurar Widget") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Escolha o período", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Period.values().forEach { period ->
                val isSelected = selected == period
                Card(
                    onClick = {
                        selected = period
                        if (period == Period.CUSTOM) showDatePicker = true
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = isSelected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(period.label, fontWeight = FontWeight.Medium)
                            if (period == Period.CUSTOM && customFrom != null && customTo != null) {
                                Text(
                                    "$customFrom → $customTo",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    if (selected == Period.CUSTOM && (customFrom == null || customTo == null)) {
                        showDatePicker = true
                    } else {
                        onSave(selected, customFrom, customTo)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Salvar e adicionar widget")
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val from = state.selectedStartDateMillis
                        val to = state.selectedEndDateMillis
                        if (from != null && to != null) {
                            customFrom = toIsoDate(from)
                            customTo = toIsoDate(to)
                            selected = Period.CUSTOM
                        }
                        showDatePicker = false
                    },
                    enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                ) { Text("Aplicar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) {
            DateRangePicker(state = state, modifier = Modifier.height(500.dp))
        }
    }
}

private fun toIsoDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}
