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
import io.github.m4stas.technotunniplaan.store.subGroupFilter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.TextStyle as DayTextStyle

private val kindKey = stringPreferencesKey("entity_kind")
private val entityKey = stringPreferencesKey("entity_key")
private val countKey = intPreferencesKey("count")
fun Preferences.toWidgetConfig(): WidgetConfig {
    val kind = EntityKind.entries.find { it.name == this[kindKey] }
    val key = this[entityKey]?.takeIf { it.isNotBlank() }
    return WidgetConfig(if (kind != null && key != null) EntityRef(kind, key) else null, (this[countKey] ?: 1).coerceIn(1, 5))
}
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
            val content = widgetContent(weeks?.let { Schedule(it, settings.subGroupFilter()) }, entity,
                LocalDateTime.now(container.clock), minOf(config.count, rowsThatFit(LocalSize.current.height.value.toInt())))
            Column(GlanceModifier.fillMaxSize().background(colors.background).cornerRadius(24.dp)
                .clickable(actionStartActivity(MainActivity.intentFor(context, entity))).padding(8.dp)) {
                when (content) {
                    WidgetContent.NoData -> Text(context.getString(R.string.widget_open_app), style = TextStyle(color = colors.text))
                    WidgetContent.NoLessons -> Text(context.getString(R.string.widget_no_lessons), style = TextStyle(color = colors.text))
                    is WidgetContent.Lessons -> content.rows.forEach { row ->
                        Row(GlanceModifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(listOfNotNull(row.day?.getDisplayName(DayTextStyle.SHORT, context.resources.configuration.locales[0]), row.text.time).joinToString(" "),
                                modifier = GlanceModifier.width(116.dp), style = androidx.glance.text.TextStyle(color = colors.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold), maxLines = 2)
                            Column(GlanceModifier.defaultWeight()) {
                                Text(row.text.title, style = androidx.glance.text.TextStyle(color = colors.text, fontWeight = FontWeight.Bold), maxLines = 1)
                                Text(listOf(row.text.place, row.text.who).filter { it.isNotBlank() }.joinToString(" · "), style = androidx.glance.text.TextStyle(color = colors.secondary, fontSize = 12.sp), maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
