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

    private fun Palette.all() = setOf(background, surface, card, cardOutline, currentCard, currentOutline, accentTint, accent, onAccent,
        accentText, accentTextSecondary, text, textSecondary, textMuted, textFaint, ghost, island, onIsland, track, selected, onSelected,
        selectedRow, sheet, outline, error)

    @Test fun everyMaterialRoleComesFromThePalette() {
        for ((palette, dark) in listOf(DarkPalette to true, LightPalette to false)) {
            val roles = roleColors(colorSchemeFor(palette, dark))
            assertTrue("no colour roles found", roles.size > 20)
            val stray = roles.filterValues { it !in palette.all() }
            assertTrue("roles outside the palette: ${stray.keys}", stray.isEmpty())
        }
    }

    @Test fun palettesHoldTheApprovedColours() {
        assertEquals(Color(0xFF000000), DarkPalette.background)
        assertEquals(Color(0xFFF4F4F2), DarkPalette.text)
        assertEquals(Color(0xFFD4FF3A), DarkPalette.accent)
        assertEquals(Color(0xFFD4FF3A), DarkPalette.accentText)
        assertEquals(Color(0x29D4FF3A), DarkPalette.accentTint)
        assertEquals(Color(0xFF1B1B1B), DarkPalette.island)
        assertEquals(Color(0xFFF6F6F3), LightPalette.background)
        assertEquals(Color(0xFF4D6100), LightPalette.accentText)
        assertEquals(Color(0xFFE6FF86), LightPalette.accentTint)
        assertEquals(Color(0xFF111111), LightPalette.island)
        assertEquals(DarkPalette.accent, LightPalette.accent)
    }

    @Test fun materialButtonsFollowTheSelectedRole() {
        val light = colorSchemeFor(LightPalette, false)
        assertEquals(Color(0xFF111111), light.primary)
        assertEquals(Color(0xFFD4FF3A), light.onPrimary)
        val dark = colorSchemeFor(DarkPalette, true)
        assertEquals(Color(0xFFD4FF3A), dark.primary)
        assertEquals(Color(0xFF000000), dark.onPrimary)
    }

    // ColorScheme getters return Color as its packed Long; read them all so new Material roles are caught too.
    private fun roleColors(scheme: ColorScheme): Map<String, Color> = ColorScheme::class.java.methods
        .filter { it.name.startsWith("get") && it.parameterCount == 0 && it.returnType == java.lang.Long.TYPE }
        .associate { it.name.substringBefore('-') to Color((it.invoke(scheme) as Long).toULong()) }
}
