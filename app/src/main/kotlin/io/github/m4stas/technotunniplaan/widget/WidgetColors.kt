package io.github.m4stas.technotunniplaan.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.Palette
import io.github.m4stas.technotunniplaan.ui.theme.paletteFor

internal data class WidgetColors(val background: androidx.glance.unit.ColorProvider, val text: androidx.glance.unit.ColorProvider,
    val secondary: androidx.glance.unit.ColorProvider, val accent: androidx.glance.unit.ColorProvider)
internal fun widgetColors(mode: ThemeMode): WidgetColors {
    fun provider(color: (Palette) -> Color): androidx.glance.unit.ColorProvider =
        ColorProvider(day = color(paletteFor(mode, false)), night = color(paletteFor(mode, true)))
    return WidgetColors(provider { it.background }, provider { it.text }, provider { it.textSecondary }, provider { it.accent })
}
