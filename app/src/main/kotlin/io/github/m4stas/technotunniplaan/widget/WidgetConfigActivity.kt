package io.github.m4stas.technotunniplaan.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.github.m4stas.technotunniplaan.App
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.Schedule
import io.github.m4stas.technotunniplaan.store.Settings
import io.github.m4stas.technotunniplaan.ui.browse.*
import io.github.m4stas.technotunniplaan.ui.theme.*
import java.io.IOException
import kotlinx.coroutines.launch

class WidgetConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, result)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }
        val container = (application as App).container
        val browse = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = BrowseViewModel(container.repository, container.settings) as T
        })[BrowseViewModel::class.java]
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity).getGlanceIdBy(widgetId)
            val initial = try { getAppWidgetState(this@WidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId).toWidgetConfig() }
                catch (_: IOException) { WidgetConfig() }
            try { container.repository.ensureLoaded() } catch (_: IOException) { }
            setContent {
                val settings by container.settings.settings.collectAsState(initial = Settings())
                val state by browse.state.collectAsState()
                val weeks by container.repository.weeks.collectAsState()
                var config by rememberSaveableConfig(initial)
                var saving by remember { mutableStateOf(false) }
                AppTheme(settings.theme) {
                    val p = LocalPalette.current
                    Surface(color = p.background) {
                        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.widget_name), color = p.text, style = MaterialTheme.typography.titleLarge)
                            FilterChip(selected = config.entity == null, onClick = { config = config.copy(entity = null) },
                                label = { Text(stringResource(R.string.settings_home)) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = p.surface, labelColor = p.text, selectedContainerColor = p.highlight, selectedLabelColor = p.accent))
                            config.entity?.let { ref -> Text(Schedule(weeks ?: emptyList()).find(ref)?.name ?: ref.key, color = p.text) }
                            Text(stringResource(R.string.widget_count), color = p.textSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..5).forEach { count ->
                                    FilterChip(selected = config.count == count, onClick = { config = config.copy(count = count) }, label = { Text(count.toString()) },
                                        colors = FilterChipDefaults.filterChipColors(containerColor = p.surface, labelColor = p.text, selectedContainerColor = p.highlight, selectedLabelColor = p.accent))
                                }
                            }
                            Box(Modifier.weight(1f)) { BrowseScreen(state, browse::setTab, browse::setQuery) { config = config.copy(entity = it) } }
                            Button(enabled = !saving, shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = p.accent, contentColor = p.onAccent),
                                modifier = Modifier.fillMaxWidth(), onClick = {
                                    saving = true
                                    lifecycleScope.launch {
                                        try {
                                            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { it.put(config) }
                                            NextLessonsWidget().update(this@WidgetConfigActivity, glanceId)
                                            WidgetUpdater.refresh(this@WidgetConfigActivity)
                                            setResult(RESULT_OK, result)
                                            finish()
                                        } catch (_: IOException) { saving = false }
                                    }
                                }) { Text(stringResource(R.string.save)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberSaveableConfig(initial: WidgetConfig): MutableState<WidgetConfig> =
    androidx.compose.runtime.saveable.rememberSaveable(stateSaver = androidx.compose.runtime.saveable.Saver<WidgetConfig, List<String>>(
        save = { listOf(it.entity?.kind?.name.orEmpty(), it.entity?.key.orEmpty(), it.count.toString()) },
        restore = { values ->
            val kind = io.github.m4stas.technotunniplaan.schedule.EntityKind.entries.find { it.name == values[0] }
            WidgetConfig(kind?.let { io.github.m4stas.technotunniplaan.schedule.EntityRef(it, values[1]) }, values[2].toInt())
        }
    )) { mutableStateOf(initial) }
