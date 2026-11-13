package io.github.m4stas.technotunniplaan.widget

import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.Settings
import io.github.m4stas.technotunniplaan.ui.timetable.BreakUi
import io.github.m4stas.technotunniplaan.ui.timetable.SlotStatus
import io.github.m4stas.technotunniplaan.ui.timetable.breakBetween
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class WidgetConfig(val entity: EntityRef? = null, val count: Int = 1)
fun WidgetConfig.resolve(settings: Settings): EntityRef? = entity ?: settings.home

enum class WidgetLayout { SINGLE, NOW_NEXT, TIMELINE }
fun widgetLayout(heightDp: Int): WidgetLayout = when {
    heightDp < 170 -> WidgetLayout.SINGLE
    heightDp < 230 -> WidgetLayout.NOW_NEXT
    else -> WidgetLayout.TIMELINE
}
fun isNarrowWidget(widthDp: Int): Boolean = widthDp < 240

data class WidgetLesson(
    val date: LocalDate, val start: LocalTime, val end: LocalTime, val text: LessonText,
    val status: SlotStatus, val progress: Float, val breakBefore: BreakUi?, val newDay: Boolean,
)

sealed interface WidgetContent {
    data object NoData : WidgetContent
    data object NoLessons : WidgetContent
    data class Lessons(val entityName: String, val lessons: List<WidgetLesson>) : WidgetContent
}

fun widgetContent(schedule: Schedule?, entity: EntityRef?, now: LocalDateTime, count: Int): WidgetContent {
    if (schedule == null || schedule.weeks.isEmpty() || entity == null) return WidgetContent.NoData
    val upcoming = schedule.nextLessons(entity, now, count.coerceIn(1, 5))
    if (upcoming.isEmpty()) return WidgetContent.NoLessons
    return WidgetContent.Lessons(schedule.find(entity)?.name.orEmpty(), upcoming.mapIndexed { index, dated ->
        val lesson = dated.lesson
        val previous = upcoming.getOrNull(index - 1)
        val current = now >= dated.startsAt && now < dated.endsAt
        val progress = if (current) {
            (Duration.between(lesson.start, now.toLocalTime()).seconds.toFloat() /
                Duration.between(lesson.start, lesson.end).seconds).coerceIn(0f, 1f)
        } else 0f
        WidgetLesson(dated.date, lesson.start, lesson.end, lessonText(lesson, entity),
            if (current) SlotStatus.CURRENT else SlotStatus.UPCOMING, progress,
            previous?.takeIf { it.date == dated.date && lesson.start > it.lesson.end }
                ?.let { breakBetween(it.lesson.end, lesson.start) },
            previous != null && previous.date != dated.date)
    })
}

fun timelineRowsThatFit(heightDp: Int, lessons: List<WidgetLesson>): Int {
    var available = heightDp - 12 - 12 - 28
    var rows = 0
    for (lesson in lessons) {
        val cost = 46 + if (rows == 0) 0 else if (lesson.newDay) 26 else if (lesson.breakBefore != null) 20 else 0
        if (available < cost) break
        available -= cost
        rows++
    }
    return rows.coerceAtLeast(minOf(1, lessons.size))
}

fun nextRedrawAt(upcoming: List<DatedLesson>, now: LocalDateTime): LocalDateTime? {
    val end = upcoming.minOfOrNull { it.endsAt } ?: return null
    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
    val next = if (upcoming.any { it.date > now.toLocalDate() } && midnight < end) midnight else end
    return if (upcoming.any { now >= it.startsAt && now < it.endsAt }) minOf(next, now.plusMinutes(5)) else next
}
