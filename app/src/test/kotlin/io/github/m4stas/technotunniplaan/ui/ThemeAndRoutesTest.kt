package io.github.m4stas.technotunniplaan.ui

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeAndRoutesTest {
    @Test fun palettesAndEncodedRoute() {
        assertEquals(DarkPalette, paletteFor(ThemeMode.SYSTEM, true))
        assertEquals(LightPalette, paletteFor(ThemeMode.LIGHT, true))
        assertEquals("entity/TEACHER/mari%20n%C3%A4idis", Routes.entity(Fixtures.MARI))
    }
}
