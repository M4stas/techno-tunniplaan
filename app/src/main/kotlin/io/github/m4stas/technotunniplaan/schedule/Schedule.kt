package io.github.m4stas.technotunniplaan.schedule

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SubGroupFilter(val classKey: String, val choices: Map<String, String>)
data class DatedLesson(val date: LocalDate, val lesson: Lesson) {
    val startsAt: LocalDateTime get() = date.atTime(lesson.start)
    val endsAt: LocalDateTime get() = date.atTime(lesson.end)
}

class Schedule(weeks: List<Timetable>, private val filter: SubGroupFilter? = null) {
    val weeks = weeks.sortedBy { it.monday }
    fun entities(kind: EntityKind): List<Entity> = weeks.flatMap {
        when (kind) {
            EntityKind.CLASS -> it.classes
            EntityKind.TEACHER -> it.teachers
            EntityKind.ROOM -> it.rooms
        }
    }.associateBy { it.key }.values.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    fun find(ref: EntityRef): Entity? = entities(ref.kind).find { it.key == ref.key }
    fun splitsFor(classKey: String): List<Split> = weeks.flatMap { it.splits }
        .filter { it.classKey == classKey }.distinctBy { it.id }
    fun schoolDays(): List<LocalDate> = weeks.flatMap { week ->
        (0..if (week.hasSaturday) 5 else 4).map { week.monday.plusDays(it.toLong()) }
    }.distinct().sorted()
    fun initialDate(today: LocalDate): LocalDate? = schoolDays().let { days ->
        days.firstOrNull { it >= today } ?: days.lastOrNull()
    }
    fun weekOf(date: LocalDate): Timetable? = weeks.find { date >= it.monday && date < it.monday.plusDays(7) }
    fun lessonsOn(entity: EntityRef, date: LocalDate): List<Lesson> {
        val week = weekOf(date) ?: return emptyList()
        val day = java.time.temporal.ChronoUnit.DAYS.between(week.monday, date).toInt()
        return week.lessons.filter { lesson ->
            val matches = when (entity.kind) {
                EntityKind.CLASS -> lesson.classes
                EntityKind.TEACHER -> lesson.teachers
                EntityKind.ROOM -> lesson.rooms
            }.any { it.key == entity.key }
            val visible = filter == null || entity != EntityRef(EntityKind.CLASS, filter.classKey) ||
                splitsFor(filter.classKey).all { split ->
                    val choice = filter.choices[split.id]
                    val groups = lesson.subGroups.filter { it.classKey == filter.classKey && it.name in split.groups }
                    choice == null || groups.isEmpty() || groups.any { it.name == choice }
                }
            lesson.day == day && matches && visible
        }.sortedBy { it.start }
    }
    fun week(entity: EntityRef, monday: LocalDate): Map<Int, List<Lesson>> = (0..6).associateWith {
        lessonsOn(entity, monday.plusDays(it.toLong()))
    }.filterValues { it.isNotEmpty() }
    fun nextLessons(entity: EntityRef, now: LocalDateTime, count: Int): List<DatedLesson> =
        schoolDays().flatMap { date -> lessonsOn(entity, date).map { DatedLesson(date, it) } }
            .filter { it.endsAt > now }.sortedBy { it.startsAt }.take(count.coerceAtLeast(0))
}

fun List<Lesson>.overlapGroups(): List<List<Lesson>> {
    val groups = mutableListOf<MutableList<Lesson>>()
    for (lesson in sortedBy { it.start }) {
        val last = groups.lastOrNull()
        if (last != null && lesson.start < last.maxOf { it.end }) last.add(lesson)
        else groups.add(mutableListOf(lesson))
    }
    return groups
}
fun List<Lesson>.highlightIndex(now: LocalTime): Int? {
    val current = indexOfFirst { it.start <= now && now < it.end }
    if (current >= 0) return current
    return indices.filter { this[it].start > now }.minByOrNull { this[it].start }
}
fun searchEntities(all: List<Entity>, query: String): List<Entity> =
    if (query.isBlank()) all else all.filter { it.name.contains(query, true) || it.short.contains(query, true) }
