package com.empresa.dashboard.widget

import android.content.BroadcastReceiver
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
                WidgetPrefs.saveConfig(context, widgetId, period, null, null)
                RevenueWidget().updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION = "com.empresa.dashboard.CHANGE_PERIOD"
    }
}
