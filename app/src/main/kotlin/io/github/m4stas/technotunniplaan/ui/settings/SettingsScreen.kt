package io.github.m4stas.technotunniplaan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.github.m4stas.technotunniplaan.ui.components.Clearance
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable fun SettingsScreen(state: SettingsUiState, onChangeHome: () -> Unit, onChooseGroup: (Split, String?) -> Unit,
    onTheme: (ThemeMode) -> Unit, onLanguage: (String?) -> Unit, onSyncNow: () -> Unit, language: String? = null) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(stringResource(R.string.nav_settings), color = p.text, style = MaterialTheme.typography.headlineMedium)
        SettingsSection(stringResource(R.string.settings_home)) {
            TextButton(onClick = onChangeHome) { Text(state.homeTitle, color = p.accentText) }
        }
        if (state.splits.isNotEmpty()) SettingsSection(stringResource(R.string.settings_groups)) {
            state.splits.forEach { item ->
                Pills(listOf(null to stringResource(R.string.groups_all)) + item.split.groups.map { it to it }, item.chosen) { onChooseGroup(item.split, it) }
            }
        }
        SettingsSection(stringResource(R.string.settings_theme)) {
            Pills(listOf(ThemeMode.SYSTEM to stringResource(R.string.theme_system), ThemeMode.DARK to stringResource(R.string.theme_dark), ThemeMode.LIGHT to stringResource(R.string.theme_light)), state.theme, onTheme)
        }
        SettingsSection(stringResource(R.string.settings_language)) {
            Pills(listOf(null to stringResource(R.string.theme_system), "et" to stringResource(R.string.language_et), "en" to stringResource(R.string.language_en)), language, onLanguage)
        }
        val locale = LocalConfiguration.current.locales[0]
        val time = state.lastSyncMillis?.let { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale).withZone(SCHOOL_ZONE).format(Instant.ofEpochMilli(it)) }
        Text(if (time == null) stringResource(R.string.never_synced) else stringResource(R.string.last_synced, time), color = p.textSecondary)
        Button(onClick = onSyncNow, enabled = !state.syncing, colors = ButtonDefaults.buttonColors(containerColor = p.selected, contentColor = p.onSelected, disabledContainerColor = p.surface, disabledContentColor = p.textSecondary)) { Text(stringResource(R.string.sync_now)) }
        Spacer(Modifier.height(Clearance.Nav))
    }
}
@Composable private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = p.text, style = MaterialTheme.typography.titleMedium)
        Surface(shape = RoundedCornerShape(24.dp), color = p.card, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable private fun <T> Pills(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    val p = LocalPalette.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (value, label) ->
            Surface(shape = RoundedCornerShape(50), color = if (value == selected) p.selected else p.surface,
                modifier = Modifier.selectable(value == selected, role = Role.RadioButton, onClick = { onSelect(value) })) {
                Text(label, Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = if (value == selected) p.onSelected else p.text)
            }
        }
    }
}
