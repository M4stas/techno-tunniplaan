package io.github.m4stas.technotunniplaan.store

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.SubGroupFilter
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsTest {
    @get:Rule val tmp = TemporaryFolder()

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
