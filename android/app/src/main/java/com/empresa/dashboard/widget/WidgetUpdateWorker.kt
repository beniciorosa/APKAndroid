package com.empresa.dashboard.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.empresa.dashboard.data.RevenueRepository
import com.empresa.dashboard.ui.util.CurrencyFormatter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repo: RevenueRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(RevenueWidget::class.java)

        for (glanceId in ids) {
            val appWidgetId = manager.getAppWidgetId(glanceId)
            val (period, from, to) = WidgetPrefs.readPeriod(context, appWidgetId)
            val result = repo.getRevenue(period, from, to)
            result.onSuccess { resp ->
                WidgetPrefs.saveData(
                    ctx = context,
                    widgetId = appWidgetId,
                    total = CurrencyFormatter.format(resp.total),
                    label = resp.period.label,
                    updatedAt = formatNow(),
                )
            }
        }
        RevenueWidget().updateAll(context)
        return Result.success()
    }

    private fun formatNow(): String =
        SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR")).format(Date())

    companion object {
        const val WORK_NAME = "RevenueWidgetPeriodicUpdate"
        const val ONE_TIME_NAME = "RevenueWidgetOneTimeUpdate"

        fun enqueueOneTime(context: Context) {
            val work = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_NAME,
                androidx.work.ExistingWorkPolicy.REPLACE,
                work,
            )
        }
    }
}
