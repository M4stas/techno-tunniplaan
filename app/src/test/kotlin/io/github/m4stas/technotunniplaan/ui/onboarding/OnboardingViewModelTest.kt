package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.m4stas.technotunniplaan.*
import io.github.m4stas.technotunniplaan.edupage.EduPageDecoder
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule val tmp = TemporaryFolder()
    @Test fun offlineRetrySearchAndPick() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = TimetableRepository(TimetableCache(File(tmp.root, "cache")), StandardTestDispatcher(testScheduler))
            val settings = SettingsStore(PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") })
            val api = FakeApi(EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()), IOException())
            val sync = SyncEngine(api, repository, settings, Clock.fixed(Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE))
            val vm = OnboardingViewModel(sync, repository, settings)
            // DataStore writes on its own IO thread, so wait on the state instead of advanceUntilIdle alone.
            assertEquals(OnboardingState.Offline, vm.settled())
            api.failure = null
            vm.retry()
            assertEquals(OnboardingState.ChooseRole, vm.settled())
            vm.chooseRole(Role.STUDENT)
            assertEquals(listOf("K-XX-24A", "K-XX-24B"), (vm.state.value as OnboardingState.Pick).results.map { it.name })
            vm.setQuery("24b")
            assertEquals(listOf("K-XX-24B"), (vm.state.value as OnboardingState.Pick).results.map { it.name })
            vm.back(); assertEquals(OnboardingState.ChooseRole, vm.state.value)
            vm.chooseRole(Role.TEACHER)
            vm.pick(Entity("mari näidis", "Mari Näidis", "MN"))
            assertEquals(Fixtures.MARI, vm.picked.first { it != null })
            assertEquals(Fixtures.MARI, settings.settings.first().home)
            api.requested.clear(); api.requestedYear = null; api.failure = IOException()
            val cached = OnboardingViewModel(sync, repository, settings)
            assertEquals(OnboardingState.ChooseRole, cached.settled())
            assertNull(api.requestedYear)
            assertTrue(api.requested.isEmpty())
        } finally { Dispatchers.resetMain() }
    }

    private suspend fun OnboardingViewModel.settled() = state.first { it != OnboardingState.Loading }
}
