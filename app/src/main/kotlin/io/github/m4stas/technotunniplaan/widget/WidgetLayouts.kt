package io.github.m4stas.technotunniplaan.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import io.github.m4stas.technotunniplaan.R
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
    val width = LocalSize.current.width.value - outer * 2 - if (!narrow && !large) 48 else 0
    val shape = if (single || large) R.drawable.widget_card_18 else R.drawable.widget_card_16
    val fill = if (single || large) R.drawable.widget_fill_18 else R.drawable.widget_fill_16
    Row(GlanceModifier.fillMaxWidth().height(height.dp)) {
        if (!narrow && !large) {
            Column(GlanceModifier.width(38.dp)) {
                Label(lesson.start.format(timeFormat), 11, if (current) colors.accentText else colors.textSecondary)
                Label(lesson.end.format(timeFormat), 11, if (current) colors.accentTextSecondary else colors.textFaint)
            }
            Spacer(GlanceModifier.width(10.dp))
        }
        Box(GlanceModifier.defaultWeight().fillMaxHeight().shape(shape,
            if (current) colors.currentOutline else colors.cardOutline).padding(1.dp)) {
            Box(GlanceModifier.fillMaxSize().shape(shape, if (current) colors.widgetCurrentCard else colors.widgetCard)) {
                val elapsed = lesson.progress.coerceIn(0f, 1f) * (width - 2).coerceAtLeast(0f)
                if (current && elapsed >= 1) Spacer(GlanceModifier.width(elapsed.dp).fillMaxHeight().shape(fill, colors.accentTint))
                Column(GlanceModifier.fillMaxSize().padding(horizontal = (if (large || single) 14 else 12).dp,
                    vertical = (if (large) 12 else if (single) 0 else 8).dp), verticalAlignment = Alignment.CenterVertically) {
                    Label(lesson.text.title, if (large) 16 else if (single) 15 else 14,
                        if (current) colors.accentText else colors.text, large)
                    val clock = if (current) context.getString(R.string.widget_until, lesson.end.format(timeFormat)) else lesson.start.format(timeFormat)
                    val details = if (narrow && !large) {
                        (if (single) listOf(lesson.text.place, clock) else listOf(clock, lesson.text.place))
                    } else if (narrow) listOf(lesson.text.place) else listOf(lesson.text.who, lesson.text.place)
                    Label(details.filter { it.isNotBlank() }.joinToString(" · "), if (single || large) 12 else 11,
                        if (large || current) colors.textSecondary else colors.textMuted)
                    if (large) {
                        Spacer(GlanceModifier.defaultWeight())
                        Row(verticalAlignment = Alignment.Bottom) {
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

@Composable internal fun WidgetBreakDayLabel(lesson: WidgetLesson, colors: WidgetColors, narrow: Boolean, timeline: Boolean) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    Row(GlanceModifier.fillMaxWidth().height((if (timeline && lesson.newDay) 26 else 20).dp)
        .padding(start = (if (!timeline) 14 else if (narrow) 12 else 48).dp), verticalAlignment = Alignment.CenterVertically) {
        if (lesson.newDay) {
            Label(dayTitle(lesson.date, locale), if (timeline) 12 else 10, colors.textFaint, timeline)
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
                    Column(GlanceModifier.defaultWeight()) { Label(second.text.title, 13, colors.text) }
                    if (!narrow) Label(if (LocalSize.current.width.value >= 330) listOf(second.text.who, second.text.place).filter { it.isNotBlank() }.joinToString(" · ") else second.text.place, 12, colors.textMuted)
                }
            }
        }
    }
}

@Composable internal fun WidgetTimeline(entityName: String, lessons: List<WidgetLesson>, colors: WidgetColors, narrow: Boolean) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize().padding(12.dp)) {
        Row(GlanceModifier.fillMaxWidth().height(20.dp)) {
            lessons.firstOrNull()?.let { Label(dayTitle(it.date, context.resources.configuration.locales[0]), 18, colors.text, true) }
            if (!narrow) { Spacer(GlanceModifier.defaultWeight()); Label(entityName, 12, colors.textMuted) }
        }
        Spacer(GlanceModifier.height(8.dp))
        lessons.forEachIndexed { index, lesson ->
            if (index > 0 && (lesson.newDay || lesson.breakBefore != null)) WidgetBreakDayLabel(lesson, colors, narrow, true)
            WidgetLessonCard(lesson, colors, narrow, 46f, 12)
        }
    }
}

@Composable internal fun WidgetEmpty(colors: WidgetColors, single: Boolean) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize().padding((if (single) 8 else 12).dp), verticalAlignment = Alignment.CenterVertically, horizontalAlignment = Alignment.CenterHorizontally) {
        Label(context.getString(R.string.free_day), if (single) 36 else 44, colors.ghost, true)
        Label(context.getString(R.string.widget_no_lessons), 14, colors.text)
    }
}

@Composable internal fun WidgetNoData(colors: WidgetColors, single: Boolean) {
    Box(GlanceModifier.fillMaxSize().padding((if (single) 8 else 12).dp), contentAlignment = Alignment.Center) {
        Label(LocalContext.current.getString(R.string.widget_open_app), 14, colors.text)
    }
}
