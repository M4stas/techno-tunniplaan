package io.github.m4stas.technotunniplaan.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import io.github.m4stas.technotunniplaan.App
import io.github.m4stas.technotunniplaan.schedule.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime

object WidgetUpdater {
    private val mutex = Mutex()
    suspend fun refresh(context: Context) = mutex.withLock {
        val container = (context.applicationContext as App).container
        val ids = GlanceAppWidgetManager(context).getGlanceIds(NextLessonsWidget::class.java)
        ids.forEach { id -> updateAppWidgetState(context, id) { it.markRedraw(container.clock.millis()) } }
        NextLessonsWidget().updateAll(context)
        container.repository.ensureLoaded()
        val settings = container.settings.settings.first()
        val schedule = Schedule(container.repository.weeks.value ?: emptyList())
        val now = LocalDateTime.now(container.clock)
        val next = ids.mapNotNull { id ->
            val config = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetConfig()
            config.resolve(settings)?.let { nextRedrawAt(schedule.nextLessons(it, now, config.count), now) }
        }.minOrNull()
        val alarm = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(context, 0, Intent(context, WidgetAlarmReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        if (next == null) alarm.cancel(intent)
        else alarm.setWindow(AlarmManager.RTC, next.atZone(SCHOOL_ZONE).toInstant().toEpochMilli(), 300_000, intent)
    }
}
