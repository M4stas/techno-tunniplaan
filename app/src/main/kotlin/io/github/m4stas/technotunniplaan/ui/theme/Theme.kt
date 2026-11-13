package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import io.github.m4stas.technotunniplaan.store.ThemeMode

/** Maps every Material role to a palette colour, so default Material controls never fall back to stock colours. */
fun colorSchemeFor(p: Palette, dark: Boolean): ColorScheme {
    val roles = { base: ColorScheme -> base.copy(
        primary = p.selected, onPrimary = p.onSelected, primaryContainer = p.track, onPrimaryContainer = p.text, inversePrimary = p.accent,
        secondary = p.selected, onSecondary = p.onSelected, secondaryContainer = p.track, onSecondaryContainer = p.text,
        tertiary = p.selected, onTertiary = p.onSelected, tertiaryContainer = p.track, onTertiaryContainer = p.text,
        background = p.background, onBackground = p.text, surface = p.surface, onSurface = p.text,
        surfaceVariant = p.card, onSurfaceVariant = p.textSecondary, surfaceTint = p.surface,
        inverseSurface = p.text, inverseOnSurface = p.background,
        error = p.error, onError = p.background, errorContainer = p.card, onErrorContainer = p.error,
        outline = p.outline, outlineVariant = p.outline, scrim = p.background,
        surfaceBright = p.card, surfaceDim = p.background, surfaceContainerLowest = p.background, surfaceContainerLow = p.surface,
        surfaceContainer = p.card, surfaceContainerHigh = p.card, surfaceContainerHighest = p.card,
        primaryFixed = p.track, primaryFixedDim = p.track, onPrimaryFixed = p.text, onPrimaryFixedVariant = p.textSecondary,
        secondaryFixed = p.track, secondaryFixedDim = p.track, onSecondaryFixed = p.text, onSecondaryFixedVariant = p.textSecondary,
        tertiaryFixed = p.track, tertiaryFixedDim = p.track, onTertiaryFixed = p.text, onTertiaryFixedVariant = p.textSecondary,
    ) }
    return roles(if (dark) darkColorScheme() else lightColorScheme())
}

/** Every dp and sp in the app is drawn this much larger than the system density. */
const val UI_SCALE = 1.25f

@Composable fun AppTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val p = paletteFor(mode, isSystemInDarkTheme())
    val system = LocalDensity.current
    val scaled = Density(system.density * UI_SCALE, system.fontScale)
    CompositionLocalProvider(LocalPalette provides p, LocalDensity provides scaled) { MaterialTheme(colorScheme = colorSchemeFor(p, p == DarkPalette), typography = AppTypography, content = content) }
}
