package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import io.github.m4stas.technotunniplaan.store.ThemeMode

@Composable fun AppTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val p = paletteFor(mode, isSystemInDarkTheme())
    val scheme = lightColorScheme(primary = p.accent, onPrimary = p.onAccent,
        secondary = p.accent, onSecondary = p.onAccent, tertiary = p.accent, onTertiary = p.onAccent,
        background = p.background, onBackground = p.text, surface = p.surface, onSurface = p.text,
        surfaceVariant = p.card, onSurfaceVariant = p.textSecondary, outline = p.outline,
        primaryContainer = p.highlight, onPrimaryContainer = p.text, error = p.error,
        onError = p.background, surfaceContainer = p.card)
    CompositionLocalProvider(LocalPalette provides p) { MaterialTheme(colorScheme = scheme, content = content) }
}
