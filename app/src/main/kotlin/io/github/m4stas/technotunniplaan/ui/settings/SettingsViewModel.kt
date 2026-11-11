package io.github.m4stas.technotunniplaan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(repository: TimetableRepository, private val settings: SettingsStore, private val sync: SyncEngine) : ViewModel() {
    private val syncing = MutableStateFlow(false)
    val state: StateFlow<SettingsUiState> = combine(repository.weeks, settings.settings, syncing) { weeks, preferences, active ->
        settingsUiState(Schedule(weeks ?: emptyList()), preferences, active)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settingsUiState(Schedule(emptyList()), Settings()))
    fun chooseGroup(split: Split, group: String?) = write { it.copy(subGroupChoices = if (group == null) it.subGroupChoices - split.id else it.subGroupChoices + (split.id to group)) }
    fun setTheme(mode: ThemeMode) = write { it.copy(theme = mode) }
    private fun write(transform: (Settings) -> Settings) { viewModelScope.launch { try { settings.update(transform) } catch (_: IOException) { } } }
    fun syncNow() {
        if (!syncing.compareAndSet(false, true)) return
        viewModelScope.launch { try { sync.sync() } catch (_: IOException) { } finally { syncing.value = false } }
    }
}
