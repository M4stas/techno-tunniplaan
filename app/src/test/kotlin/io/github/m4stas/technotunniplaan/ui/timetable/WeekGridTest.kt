package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import org.junit.Test
import org.junit.Assert.*

class WeekGridTest {
    @Test fun buildsSpanningCells() {
        val week = Fixtures.week1()
        val grid = weekGrid(week, Schedule(listOf(week)).week(Fixtures.CLASS_A, Fixtures.MONDAY))
        assertEquals(listOf(0, 1, 2, 3, 4), grid.days)
        assertEquals((1..6).toList(), grid.periods.map { it.number })
        assertEquals(listOf(Triple(0,1,2), Triple(0,3,3), Triple(1,5,6), Triple(2,3,4), Triple(4,6,6)), grid.cells.map { Triple(it.day,it.firstPeriod,it.lastPeriod) })
        assertEquals(2, grid.cells[1].lessons.size)
        assertEquals(emptyList<Period>(), weekGrid(week, emptyMap()).periods)
        assertEquals((1..3).toList(), weekGrid(week, Schedule(listOf(week)).week(EntityRef(EntityKind.ROOM,"x-102"),Fixtures.MONDAY)).periods.map { it.number })
    }
}
