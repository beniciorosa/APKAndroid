package com.empresa.dashboard.widget

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChangePeriodReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val period = intent.getStringExtra("period") ?: return
        val widgetId = intent.getIntExtra("widgetId", -1)
        if (widgetId == -1) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Salvar novo período
                WidgetPrefs.saveConfig(context, widgetId, period, null, null)
                // 2. Marcar como "carregando" pra feedback imediato
                WidgetPrefs.saveData(context, widgetId, "Carregando...", periodLabel(period), "")
                // 3. Atualizar widget imediatamente (mostra "Carregando...")
                RevenueWidget().updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }

    private fun periodLabel(period: String) = when (period) {
        "today" -> "Hoje"
        "this-month" -> "Este mês"
        "last-30-days" -> "Últimos 30 dias"
        else -> period
    }

    companion object {
        fun createIntent(context: Context, widgetId: Int, period: String): Intent {
            return Intent(context, ChangePeriodReceiver::class.java).apply {
                component = ComponentName(context, ChangePeriodReceiver::class.java)
                action = "CHANGE_PERIOD_${widgetId}_${period}"
                putExtra("period", period)
                putExtra("widgetId", widgetId)
            }
        }
    }
}
