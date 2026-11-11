package io.github.m4stas.technotunniplaan.ui.settings

import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*

data class SplitUi(val split: Split, val chosen: String?)
data class SettingsUiState(val homeTitle: String, val splits: List<SplitUi>, val theme: ThemeMode, val lastSyncMillis: Long?, val syncing: Boolean)
fun settingsUiState(schedule: Schedule, settings: Settings, syncing: Boolean = false) = SettingsUiState(
    settings.home?.let { schedule.find(it)?.name }.orEmpty(),
    settings.home?.takeIf { it.kind == EntityKind.CLASS }?.let { home ->
        schedule.splitsFor(home.key).map { SplitUi(it, settings.subGroupChoices[it.id]) }
    }.orEmpty(), settings.theme, settings.lastSyncMillis, syncing,
)
