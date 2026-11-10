package io.github.m4stas.technotunniplaan.schedule

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.Fixtures.CLASS_A
import io.github.m4stas.technotunniplaan.Fixtures.CLASS_B
import io.github.m4stas.technotunniplaan.Fixtures.JAAN
import io.github.m4stas.technotunniplaan.Fixtures.MARI
import io.github.m4stas.technotunniplaan.Fixtures.ROOM_101
import java.time.LocalTime
import org.junit.Test
import kotlin.test.*

class ScheduleTest {
    private val s = Schedule(listOf(Fixtures.week1()))
    private val both = Schedule(listOf(Fixtures.week2(), Fixtures.week1()))
    private val mon = Fixtures.MONDAY
    private fun at(day: Long, h: Int, m: Int) = mon.plusDays(day).atTime(h, m)
    private fun List<Lesson>.subjects() = map { it.subject }

    @Test fun entityLessonsAndFiltering() {
        assertEquals(listOf("Matemaatika", "Inglise keel", "Inglise keel"), s.lessonsOn(CLASS_A, mon).subjects())
        assertEquals(listOf("Kehaline kasvatus"), s.lessonsOn(CLASS_A, mon.plusDays(1)).subjects())
        assertEquals(listOf("Programmeerimine", "Kehaline kasvatus"), s.lessonsOn(CLASS_B, mon.plusDays(1)).subjects())
        assertEquals(listOf("Kehaline kasvatus"), s.lessonsOn(JAAN, mon.plusDays(1)).subjects())
        assertEquals(listOf(0, 2, 4), s.week(ROOM_101, mon).keys.sorted())
        assertEquals(setOf(0, 1, 2, 4), s.week(CLASS_A, mon).keys)
        assertEquals(emptyList<Lesson>(), s.lessonsOn(CLASS_A, mon.minusDays(3)))
        assertEquals(emptyList<Lesson>(), s.lessonsOn(EntityRef(EntityKind.CLASS, "nobody"), mon))
        val f = Schedule(listOf(Fixtures.week1()), SubGroupFilter("k-xx-24a", mapOf("Grupp 1|Grupp 2" to "Grupp 2")))
        assertEquals(listOf("Mari Näidis", "Tiit Katse"), f.lessonsOn(CLASS_A, mon).map { it.teachers[0].name })
        assertEquals(1, f.lessonsOn(JAAN, mon).size)
        assertEquals(listOf(Split("k-xx-24a", listOf("Grupp 1", "Grupp 2"))), s.splitsFor("k-xx-24a"))
        assertEquals(emptyList<Split>(), s.splitsFor("k-xx-24b"))
    }
    @Test fun upcomingLessonsAndDays() {
        assertEquals(listOf("Matemaatika", "Inglise keel"), s.nextLessons(CLASS_A, at(0, 9, 0), 2).map { it.lesson.subject })
        assertEquals(mon.plusDays(1), s.nextLessons(CLASS_A, at(0, 11, 0), 1).single().date)
        assertEquals(emptyList<DatedLesson>(), s.nextLessons(CLASS_A, at(4, 14, 15), 3))
        assertEquals(mon.plusWeeks(1), both.nextLessons(CLASS_A, at(4, 14, 15), 3).first().date)
        assertEquals(6, s.nextLessons(CLASS_A, at(-8, 12, 0), 50).size)
        assertEquals(emptyList<DatedLesson>(), s.nextLessons(CLASS_A, at(0, 9, 0), 0))
        assertEquals((0L..4L).map { mon.plusDays(it) }, s.schoolDays())
        assertEquals(mon.plusDays(1), s.initialDate(mon.plusDays(1)))
        assertEquals(mon.plusDays(7), both.initialDate(mon.plusDays(5)))
        assertEquals(mon.plusDays(4), s.initialDate(mon.plusDays(5)))
        assertNull(Schedule(emptyList()).initialDate(mon))
    }
    @Test fun entitiesAndDisplayHelpers() {
        assertEquals(3, both.entities(EntityKind.TEACHER).size)
        assertEquals("Näidis Mari", both.find(MARI)!!.name)
        assertEquals(1, both.lessonsOn(MARI, mon).size)
        assertEquals(1, both.lessonsOn(MARI, mon.plusWeeks(1)).size)
        val m = s.lessonsOn(CLASS_A, mon)
        assertEquals(listOf(1, 2), m.overlapGroups().map { it.size })
        assertEquals(0, m.highlightIndex(LocalTime.of(9, 0)))
        assertEquals(1, m.highlightIndex(LocalTime.of(10, 5)))
        assertNull(m.highlightIndex(LocalTime.of(11, 0)))
        assertEquals(listOf("Mari Näidis"), searchEntities(s.entities(EntityKind.TEACHER), "näi").map { it.name })
        assertEquals(listOf("Mari Näidis"), searchEntities(s.entities(EntityKind.TEACHER), "mn").map { it.name })
        assertEquals(listOf("Jaan Proov", "Mari Näidis", "Tiit Katse"), searchEntities(s.entities(EntityKind.TEACHER), " ").map { it.name })
    }
}
