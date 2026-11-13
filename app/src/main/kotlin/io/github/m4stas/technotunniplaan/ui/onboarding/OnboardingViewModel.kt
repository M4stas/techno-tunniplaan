package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import java.io.IOException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** A class or teacher the user can pick as their timetable. */
data class Choice(val ref: EntityRef, val entity: Entity)
sealed interface OnboardingState {
    data object Loading : OnboardingState
    data object Offline : OnboardingState
    /** Classes first, then teachers, filtered by [query]. */
    data class Pick(val query: String, val results: List<Choice>) : OnboardingState
}
class OnboardingViewModel(private val sync: SyncEngine, private val repository: TimetableRepository, private val settings: SettingsStore) : ViewModel() {
    private val mutableState = MutableStateFlow<OnboardingState>(OnboardingState.Loading)
    val state: StateFlow<OnboardingState> = mutableState.asStateFlow()
    private val mutablePicked = MutableStateFlow<EntityRef?>(null)
    /** The home that [pick] stored, set only after the settings write has finished. */
    val picked: StateFlow<EntityRef?> = mutablePicked.asStateFlow()
    init { load(force = false) }
    private fun load(force: Boolean) {
        mutableState.value = OnboardingState.Loading
        viewModelScope.launch {
            try {
                // AppRoot starts a stale check too, so the first load only downloads when that one has not.
                // A fresh sync that left the cache empty does not count, so an empty cache still downloads.
                if (repository.ensureLoaded().isEmpty()) {
                    if (force || (sync.syncIfStale() == null && repository.weeks.value.orEmpty().isEmpty())) sync.sync()
                }
            } catch (e: IOException) {
                // The cache or the settings could not be written. Offline lets the user retry.
            }
            if (repository.weeks.value.orEmpty().isEmpty()) mutableState.value = OnboardingState.Offline else show("")
        }
    }
    fun retry() { if (state.value != OnboardingState.Loading) load(force = true) }
    private fun show(query: String) {
        val schedule = Schedule(repository.weeks.value.orEmpty())
        val results = listOf(EntityKind.CLASS, EntityKind.TEACHER).flatMap { kind ->
            searchEntities(schedule.entities(kind), query).map { Choice(EntityRef(kind, it.key), it) }
        }
        mutableState.value = OnboardingState.Pick(query, results)
    }
    fun setQuery(query: String) { if (state.value is OnboardingState.Pick) show(query) }
    fun pick(choice: Choice) {
        val pick = state.value as? OnboardingState.Pick ?: return
        if (choice !in pick.results) return
        val ref = choice.ref
        viewModelScope.launch {
            try {
                settings.update { it.withHome(ref) }
                mutablePicked.value = ref
            } catch (e: IOException) {
                // The home could not be saved. The list stays on screen so the user can pick again.
            }
        }
    }
}
