package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.m4stas.technotunniplaan.store.ThemeMode

/** Every colour the app draws. Screens read roles from here and hold no colour literals. */
data class Palette(
    val onIslandStrong: Color, val backgroundClear: Color, val divider: Color, val background: Color, val surface: Color, val card: Color, val cardOutline: Color,
    val currentCard: Color, val currentOutline: Color, val accentTint: Color,
    val accent: Color, val onAccent: Color, val accentText: Color, val accentTextSecondary: Color,
    val text: Color, val textSecondary: Color, val textMuted: Color, val textFaint: Color, val ghost: Color,
    val island: Color, val onIsland: Color, val track: Color, val selected: Color, val onSelected: Color,
    val selectedRow: Color, val sheet: Color, val outline: Color, val error: Color,
)

val DarkPalette = Palette(
    onIslandStrong = Color(0xFFE8E8E6), backgroundClear = Color(0x00000000),
    divider = Color(0xFF1C1C1C),
    background = Color(0xFF000000), surface = Color(0xFF121212), card = Color(0xFF0E0E0E), cardOutline = Color(0xFF0E0E0E),
    currentCard = Color(0xFF121212), currentOutline = Color(0xFF262626), accentTint = Color(0x29D4FF3A),
    accent = Color(0xFFD4FF3A), onAccent = Color(0xFF000000), accentText = Color(0xFFD4FF3A), accentTextSecondary = Color(0xFF7F9A1F),
    text = Color(0xFFF4F4F2), textSecondary = Color(0xFF8A8A8A), textMuted = Color(0xFF777777), textFaint = Color(0xFF4D4D4D), ghost = Color(0xFF1E1E1E),
    island = Color(0xFF1B1B1B), onIsland = Color(0xFF8A8A8A), track = Color(0xFF1A1A1A), selected = Color(0xFFD4FF3A), onSelected = Color(0xFF000000),
    selectedRow = Color(0x1FD4FF3A), sheet = Color(0xFF111111), outline = Color(0xFF262626), error = Color(0xFFFF6B6B),
)

val LightPalette = Palette(
    onIslandStrong = Color(0xFFE8E8E6), backgroundClear = Color(0x00F6F6F3),
    divider = Color(0xFFEBEBE7),
    background = Color(0xFFF6F6F3), surface = Color(0xFFFFFFFF), card = Color(0xFFFFFFFF), cardOutline = Color(0xFFEBEBE7),
    currentCard = Color(0xFFFFFFFF), currentOutline = Color(0xFFCFE46A), accentTint = Color(0xFFE6FF86),
    accent = Color(0xFFD4FF3A), onAccent = Color(0xFF000000), accentText = Color(0xFF4D6100), accentTextSecondary = Color(0xFF8EA33A),
    text = Color(0xFF111111), textSecondary = Color(0xFF6B6B66), textMuted = Color(0xFF8A8A85), textFaint = Color(0xFFA9A9A3), ghost = Color(0xFFE6E6E1),
    island = Color(0xFF111111), onIsland = Color(0xFF8A8A8A), track = Color(0xFFEFEFEB), selected = Color(0xFF111111), onSelected = Color(0xFFD4FF3A),
    selectedRow = Color(0xFFF0FBC2), sheet = Color(0xFFFFFFFF), outline = Color(0xFFEBEBE7), error = Color(0xFFB3261E),
)

val LocalPalette = staticCompositionLocalOf { LightPalette }
fun paletteFor(mode: ThemeMode, systemDark: Boolean): Palette = if (mode == ThemeMode.DARK || mode == ThemeMode.SYSTEM && systemDark) DarkPalette else LightPalette
