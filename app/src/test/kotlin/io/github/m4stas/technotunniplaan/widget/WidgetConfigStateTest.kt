package io.github.m4stas.technotunniplaan.widget

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.m4stas.technotunniplaan.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WidgetConfigStateTest {
    @Test fun roundTripsEntityAndCount() {
        val config = WidgetConfig(Fixtures.MARI, 3)
        assertEquals(config, mutablePreferencesOf().apply { put(config) }.toWidgetConfig())
    }
    @Test fun defaultsAndClampsCount() {
        assertEquals(WidgetConfig(), mutablePreferencesOf().toWidgetConfig())
        assertEquals(WidgetConfig(null, 5), mutablePreferencesOf().apply { put(WidgetConfig(null, 9)) }.toWidgetConfig())
        assertEquals(WidgetConfig(null, 1), mutablePreferencesOf().apply { put(WidgetConfig(null, 0)) }.toWidgetConfig())
    }
    @Test fun ignoresUnknownEntityKind() {
        assertEquals(WidgetConfig(), mutablePreferencesOf(stringPreferencesKey("entity_kind") to "PLANET", stringPreferencesKey("entity_key") to "x").toWidgetConfig())
    }
    @Test fun removesPreviousEntity() {
        val prefs = mutablePreferencesOf().apply { put(WidgetConfig(Fixtures.MARI, 3)); put(WidgetConfig()) }
        assertEquals(WidgetConfig(), prefs.toWidgetConfig())
    }
    @Test fun redrawMarkChangesStateButNotConfig() {
        val config = WidgetConfig(Fixtures.MARI, 2)
        val before = mutablePreferencesOf().apply { put(config); markRedraw(1L) }
        val after = before.toMutablePreferences().apply { markRedraw(2L) }
        assertNotEquals(before, after)
        assertEquals(config, after.toWidgetConfig())
    }
}
