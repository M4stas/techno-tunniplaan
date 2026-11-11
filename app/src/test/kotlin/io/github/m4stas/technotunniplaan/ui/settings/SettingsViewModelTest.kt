package io.github.m4stas.technotunniplaan.ui.settings

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.junit.Assert.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @get:org.junit.Rule val tmp = org.junit.rules.TemporaryFolder()
    @Test fun nightModesMatchAppCompat() {
        assertEquals(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, nightMode(ThemeMode.SYSTEM))
        assertEquals(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES, nightMode(ThemeMode.DARK))
        assertEquals(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO, nightMode(ThemeMode.LIGHT))
    }
    @Test fun writesChoicesAndSyncs() = kotlinx.coroutines.test.runTest {
        kotlinx.coroutines.Dispatchers.setMain(kotlinx.coroutines.test.StandardTestDispatcher(testScheduler))
        try {
            val repository = TimetableRepository(TimetableCache(java.io.File(tmp.root, "cache")), kotlinx.coroutines.test.StandardTestDispatcher(testScheduler))
            val settings = SettingsStore(androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(scope = backgroundScope) { java.io.File(tmp.root, "settings.preferences_pb") })
            val api = io.github.m4stas.technotunniplaan.FakeApi(io.github.m4stas.technotunniplaan.edupage.EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()))
            val sync = io.github.m4stas.technotunniplaan.sync.SyncEngine(api, repository, settings, java.time.Clock.fixed(java.time.Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE))
            val vm = SettingsViewModel(repository, settings, sync)
            val split = Split("k-xx-24a", listOf("Grupp 1", "Grupp 2"))
            vm.chooseGroup(split, "Grupp 1")
            assertEquals(mapOf(split.id to "Grupp 1"), settings.settings.first { it.subGroupChoices.isNotEmpty() }.subGroupChoices)
            vm.chooseGroup(split, null)
            assertTrue(settings.settings.first { it.subGroupChoices.isEmpty() }.subGroupChoices.isEmpty())
            vm.setTheme(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, settings.settings.first { it.theme == ThemeMode.LIGHT }.theme)
            vm.syncNow()
            // Wait until syncNow has finished, then let the state's stop timer run before Main is reset.
            vm.state.first { it.lastSyncMillis != null && !it.syncing }
            advanceUntilIdle()
            assertEquals(listOf("22"), api.requested)
        } finally { kotlinx.coroutines.Dispatchers.resetMain() }
    }

    @Test fun classGroupsUseStoredChoice() {
        val schedule = Schedule(listOf(Fixtures.week1()))
        val state = settingsUiState(schedule, Settings(home = Fixtures.CLASS_A, subGroupChoices = mapOf("Grupp 1|Grupp 2" to "Grupp 2")))
        assertEquals("K-XX-24A", state.homeTitle)
        assertEquals(listOf(SplitUi(Split("k-xx-24a", listOf("Grupp 1", "Grupp 2")), "Grupp 2")), state.splits)
        assertTrue(settingsUiState(schedule, Settings(home = Fixtures.MARI)).splits.isEmpty())
        assertTrue(settingsUiState(schedule, Settings(home = Fixtures.CLASS_B)).splits.isEmpty())
    }
}
