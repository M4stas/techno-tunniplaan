package io.github.m4stas.technotunniplaan.widget

import androidx.compose.ui.graphics.Color
import io.github.m4stas.technotunniplaan.ui.theme.DarkPalette
import io.github.m4stas.technotunniplaan.ui.theme.LightPalette
import org.junit.Assert.assertEquals
import org.junit.Test
import io.github.m4stas.technotunniplaan.store.ThemeMode

class WidgetColorsTest {
    @Test fun rolesUseWidgetColoursAndAccent() {
        for ((palette, background, current) in listOf(
            Triple(DarkPalette, Color(0xFF121212), Color(0xFF1B1B1B)),
            Triple(LightPalette, Color(0xFFF6F6F3), Color(0xFFF4FBD6)),
        )) {
            val roles = widgetRoles(palette)
            assertEquals(background, roles.widgetBackground)
            assertEquals(current, roles.widgetCurrentCard)
            assertEquals(widgetAccent(palette), roles.accentText)
        }
    }
    @Test fun buildsAllModes() { ThemeMode.entries.forEach { widgetColors(it) } }
    @Test fun timeTextUsesTheAccentTextRole() {
        assertEquals(Color(0xFFD4FF3A), widgetAccent(DarkPalette))
        assertEquals(Color(0xFF4D6100), widgetAccent(LightPalette))
    }
}
