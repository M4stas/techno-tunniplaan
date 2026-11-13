package io.github.m4stas.technotunniplaan.ui.settings

import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*

data class SettingsUiState(val homeTitle: String, val homeKind: EntityKind?, val theme: ThemeMode, val lastSyncMillis: Long?, val syncing: Boolean)
fun settingsUiState(schedule: Schedule, settings: Settings, syncing: Boolean = false) = SettingsUiState(
    settings.home?.let { schedule.find(it)?.name }.orEmpty(),
    settings.home?.kind, settings.theme, settings.lastSyncMillis, syncing,
)
