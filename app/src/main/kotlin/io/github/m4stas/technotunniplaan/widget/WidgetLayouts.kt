package io.github.m4stas.technotunniplaan.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.LessonText
import io.github.m4stas.technotunniplaan.ui.timetable.*
import java.time.format.DateTimeFormatter

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
private fun GlanceModifier.shape(drawable: Int, color: ColorProvider) =
    background(ImageProvider(drawable), colorFilter = ColorFilter.tint(color))

@Composable private fun Label(value: String, size: Int, color: ColorProvider, bold: Boolean = false) {
    Text(value, style = TextStyle(color = color, fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium), maxLines = 1)
}

@Composable internal fun WidgetLessonCard(lesson: WidgetLesson, colors: WidgetColors, narrow: Boolean,
    height: Float, outer: Int, large: Boolean = false, single: Boolean = false) {
    val context = LocalContext.current
    val current = lesson.status == SlotStatus.CURRENT
    val compact = single && isCompactWidget(LocalSize.current.height.value.toInt())
    val title = lesson.entries.map { it.title }.distinct().joinToString(" / ")
    val stacked = lesson.entries.size > 1 && !single && !large
    val column = !narrow && !large && !single
    val width = LocalSize.current.width.value - outer * 2 - if (column) 48 else 0
    val shape = if (single || large) R.drawable.widget_card_18 else R.drawable.widget_card_16
    val fill = if (single || large) R.drawable.widget_fill_18 else R.drawable.widget_fill_16
    Row(GlanceModifier.fillMaxWidth().height(height.dp)) {
        if (column) {
            TimeColumn(lesson, colors, current)
            Spacer(GlanceModifier.width(10.dp))
        }
        Box(GlanceModifier.defaultWeight().fillMaxHeight().shape(shape,
            if (current) colors.currentOutline else colors.cardOutline).padding(1.dp)) {
            Box(GlanceModifier.fillMaxSize().shape(shape, if (current) colors.widgetCurrentCard else colors.widgetCard)) {
                val elapsed = lesson.progress.coerceIn(0f, 1f) * (width - 2).coerceAtLeast(0f)
                if (current && elapsed >= 1) Spacer(GlanceModifier.width(elapsed.dp).fillMaxHeight().shape(fill, colors.accentTint))
                Row(GlanceModifier.fillMaxSize().padding(horizontal = (if (large || single) 14 else 12).dp,
                    vertical = (if (large) 12 else 0).dp), verticalAlignment = Alignment.CenterVertically) {
                    val clock = if (current) context.getString(R.string.widget_until, lesson.end.format(timeFormat)) else lesson.start.format(timeFormat)
                    if (compact) {
                        Label("$title · $clock", 12, if (current) colors.accentText else colors.text)
                    } else {
                        if (single && !narrow) TimeColumn(lesson, colors, current)
                        Column(GlanceModifier.defaultWeight().fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                            val titleColor = if (current) colors.accentText else colors.text
                            val detailSize = if (single || large) 12 else 11
                            val detailColor = if (large || current) colors.textSecondary else colors.textMuted
                            if (stacked && narrow) {
                                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(GlanceModifier.defaultWeight()) { Label(title, 14, titleColor) }
                                    Label(clock, detailSize, detailColor)
                                }
                            } else Label(title, if (large) 16 else if (single) 15 else 14, titleColor, large)
                            if (stacked) EntryRows(lesson.entries, colors, detailSize, detailColor, titleColor)
                            else {
                                val who = lesson.entries.map { it.who.trim() }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                                val places = lesson.entries.map { it.place.trim() }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                                val details = if (narrow && !large) {
                                    (if (single) listOf(places, clock) else listOf(clock, places))
                                } else if (narrow) listOf(places) else listOf(who, places)
                                Label(details.map { it.trim() }.filter { it.isNotBlank() }.joinToString(" · "), detailSize, detailColor)
                            }
                            if (large) {
                                Spacer(GlanceModifier.defaultWeight())
                                Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                                    Label(if (narrow) (if (current) lesson.end else lesson.start).format(timeFormat)
                                        else lesson.start.format(timeFormat), 22, if (current) colors.accentText else colors.textSecondary, true)
                                    Label(if (narrow) " " + context.getString(if (current) R.string.widget_ends else R.string.widget_starts)
                                        else " – " + lesson.end.format(timeFormat), 12, if (current) colors.accentTextSecondary else colors.textSecondary)
                                    if (LocalSize.current.width.value >= 330) {
                                        Spacer(GlanceModifier.defaultWeight())
                                        Label(dayTitle(lesson.date, context.resources.configuration.locales[0]), 12, colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** One row per parallel lesson: who on the left, place on the right, a hairline between rows. */
@Composable private fun EntryRows(entries: List<LessonText>, colors: WidgetColors, size: Int,
    whoColor: ColorProvider, placeColor: ColorProvider) {
    entries.forEachIndexed { index, entry ->
        val place = entry.place.trim()
        if (index > 0) Spacer(GlanceModifier.fillMaxWidth().height(1.dp).background(colors.cardOutline))
        Row(GlanceModifier.fillMaxWidth().height(23.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) { Label(entry.who.trim(), size, whoColor) }
            if (place.isNotBlank()) { Spacer(GlanceModifier.width(8.dp)); Label(place, size, placeColor) }
        }
    }
}

@Composable private fun TimeColumn(lesson: WidgetLesson, colors: WidgetColors, current: Boolean) {
    Column(GlanceModifier.width(38.dp)) {
        Label(lesson.start.format(timeFormat), 11, if (current) colors.accentText else colors.textSecondary)
        Label(lesson.end.format(timeFormat), 11, if (current) colors.accentTextSecondary else colors.textFaint)
    }
}

@Composable internal fun WidgetBreakDayLabel(lesson: WidgetLesson, colors: WidgetColors, narrow: Boolean, timeline: Boolean) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    Row(GlanceModifier.fillMaxWidth().height((if (timeline && lesson.newDay) 26 else 20).dp)
        .padding(start = (if (!timeline) 14 else if (narrow) 12 else 48).dp), verticalAlignment = Alignment.CenterVertically) {
        if (lesson.newDay) {
            Label(dayTitle(lesson.date, locale), if (timeline) 12 else 10, if (timeline) colors.textSecondary else colors.textFaint, timeline)
            if (timeline) {
                Spacer(GlanceModifier.defaultWeight())
                Label(dateLabel(lesson.date, context.getString(R.string.short_date_pattern), locale), 12, colors.textMuted)
            }
        } else lesson.breakBefore?.let {
            val resource = when (it.kind) { BreakKind.BREAK -> R.string.break_short; BreakKind.LUNCH -> R.string.break_lunch; BreakKind.FREE -> R.string.break_free }
            Label(context.getString(resource, it.minutes), 10, colors.textFaint)
        }
    }
}

@Composable internal fun WidgetSingle(lessons: List<WidgetLesson>, colors: WidgetColors, narrow: Boolean) {
    Column(GlanceModifier.fillMaxSize().padding(8.dp)) {
        lessons.firstOrNull()?.let { WidgetLessonCard(it, colors, narrow, (LocalSize.current.height.value - 16).coerceAtLeast(0f), 8, single = true) }
    }
}

@Composable internal fun WidgetNowNext(lessons: List<WidgetLesson>, colors: WidgetColors, narrow: Boolean) {
    val second = lessons.getOrNull(1)
    val label = second != null && (second.newDay || second.breakBefore != null)
    Column(GlanceModifier.fillMaxSize().padding(12.dp)) {
        lessons.firstOrNull()?.let { WidgetLessonCard(it, colors, narrow,
            (LocalSize.current.height.value - 24 - (if (second != null) 38 else 0) - (if (label) 20 else 0)).coerceAtLeast(0f), 12, large = true) }
        if (second != null) {
            if (label) WidgetBreakDayLabel(second, colors, narrow, false)
            Box(GlanceModifier.fillMaxWidth().height(38.dp).shape(R.drawable.widget_slim_14, colors.cardOutline).padding(1.dp)) {
                Row(GlanceModifier.fillMaxSize().shape(R.drawable.widget_slim_14, colors.widgetCard).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Label(second.start.format(timeFormat), 13, colors.textSecondary)
                    Spacer(GlanceModifier.width(10.dp))
                    val places = second.entries.map { it.place.trim() }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    val who = second.entries.map { it.who.trim() }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                    Column(GlanceModifier.defaultWeight()) { Label(second.entries.map { it.title }.distinct().joinToString(" / "), 13, colors.text) }
                    if (!narrow) Label(if (second.entries.size == 1 && LocalSize.current.width.value >= 330) listOf(who, places).filter { it.isNotBlank() }.joinToString(" · ") else places, 12, colors.textMuted)
                }
            }
        }
    }
}

@Composable internal fun WidgetTimeline(entityName: String, lessons: List<WidgetLesson>, colors: WidgetColors, narrow: Boolean) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize().padding(12.dp)) {
        Row(GlanceModifier.fillMaxWidth().height(28.dp)) {
            lessons.firstOrNull()?.let { Label(dayTitle(it.date, context.resources.configuration.locales[0]), 18, colors.text, true) }
            if (!narrow) { Spacer(GlanceModifier.defaultWeight()); Label(entityName, 12, colors.textMuted) }
        }
        lessons.forEachIndexed { index, lesson ->
            if (index > 0 && (lesson.newDay || lesson.breakBefore != null)) WidgetBreakDayLabel(lesson, colors, narrow, true)
            WidgetLessonCard(lesson, colors, narrow, timelineCardHeight(lesson).toFloat(), 12)
        }
    }
}

@Composable internal fun WidgetEmpty(colors: WidgetColors, single: Boolean) {
    val context = LocalContext.current
    val compact = isCompactWidget(LocalSize.current.height.value.toInt())
    Column(GlanceModifier.fillMaxSize().padding((if (single) 8 else 12).dp), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
        if (!compact) Label(context.getString(R.string.free_day), if (single) 36 else 44, colors.ghost, true)
        Label(context.getString(R.string.widget_no_lessons), 14, colors.text)
    }
}

@Composable internal fun WidgetNoData(colors: WidgetColors, single: Boolean) {
    Box(GlanceModifier.fillMaxSize().padding((if (single) 8 else 12).dp), contentAlignment = Alignment.Center) {
        Label(LocalContext.current.getString(R.string.widget_open_app), 14, colors.text)
    }
}
