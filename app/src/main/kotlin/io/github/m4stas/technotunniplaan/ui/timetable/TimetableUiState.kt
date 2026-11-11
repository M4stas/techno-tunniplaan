package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import java.time.LocalDate
import java.time.LocalDateTime

data class DayUi(val date: LocalDate, val rows: List<List<Lesson>>, val highlight: Lesson?)
data class TimetableUiState(
    val entity: EntityRef, val title: String, val entityMissing: Boolean, val isHome: Boolean, val isFavourite: Boolean,
    val viewMode: ViewMode, val days: List<DayUi>, val selectedDate: LocalDate?,
    val weekTitle: String, val weekIsOutdated: Boolean, val lastSyncMillis: Long?, val syncError: SyncError?, val refreshing: Boolean,
)
fun timetableUiState(schedule: Schedule, settings: Settings, entity: EntityRef, selected: LocalDate?, now: LocalDateTime, refreshing: Boolean = false): TimetableUiState {
    val dates = schedule.schoolDays()
    val date = selected?.takeIf { it in dates } ?: schedule.initialDate(now.toLocalDate())
    val week = date?.let(schedule::weekOf)
    val found = schedule.find(entity)
    return TimetableUiState(entity, found?.name.orEmpty(), found == null && schedule.weeks.isNotEmpty(), entity == settings.home,
        entity in settings.favourites, settings.viewMode, dates.map { day ->
            val lessons = schedule.lessonsOn(entity, day)
            DayUi(day, lessons.overlapGroups(), if (day == now.toLocalDate()) lessons.highlightIndex(now.toLocalTime())?.let(lessons::get) else null)
        }, date, week?.title().orEmpty(), week != null && week.monday.plusDays(6) < now.toLocalDate(),
        settings.lastSyncMillis, settings.lastSyncError, refreshing)
}
