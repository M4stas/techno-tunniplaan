package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class DayUi(val date: LocalDate, val slots: List<SlotUi>)
data class TimetableUiState(
    val entity: EntityRef, val title: String, val entityMissing: Boolean, val isHome: Boolean, val isFavourite: Boolean,
    val days: List<DayUi>, val selectedDate: LocalDate?,
    val weekTitle: String, val weekIsOutdated: Boolean, val lastSyncMillis: Long?, val syncError: SyncError?, val refreshing: Boolean,
    val nextLesson: NextLessonUi? = null,
)
fun timetableUiState(schedule: Schedule, settings: Settings, entity: EntityRef, selected: LocalDate?, now: LocalDateTime, refreshing: Boolean = false): TimetableUiState {
    val dates = schedule.schoolDays()
    val date = selected?.let { requested ->
        requested.takeIf { it in dates } ?: schedule.weekOf(requested)?.let { destination ->
            dates.lastOrNull { schedule.weekOf(it)?.monday == destination.monday }
        }
    } ?: schedule.initialDate(now.toLocalDate())
    val week = date?.let(schedule::weekOf)
    val found = schedule.find(entity)
    val today = now.toLocalDate()
    val next = schedule.nextLessons(entity, now, Int.MAX_VALUE).firstOrNull { it.startsAt > now }
    return TimetableUiState(entity, found?.name.orEmpty(), found == null && schedule.weeks.isNotEmpty(), entity == settings.home,
        entity in settings.favourites, dates.map { day ->
            val lessons = schedule.lessonsOn(entity, day)
            DayUi(day,
                daySlots(lessons, entity, now.toLocalTime().takeIf { day == today }))
        }, date, week?.title().orEmpty(), week != null && week.monday.plusDays(6) < today,
        settings.lastSyncMillis, settings.lastSyncError, refreshing,
        next?.let { NextLessonUi(it.date, it.lesson.start, ChronoUnit.DAYS.between(today, it.date).toInt()) })
}
