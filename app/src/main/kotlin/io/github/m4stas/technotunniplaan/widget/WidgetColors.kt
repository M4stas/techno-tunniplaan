package io.github.m4stas.technotunniplaan.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.Palette
import io.github.m4stas.technotunniplaan.ui.theme.paletteFor

internal data class WidgetRoles(
    val widgetBackground: Color,
    val widgetCard: Color,
    val widgetCurrentCard: Color,
    val cardOutline: Color,
    val currentOutline: Color,
    val accentTint: Color,
    val accentText: Color,
    val accentTextSecondary: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textFaint: Color,
    val ghost: Color,
)

internal fun widgetRoles(p: Palette) = WidgetRoles(
    widgetBackground = p.widgetBackground,
    widgetCard = p.widgetCard,
    widgetCurrentCard = p.widgetCurrentCard,
    cardOutline = p.cardOutline,
    currentOutline = p.currentOutline,
    accentTint = p.accentTint,
    accentText = widgetAccent(p),
    accentTextSecondary = p.accentTextSecondary,
    text = p.text,
    textSecondary = p.textSecondary,
    textMuted = p.textMuted,
    textFaint = p.textFaint,
    ghost = p.ghost,
)

internal data class WidgetColors(
    val widgetBackground: androidx.glance.unit.ColorProvider,
    val widgetCard: androidx.glance.unit.ColorProvider,
    val widgetCurrentCard: androidx.glance.unit.ColorProvider,
    val cardOutline: androidx.glance.unit.ColorProvider,
    val currentOutline: androidx.glance.unit.ColorProvider,
    val accentTint: androidx.glance.unit.ColorProvider,
    val accentText: androidx.glance.unit.ColorProvider,
    val accentTextSecondary: androidx.glance.unit.ColorProvider,
    val text: androidx.glance.unit.ColorProvider,
    val textSecondary: androidx.glance.unit.ColorProvider,
    val textMuted: androidx.glance.unit.ColorProvider,
    val textFaint: androidx.glance.unit.ColorProvider,
    val ghost: androidx.glance.unit.ColorProvider,
)

internal fun widgetColors(mode: ThemeMode): WidgetColors {
    val day = widgetRoles(paletteFor(mode, false))
    val night = widgetRoles(paletteFor(mode, true))
    return WidgetColors(
        widgetBackground = ColorProvider(day = day.widgetBackground, night = night.widgetBackground),
        widgetCard = ColorProvider(day = day.widgetCard, night = night.widgetCard),
        widgetCurrentCard = ColorProvider(day = day.widgetCurrentCard, night = night.widgetCurrentCard),
        cardOutline = ColorProvider(day = day.cardOutline, night = night.cardOutline),
        currentOutline = ColorProvider(day = day.currentOutline, night = night.currentOutline),
        accentTint = ColorProvider(day = day.accentTint, night = night.accentTint),
        accentText = ColorProvider(day = day.accentText, night = night.accentText),
        accentTextSecondary = ColorProvider(day = day.accentTextSecondary, night = night.accentTextSecondary),
        text = ColorProvider(day = day.text, night = night.text),
        textSecondary = ColorProvider(day = day.textSecondary, night = night.textSecondary),
        textMuted = ColorProvider(day = day.textMuted, night = night.textMuted),
        textFaint = ColorProvider(day = day.textFaint, night = night.textFaint),
        ghost = ColorProvider(day = day.ghost, night = night.ghost),
    )
}

/** The widget's time text. Lime on the dark widget, olive on the light one. */
internal fun widgetAccent(p: Palette): Color = p.accentText
