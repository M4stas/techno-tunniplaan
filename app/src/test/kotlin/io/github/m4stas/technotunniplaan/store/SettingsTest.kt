package io.github.m4stas.technotunniplaan.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.SubGroupFilter
import java.io.File
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun readFailureRecoversForTheSameCollector() = runTest {
        var attempts = 0
        val dataStore = ReadStore(flow {
            if (attempts++ == 0) throw IOException("read failed")
            emit(preferencesOf(stringPreferencesKey("settings") to """{"home":{"kind":"CLASS","key":"k-xx-24a"}}"""))
        })
        val received = mutableListOf<Settings>()
        val collector = launch { SettingsStore(dataStore).settings.take(2).toList(received) }
        runCurrent()
        assertEquals(listOf(Settings()), received)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, received.size)
        assertEquals(Fixtures.CLASS_A, received.last().home)
        collector.join()
    }

    @Test fun firstReadReturnsDefaultsWithoutWaitingForRetry() = runTest {
        val store = SettingsStore(ReadStore(flow { throw IOException("read failed") }))
        val before = testScheduler.currentTime
        assertEquals(Settings(), store.settings.first())
        assertEquals(before, testScheduler.currentTime)
    }

    @Test fun nonIoReadFailurePropagates() = runTest {
        val failure = IllegalStateException("invalid store")
        val store = SettingsStore(ReadStore(flow { throw failure }))
        val thrown = runCatching { store.settings.first() }.exceptionOrNull()
        assertSame(failure, thrown)
    }

    @Test fun settingsSavedWithAViewModeStillLoad() = runTest {
        val stored = preferencesOf(stringPreferencesKey("settings") to """{"home":{"kind":"CLASS","key":"k-xx-24a"},"viewMode":"WEEK"}""")
        assertEquals(Settings(home = Fixtures.CLASS_A), SettingsStore(ReadStore(flowOf(stored))).settings.first())
    }

    private class ReadStore(override val data: Flow<Preferences>) : DataStore<Preferences> {
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            error("Read-only test store")
    }

    @Test fun settingsPersistAndRecover() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { File(tmp.root, "settings.preferences_pb") })
        val store = SettingsStore(dataStore)
        assertEquals(Settings(), store.settings.first())
        val choices = mapOf("Grupp 1|Grupp 2" to "Grupp 2")
        store.update { it.withHome(Fixtures.CLASS_A).copy(subGroupChoices = choices, theme = ThemeMode.DARK) }
        val saved = store.settings.first()
        assertEquals(Fixtures.CLASS_A, saved.home)
        assertEquals(ThemeMode.DARK, saved.theme)
        assertEquals(SubGroupFilter("k-xx-24a", choices), saved.subGroupFilter())
        assertTrue(saved.withHome(Fixtures.CLASS_B).subGroupChoices.isEmpty())
        assertNull(Settings(home = Fixtures.MARI, subGroupChoices = choices).subGroupFilter())
        assertEquals(listOf(Fixtures.MARI), Settings().toggleFavourite(Fixtures.MARI).favourites)
        assertTrue(Settings().toggleFavourite(Fixtures.MARI).toggleFavourite(Fixtures.MARI).favourites.isEmpty())
        dataStore.edit { it[stringPreferencesKey("settings")] = "{oops" }
        assertEquals(Settings(), store.settings.first())
    }
}
