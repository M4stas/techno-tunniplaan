package io.github.m4stas.technotunniplaan.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.*
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
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
            val size = LocalSize.current
            val layout = widgetLayout(size.height.value.toInt())
            val narrow = isNarrowWidget(size.width.value.toInt())
            Box(GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_background_24),
                colorFilter = ColorFilter.tint(colors.widgetBackground))
                .clickable(actionStartActivity(MainActivity.intentFor(context, entity)))) {
                when (content) {
                    WidgetContent.NoData -> WidgetNoData(colors, layout == WidgetLayout.SINGLE)
                    WidgetContent.NoLessons -> WidgetEmpty(colors, layout == WidgetLayout.SINGLE)
                    is WidgetContent.Lessons -> when (layout) {
                        WidgetLayout.SINGLE -> WidgetSingle(content.lessons, colors, narrow)
                        WidgetLayout.NOW_NEXT -> WidgetNowNext(content.lessons, colors, narrow)
                        WidgetLayout.TIMELINE -> WidgetTimeline(content.entityName,
                            content.lessons.take(timelineRowsThatFit(size.height.value.toInt(), content.lessons)), colors, narrow)
                    }
                }
            }
        }
    }
}
