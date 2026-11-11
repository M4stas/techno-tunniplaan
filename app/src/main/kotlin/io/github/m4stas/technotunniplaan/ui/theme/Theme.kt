package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.m4stas.technotunniplaan.store.ThemeMode

/** Maps every Material role to a palette colour, so default Material controls never fall back to stock colours. */
fun colorSchemeFor(p: Palette, dark: Boolean): ColorScheme {
    val roles = { base: ColorScheme -> base.copy(
        primary = p.accent, onPrimary = p.onAccent, primaryContainer = p.highlight, onPrimaryContainer = p.text, inversePrimary = p.accent,
        secondary = p.accent, onSecondary = p.onAccent, secondaryContainer = p.highlight, onSecondaryContainer = p.text,
        tertiary = p.accent, onTertiary = p.onAccent, tertiaryContainer = p.highlight, onTertiaryContainer = p.text,
        background = p.background, onBackground = p.text, surface = p.surface, onSurface = p.text,
        surfaceVariant = p.card, onSurfaceVariant = p.textSecondary, surfaceTint = p.surface,
        inverseSurface = p.text, inverseOnSurface = p.background,
        error = p.error, onError = p.background, errorContainer = p.card, onErrorContainer = p.error,
        outline = p.outline, outlineVariant = p.outline, scrim = p.background,
        surfaceBright = p.card, surfaceDim = p.background, surfaceContainerLowest = p.background, surfaceContainerLow = p.surface,
        surfaceContainer = p.card, surfaceContainerHigh = p.card, surfaceContainerHighest = p.card,
        primaryFixed = p.highlight, primaryFixedDim = p.highlight, onPrimaryFixed = p.text, onPrimaryFixedVariant = p.textSecondary,
        secondaryFixed = p.highlight, secondaryFixedDim = p.highlight, onSecondaryFixed = p.text, onSecondaryFixedVariant = p.textSecondary,
        tertiaryFixed = p.highlight, tertiaryFixedDim = p.highlight, onTertiaryFixed = p.text, onTertiaryFixedVariant = p.textSecondary,
    ) }
    return roles(if (dark) darkColorScheme() else lightColorScheme())
}

@Composable fun AppTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val p = paletteFor(mode, isSystemInDarkTheme())
    CompositionLocalProvider(LocalPalette provides p) { MaterialTheme(colorScheme = colorSchemeFor(p, p == DarkPalette), content = content) }
}
