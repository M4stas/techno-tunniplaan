package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.schedule.*

data class GridCell(val day: Int, val firstPeriod: Int, val lastPeriod: Int, val lessons: List<Lesson>)
data class WeekGrid(val days: List<Int>, val periods: List<Period>, val cells: List<GridCell>)
fun weekGrid(timetable: Timetable, byDay: Map<Int, List<Lesson>>): WeekGrid {
    val lessons = byDay.values.flatten()
    val first = lessons.minOfOrNull { it.firstPeriod }
    val last = lessons.maxOfOrNull { it.lastPeriod }
    return WeekGrid((0..if (timetable.hasSaturday) 5 else 4).toList(),
        timetable.periods.filter { first != null && last != null && it.number in first..last }.sortedBy { it.number },
        byDay.toSortedMap().flatMap { (day, daily) -> daily.overlapGroups().map { group ->
            GridCell(day, group.minOf { it.firstPeriod }, group.maxOf { it.lastPeriod }, group)
        } })
}
