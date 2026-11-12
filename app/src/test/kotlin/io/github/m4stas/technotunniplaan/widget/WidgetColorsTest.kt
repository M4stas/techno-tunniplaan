package io.github.m4stas.technotunniplaan.widget

import androidx.compose.ui.graphics.Color
import io.github.m4stas.technotunniplaan.ui.theme.DarkPalette
import io.github.m4stas.technotunniplaan.ui.theme.LightPalette
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetColorsTest {
    @Test fun timeTextUsesTheAccentTextRole() {
        assertEquals(Color(0xFFD4FF3A), widgetAccent(DarkPalette))
        assertEquals(Color(0xFF4D6100), widgetAccent(LightPalette))
    }
}
