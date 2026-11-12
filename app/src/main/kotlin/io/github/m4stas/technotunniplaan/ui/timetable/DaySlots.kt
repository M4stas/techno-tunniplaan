package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.schedule.Lesson
import io.github.m4stas.technotunniplaan.schedule.lessonText
import io.github.m4stas.technotunniplaan.schedule.overlapGroups
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal val HOUR_MINUTE: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

enum class SlotStatus { PAST, CURRENT, UPCOMING }
enum class BreakKind { BREAK, LUNCH, FREE }
data class BreakUi(val minutes: Int, val kind: BreakKind)

/** One lesson inside a slot. [time] is set only when the lesson's own times differ from the slot's. */
data class LessonLine(val lesson: Lesson, val subject: String, val who: String, val place: String, val time: String?)

/** Lessons that overlap in time. The day view draws one card for each slot. */
data class SlotUi(val start: LocalTime, val end: LocalTime, val lines: List<LessonLine>, val sameSubject: Boolean,
    val status: SlotStatus, val progress: Float, val breakBefore: BreakUi?)

data class NextLessonUi(val date: LocalDate, val start: LocalTime, val daysAhead: Int)

fun breakBetween(previousEnd: LocalTime, nextStart: LocalTime): BreakUi? {
    val minutes = Duration.between(previousEnd, nextStart).toMinutes().toInt()
    return when {
        minutes <= 0 -> null
        minutes < 30 -> BreakUi(minutes, BreakKind.BREAK)
        minutes <= 60 -> BreakUi(minutes, BreakKind.LUNCH)
        else -> BreakUi(minutes, BreakKind.FREE)
    }
}

/** [now] is the school's local time when the lessons are today's, and null for any other day. */
fun daySlots(lessons: List<Lesson>, viewer: EntityRef, now: LocalTime?): List<SlotUi> {
    var previousEnd: LocalTime? = null
    return lessons.overlapGroups().map { group ->
        val start = group.minOf { it.start }
        val end = group.maxOf { it.end }
        val status = when {
            now == null -> SlotStatus.UPCOMING
            now >= end -> SlotStatus.PAST
            now >= start -> SlotStatus.CURRENT
            else -> SlotStatus.UPCOMING
        }
        val progress = if (now != null && status == SlotStatus.CURRENT) {
            (Duration.between(start, now).seconds.toFloat() / Duration.between(start, end).seconds).coerceIn(0f, 1f)
        } else 0f
        val lines = group.map { lesson ->
            val text = lessonText(lesson, viewer)
            LessonLine(lesson, text.title, text.who, text.place, text.time.takeIf { lesson.start != start || lesson.end != end })
        }
        val slot = SlotUi(start, end, lines, lines.map { it.subject }.distinct().size == 1, status, progress, previousEnd?.let { breakBetween(it, start) })
        previousEnd = end
        slot
    }
}
