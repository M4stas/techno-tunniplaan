package io.github.m4stas.technotunniplaan.schedule

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.Fixtures.CLASS_A
import io.github.m4stas.technotunniplaan.Fixtures.MARI
import io.github.m4stas.technotunniplaan.Fixtures.ROOM_101
import org.junit.Test
import kotlin.test.assertEquals

class LessonTextTest {
    @Test fun viewerSpecificTextAndWeekTitle() {
        val s = Schedule(listOf(Fixtures.week1()))
        val m = s.lessonsOn(CLASS_A, Fixtures.MONDAY)
        assertEquals(LessonText("08:30–10:00", "Matemaatika", "101", "Mari Näidis", null), lessonText(m[0], CLASS_A))
        assertEquals(LessonText("10:15–11:00", "Inglise keel", "102", "Jaan Proov", "Grupp 1"), lessonText(m[1], CLASS_A))
        assertEquals("K-XX-24A", lessonText(m[0], MARI).who)
        assertEquals("K-XX-24A" to "Mari Näidis", lessonText(m[0], ROOM_101).let { it.place to it.who })
        val pe = s.lessonsOn(MARI, Fixtures.MONDAY.plusDays(1))[0]
        assertEquals("K-XX-24A, K-XX-24B", lessonText(pe, MARI).who)
        assertEquals("", lessonText(pe, MARI).place)
        assertEquals("Nädal11 · 9.11–13.11", Fixtures.week1().title())
    }
}
