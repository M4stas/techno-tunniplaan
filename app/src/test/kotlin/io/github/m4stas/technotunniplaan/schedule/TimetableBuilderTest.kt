package io.github.m4stas.technotunniplaan.schedule
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.edupage.RawTimetable
import java.time.LocalTime
import org.junit.Test
import kotlin.test.*

class TimetableBuilderTest {
    private fun build(raw: RawTimetable) = buildTimetable(raw, Fixtures.MONDAY, "Nädal11")
    @Test fun fixture() {
        val t = Fixtures.week1()
        assertEquals(7, t.lessons.size)
        assertEquals(listOf(0, 0, 0, 1, 1, 2, 4), t.lessons.map { it.day })
        with(t.lessons[0]) {
            assertEquals("Matemaatika", subject); assertEquals("Mat", subjectShort)
            assertEquals(1 to 2, firstPeriod to lastPeriod)
            assertEquals(LocalTime.of(8, 30) to LocalTime.of(10, 0), start to end)
            assertEquals(listOf(Entity("mari näidis", "Mari Näidis", "MN")), teachers)
            assertEquals(listOf(Entity("x-101", "X-101", "101")), rooms)
            assertEquals(emptyList<SubGroup>(), subGroups)
        }
        assertEquals(listOf(SubGroup("k-xx-24a", "Grupp 1")), t.lessons[1].subGroups)
        assertEquals(listOf(SubGroup("k-xx-24a", "Grupp 2")), t.lessons[2].subGroups)
        val pe = t.lessons.single { it.subject == "Kehaline kasvatus" }
        assertEquals(listOf("k-xx-24a", "k-xx-24b"), pe.classes.map { it.key })
        assertEquals(2, pe.teachers.size); assertEquals(emptyList<Entity>(), pe.rooms)
        assertEquals(listOf(Split("k-xx-24a", listOf("Grupp 1", "Grupp 2"))), t.splits)
        assertEquals("Grupp 1|Grupp 2", t.splits[0].id)
        assertEquals(3, t.teachers.size); assertTrue(t.lessons.none { it.day == 5 })
    }
    @Test fun unknownLessonIsSkipped() { val r = Fixtures.raw(); assertEquals(6, build(r.copy(cards = r.cards.map { if (it.id == "c1") it.copy(lessonid = "*99") else it })).lessons.size) }
    @Test fun unknownPeriodIsSkipped() { val r = Fixtures.raw(); assertEquals(6, build(r.copy(cards = r.cards.map { if (it.id == "c1") it.copy(period = "20") else it })).lessons.size) }
    @Test fun unknownTeacherIsDropped() { val r = Fixtures.raw(); val t = build(r.copy(lessons = r.lessons.map { if (it.id == "*1") it.copy(teacherids = listOf("-99")) else it })); assertEquals(7, t.lessons.size); assertTrue(t.lessons.first().teachers.isEmpty()) }
    @Test fun unknownSubjectIsBlank() { val r = Fixtures.raw(); val t = build(r.copy(lessons = r.lessons.map { if (it.id == "*1") it.copy(subjectid = "-99") else it })); assertTrue(t.lessons.any { it.day == 0 && it.start == LocalTime.of(8, 30) && it.subject == "" }) }
    @Test fun doublePeriodAtDayEnd() { val r = Fixtures.raw(); val t = build(r.copy(cards = r.cards.map { if (it.id == "c7") it.copy(lessonid = "*1") else it })); val l = t.lessons.single { it.day == 4 }; assertEquals(6, l.lastPeriod); assertEquals(LocalTime.of(14, 15), l.end) }
    @Test fun twoDaysInOneCard() { val r = Fixtures.raw(); val t = build(r.copy(cards = r.cards.map { if (it.id == "c7") it.copy(days = "000011") else it })); assertEquals(8, t.lessons.size); assertTrue(t.lessons.any { it.day == 5 }) }
    @Test fun badPeriodTimeIsDropped() { val r = Fixtures.raw(); val t = build(r.copy(periods = r.periods.map { if (it.period == "1") it.copy(starttime = "x") else it })); assertEquals(5, t.lessons.size); assertTrue(t.lessons.none { it.start == LocalTime.of(8, 30) }) }
    @Test fun outOfRangePeriodTimeIsDropped() {
        val r = Fixtures.raw()
        val t = build(r.copy(periods = r.periods.map { if (it.period == "6") it.copy(endtime = "24:00") else it }))
        assertEquals(listOf(1, 2, 3, 4, 5), t.periods.map { it.number })
        assertEquals(6, t.lessons.size)
        assertTrue(t.lessons.none { it.day == 4 })
    }
}
