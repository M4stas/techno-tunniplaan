package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import java.io.IOException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Role { STUDENT, TEACHER }
sealed interface OnboardingState {
    data object Loading : OnboardingState
    data object Offline : OnboardingState
    data object ChooseRole : OnboardingState
    data class Pick(val role: Role, val query: String, val results: List<Entity>) : OnboardingState
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
            mutableState.value = if (repository.weeks.value.orEmpty().isEmpty()) OnboardingState.Offline else OnboardingState.ChooseRole
        }
    }
    fun retry() { if (state.value != OnboardingState.Loading) load(force = true) }
    fun chooseRole(role: Role) { show(role, "") }
    private fun show(role: Role, query: String) {
        val kind = if (role == Role.STUDENT) EntityKind.CLASS else EntityKind.TEACHER
        mutableState.value = OnboardingState.Pick(role, query, searchEntities(Schedule(repository.weeks.value.orEmpty()).entities(kind), query))
    }
    fun setQuery(query: String) { (state.value as? OnboardingState.Pick)?.let { show(it.role, query) } }
    fun back() { mutableState.value = OnboardingState.ChooseRole }
    fun pick(entity: Entity) {
        val pick = state.value as? OnboardingState.Pick ?: return
        if (entity !in pick.results) return
        val ref = EntityRef(if (pick.role == Role.STUDENT) EntityKind.CLASS else EntityKind.TEACHER, entity.key)
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
