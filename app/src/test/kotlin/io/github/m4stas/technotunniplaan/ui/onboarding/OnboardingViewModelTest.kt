package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.github.m4stas.technotunniplaan.*
import io.github.m4stas.technotunniplaan.edupage.EduPageDecoder
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
            val all = (vm.settled() as OnboardingState.Pick).results
            assertEquals(listOf(EntityKind.CLASS, EntityKind.CLASS), all.take(2).map { it.ref.kind })
            assertEquals(listOf("K-XX-24A", "K-XX-24B"), all.take(2).map { it.entity.name })
            assertTrue(all.any { it.ref == Fixtures.MARI })
            vm.setQuery("24b")
            assertEquals(listOf("K-XX-24B"), (vm.state.value as OnboardingState.Pick).results.map { it.entity.name })
            vm.setQuery("mari")
            vm.pick((vm.state.value as OnboardingState.Pick).results.single())
            assertEquals(Fixtures.MARI, vm.picked.first { it != null })
            assertEquals(Fixtures.MARI, settings.settings.first().home)
            api.requested.clear(); api.requestedYear = null; api.failure = IOException()
            val cached = OnboardingViewModel(sync, repository, settings)
            assertTrue(cached.settled() is OnboardingState.Pick)
            assertNull(api.requestedYear)
            assertTrue(api.requested.isEmpty())
        } finally { Dispatchers.resetMain() }
    }

    @Test fun storageFailureShowsOffline() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            // A file where the cache directory should be makes every cache write fail.
            File(tmp.root, "cache").writeText("x")
            val repository = TimetableRepository(TimetableCache(File(tmp.root, "cache")), StandardTestDispatcher(testScheduler))
            val settings = SettingsStore(PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") })
            val api = FakeApi(EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()))
            val sync = SyncEngine(api, repository, settings, Clock.fixed(Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE))
            val vm = OnboardingViewModel(sync, repository, settings)
            assertEquals(OnboardingState.Offline, vm.settled())
            vm.retry()
            assertEquals(OnboardingState.Offline, vm.settled())
        } finally { Dispatchers.resetMain() }
    }

    @Test fun firstLaunchDownloadsOnce() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = TimetableRepository(TimetableCache(File(tmp.root, "cache")), StandardTestDispatcher(testScheduler))
            val settings = SettingsStore(PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") })
            val api = FakeApi(EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()))
            val sync = SyncEngine(api, repository, settings, Clock.fixed(Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE))
            // AppRoot's stale check is still downloading when the view model starts.
            val entered = CompletableDeferred<Unit>()
            val gate = CompletableDeferred<Unit>()
            api.onList = { entered.complete(Unit); gate.await() }
            val appSync = async { sync.syncIfStale() }
            entered.await()
            val vm = OnboardingViewModel(sync, repository, settings)
            advanceUntilIdle()
            gate.complete(Unit)
            assertTrue(vm.settled() is OnboardingState.Pick)
            appSync.await()
            assertEquals(listOf("22"), api.requested)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun failedHomeWriteStaysOnTheList() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = TimetableRepository(TimetableCache(File(tmp.root, "cache")), StandardTestDispatcher(testScheduler))
            val store = FailingWrites(PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") })
            val settings = SettingsStore(store)
            val api = FakeApi(EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()))
            val sync = SyncEngine(api, repository, settings, Clock.fixed(Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE))
            val vm = OnboardingViewModel(sync, repository, settings)
            assertTrue(vm.settled() is OnboardingState.Pick)
            val list = vm.state.value as OnboardingState.Pick
            store.failing = true
            vm.pick(list.results.first { it.ref == Fixtures.MARI })
            store.attempted.await()
            advanceUntilIdle()
            assertNull(vm.picked.value)
            assertEquals(list, vm.state.value)
            assertNull(settings.settings.first().home)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun emptyCacheDownloadsEvenAfterARecentSync() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val now = Instant.parse("2026-11-10T08:00:00Z")
            val repository = TimetableRepository(TimetableCache(File(tmp.root, "cache")), StandardTestDispatcher(testScheduler))
            val settings = SettingsStore(PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") })
            settings.update { it.copy(lastSyncMillis = now.toEpochMilli() - 60_000) }
            val api = FakeApi(EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()))
            val sync = SyncEngine(api, repository, settings, Clock.fixed(now, SCHOOL_ZONE))
            val vm = OnboardingViewModel(sync, repository, settings)
            assertTrue(vm.settled() is OnboardingState.Pick)
            assertEquals(listOf("22"), api.requested)
        } finally { Dispatchers.resetMain() }
    }

    /** Reads pass through. Once [failing] is set, every write throws IOException, as on a full disk. */
    private class FailingWrites(private val delegate: DataStore<Preferences>) : DataStore<Preferences> {
        @Volatile var failing = false
        val attempted = CompletableDeferred<Unit>()
        override val data = delegate.data
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            if (failing) { attempted.complete(Unit); throw IOException("disk full") }
            return delegate.updateData(transform)
        }
    }

    private suspend fun OnboardingViewModel.settled() = state.first { it != OnboardingState.Loading }
}
