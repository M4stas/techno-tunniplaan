package io.github.m4stas.technotunniplaan.ui.browse

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.Settings
import org.junit.Assert.*
import org.junit.Test

class BrowseUiStateTest {
    @Test fun searchesAndKeepsPinOrder() {
        val s = Schedule(listOf(Fixtures.week1()))
        assertEquals(listOf("Mari Näidis"), browseUiState(s, Settings(), EntityKind.TEACHER, "näi").results.map { it.name })
        assertEquals(listOf("X-101", "X-102", "X-103"), browseUiState(s, Settings(), EntityKind.ROOM, "").results.map { it.name })
        val settings = Settings(favourites = listOf(Fixtures.ROOM_101, EntityRef(EntityKind.TEACHER, "gone"), Fixtures.MARI))
        assertEquals(listOf(Fixtures.ROOM_101, Fixtures.MARI), browseUiState(s, settings, EntityKind.CLASS, "").favourites.map { it.first })
        assertEquals(3, settings.favourites.size)
    }
}
