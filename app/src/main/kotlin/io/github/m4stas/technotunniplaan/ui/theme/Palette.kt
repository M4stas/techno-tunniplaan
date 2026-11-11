package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.m4stas.technotunniplaan.store.ThemeMode

data class Palette(val background: Color, val surface: Color, val card: Color, val accent: Color, val onAccent: Color,
    val text: Color, val textSecondary: Color, val outline: Color, val highlight: Color, val error: Color)
val DarkPalette = Palette(Color(0xFF0B0F0E), Color(0xFF151B19), Color(0xFF1C2422), Color(0xFF3DDC84), Color(0xFF06120C), Color(0xFFF2F5F4), Color(0xFF9AA5A1), Color(0xFF2A3431), Color(0xFF133023), Color(0xFFFF6B6B))
val LightPalette = Palette(Color(0xFFF6F8F7), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFF0B8F4D), Color(0xFFFFFFFF), Color(0xFF101413), Color(0xFF5B6763), Color(0xFFDCE3E0), Color(0xFFDDF3E6), Color(0xFFB3261E))
val LocalPalette = staticCompositionLocalOf { LightPalette }
fun paletteFor(mode: ThemeMode, systemDark: Boolean): Palette = if (mode == ThemeMode.DARK || mode == ThemeMode.SYSTEM && systemDark) DarkPalette else LightPalette
