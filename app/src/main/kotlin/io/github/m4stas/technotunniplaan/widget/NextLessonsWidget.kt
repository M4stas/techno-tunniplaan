package io.github.m4stas.technotunniplaan.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.*
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import io.github.m4stas.technotunniplaan.App
import io.github.m4stas.technotunniplaan.MainActivity
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.Settings
import java.io.IOException
import java.time.LocalDateTime

private val kindKey = stringPreferencesKey("entity_kind")
private val entityKey = stringPreferencesKey("entity_key")
private val countKey = intPreferencesKey("count")
private val redrawKey = longPreferencesKey("redraw_at")
fun Preferences.toWidgetConfig(): WidgetConfig {
    val kind = EntityKind.entries.find { it.name == this[kindKey] }
    val key = this[entityKey]?.takeIf { it.isNotBlank() }
    return WidgetConfig(if (kind != null && key != null) EntityRef(kind, key) else null, (this[countKey] ?: 1).coerceIn(1, 5))
}
/** A running Glance session recomposes only when its state changes, so every scheduled redraw stores a new value here. */
fun MutablePreferences.markRedraw(at: Long) { this[redrawKey] = at }
fun MutablePreferences.put(config: WidgetConfig) {
    this[countKey] = config.count.coerceIn(1, 5)
    val entity = config.entity
    if (entity == null) { remove(kindKey); remove(entityKey) }
    else { this[kindKey] = entity.kind.name; this[entityKey] = entity.key }
}

class NextLessonsWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as App).container
        try { container.repository.ensureLoaded() } catch (_: IOException) { }
        provideContent {
            val settings by container.settings.settings.collectAsState(initial = Settings())
            val weeks by container.repository.weeks.collectAsState()
            val config = currentState<Preferences>().toWidgetConfig()
            val entity = config.resolve(settings)
            val colors = widgetColors(settings.theme)
            val content = widgetContent(weeks?.let { Schedule(it) }, entity,
                LocalDateTime.now(container.clock), config.count)
            Column(GlanceModifier.fillMaxSize().background(colors.widgetBackground).cornerRadius(24.dp)
                .clickable(actionStartActivity(MainActivity.intentFor(context, entity)))
                .padding(horizontal = 8.dp, vertical = 8.dp)) {
                when (content) {
                    WidgetContent.NoData -> Text(context.getString(R.string.widget_open_app), style = TextStyle(color = colors.text))
                    WidgetContent.NoLessons -> Text(context.getString(R.string.widget_no_lessons), style = TextStyle(color = colors.text))
                    is WidgetContent.Lessons -> content.lessons.forEach { lesson ->
                        Text(listOf(lesson.text.time, lesson.text.title, lesson.text.place, lesson.text.who)
                            .filter { it.isNotBlank() }.joinToString(" · "),
                            style = TextStyle(color = colors.text), maxLines = 1)
                    }
                }
            }
        }
    }
}
