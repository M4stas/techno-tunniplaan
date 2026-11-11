package io.github.m4stas.technotunniplaan.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import java.io.IOException
import kotlinx.coroutines.*

class NextLessonsReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = NextLessonsWidget()
    // Glance's onUpdate calls goAsync itself, and goAsync works once per broadcast. refresh redraws every widget through updateAll.
    @SuppressLint("MissingSuperCall")
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { WidgetUpdater.refresh(context.applicationContext) } catch (_: IOException) { }
            finally { pending.finish() }
        }
    }
}
