package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

class TimetableViewModel(private val entity: EntityRef?, repository: TimetableRepository, private val settings: SettingsStore, private val sync: SyncEngine, private val clock: Clock) : ViewModel() {
    private val selected = MutableStateFlow<LocalDate?>(null)
    private val refreshing = MutableStateFlow(false)
    private val minute = flow {
        while (true) {
            emit(LocalDateTime.now(clock))
            delay(60_000L - Math.floorMod(clock.millis(), 60_000L))
        }
    }
    val state: StateFlow<TimetableUiState?> = combine(repository.weeks, settings.settings, selected, minute, refreshing) { weeks, preferences, date, now, busy ->
        val ref = entity ?: preferences.home
        if (weeks == null || ref == null) null else timetableUiState(Schedule(weeks, preferences.subGroupFilter()), preferences, ref, date, now, busy)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    fun selectDate(date: LocalDate) { selected.value = date }
    fun setViewMode(mode: ViewMode) { viewModelScope.launch { try { settings.update { it.copy(viewMode = mode) } } catch (_: IOException) { } } }
    fun toggleFavourite() { val ref = state.value?.entity ?: return; viewModelScope.launch { try { settings.update { it.toggleFavourite(ref) } } catch (_: IOException) { } } }
    fun refresh() {
        if (refreshing.value) return
        refreshing.value = true
        viewModelScope.launch { try { sync.sync() } catch (_: IOException) { } finally { refreshing.value = false } }
    }
}
