package io.github.m4stas.technotunniplaan.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeAndRoutesTest {
    @Test fun palettesAndEncodedRoute() {
        assertEquals(DarkPalette, paletteFor(ThemeMode.SYSTEM, true))
        assertEquals(LightPalette, paletteFor(ThemeMode.LIGHT, true))
        assertEquals("entity/TEACHER/mari%20n%C3%A4idis", Routes.entity(Fixtures.MARI))
    }

    @Test fun everyMaterialRoleComesFromThePalette() {
        for ((palette, dark) in listOf(DarkPalette to true, LightPalette to false)) {
            val allowed = with(palette) { setOf(background, surface, card, accent, onAccent, text, textSecondary, outline, highlight, error) }
            val roles = roleColors(colorSchemeFor(palette, dark))
            assertTrue("no colour roles found", roles.size > 20)
            val stray = roles.filterValues { it !in allowed }
            assertTrue("roles outside the palette: ${stray.keys}", stray.isEmpty())
        }
    }

    // ColorScheme getters return Color as its packed Long; read them all so new Material roles are caught too.
    private fun roleColors(scheme: ColorScheme): Map<String, Color> = ColorScheme::class.java.methods
        .filter { it.name.startsWith("get") && it.parameterCount == 0 && it.returnType == java.lang.Long.TYPE }
        .associate { it.name.substringBefore('-') to Color((it.invoke(scheme) as Long).toULong()) }
}
