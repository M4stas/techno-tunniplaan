package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
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
    init { load() }
    private fun load() {
        mutableState.value = OnboardingState.Loading
        viewModelScope.launch {
            if (repository.ensureLoaded().isEmpty()) sync.sync()
            mutableState.value = if (repository.weeks.value.orEmpty().isEmpty()) OnboardingState.Offline else OnboardingState.ChooseRole
        }
    }
    fun retry() { if (state.value != OnboardingState.Loading) load() }
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
        viewModelScope.launch { settings.update { it.withHome(ref) }; mutablePicked.value = ref }
    }
}
