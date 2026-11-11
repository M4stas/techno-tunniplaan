package io.github.m4stas.technotunniplaan.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import kotlinx.coroutines.flow.*

class BrowseViewModel(repository: TimetableRepository, settings: SettingsStore) : ViewModel() {
    private val tab = MutableStateFlow(EntityKind.CLASS)
    private val query = MutableStateFlow("")
    val state: StateFlow<BrowseUiState> = combine(repository.weeks, settings.settings, tab, query) { weeks, preferences, kind, text ->
        browseUiState(Schedule(weeks ?: emptyList()), preferences, kind, text)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), browseUiState(Schedule(emptyList()), Settings(), EntityKind.CLASS, ""))
    fun setTab(tab: EntityKind) { this.tab.value = tab }
    fun setQuery(query: String) { this.query.value = query }
}
