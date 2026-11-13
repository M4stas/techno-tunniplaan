package io.github.m4stas.technotunniplaan.widget

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.Fixtures.CLASS_A
import io.github.m4stas.technotunniplaan.Fixtures.MARI
import io.github.m4stas.technotunniplaan.schedule.LessonText
import io.github.m4stas.technotunniplaan.schedule.Schedule
import io.github.m4stas.technotunniplaan.schedule.DatedLesson
import io.github.m4stas.technotunniplaan.store.Settings
import io.github.m4stas.technotunniplaan.ui.timetable.*
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class WidgetContentTest {
    private val s = Schedule(listOf(Fixtures.week1()))
    private val mon = Fixtures.MONDAY

    @Test fun rendersLessonsForViewerAndDay() {
        val c = widgetContent(s, CLASS_A, mon.atTime(9, 0), 2) as WidgetContent.Lessons
        assertEquals(SlotStatus.CURRENT, c.lessons[0].status)
        assertEquals(1f / 3, c.lessons[0].progress, 0.001f)
        assertEquals(SlotStatus.UPCOMING, c.lessons[1].status)
        assertEquals(breakBetween(LocalTime.of(10, 0), c.lessons[1].start), c.lessons[1].breakBefore)
        assertTrue(c.lessons.none { it.newDay })
        assertEquals(s.find(CLASS_A)!!.name, c.entityName)
        assertEquals(LessonText("08:30–10:00", "Matemaatika", "101", "Mari Näidis", null), c.lessons[0].text)
        val next = widgetContent(s, CLASS_A, mon.atTime(11, 0), 1) as WidgetContent.Lessons
        assertEquals(mon.plusDays(1), next.lessons.single().date)
        assertEquals("K-XX-24A", (widgetContent(s, MARI, mon.atTime(9, 0), 1) as WidgetContent.Lessons).lessons[0].text.who)
    }

    @Test fun clampsCountAndHandlesEmptyStates() {
        assertEquals(5, (widgetContent(s, CLASS_A, mon.atTime(0, 0), 9) as WidgetContent.Lessons).lessons.size)
        assertEquals(1, (widgetContent(s, CLASS_A, mon.atTime(0, 0), 0) as WidgetContent.Lessons).lessons.size)
        assertEquals(WidgetContent.NoLessons, widgetContent(s, CLASS_A, mon.plusDays(4).atTime(14, 15), 3))
        assertEquals(WidgetContent.NoData, widgetContent(null, CLASS_A, mon.atTime(9, 0), 1))
        assertEquals(WidgetContent.NoData, widgetContent(Schedule(emptyList()), CLASS_A, mon.atTime(9, 0), 1))
        assertEquals(WidgetContent.NoData, widgetContent(s, null, mon.atTime(9, 0), 1))
    }

    @Test fun redrawsAtLessonEndOrMidnight() {
        assertEquals(mon.atTime(9, 5), nextRedrawAt(s.nextLessons(CLASS_A, mon.atTime(9, 0), 2), mon.atTime(9, 0)))
        assertEquals(mon.plusDays(1).atStartOfDay(), nextRedrawAt(s.nextLessons(CLASS_A, mon.atTime(11, 0), 1), mon.atTime(11, 0)))
        assertNull(nextRedrawAt(emptyList(), mon.atTime(9, 0)))
    }

    @Test fun resolvesEntity() {
        assertEquals(CLASS_A, WidgetConfig().resolve(Settings(home = CLASS_A)))
        assertEquals(MARI, WidgetConfig(MARI).resolve(Settings(home = CLASS_A)))
    }

    @Test fun redrawsWhenTheShorterOverlappingLessonEnds() {
        val lesson = s.nextLessons(CLASS_A, mon.atTime(9, 14), 1).single().lesson
        val long = DatedLesson(mon, lesson.copy(start = LocalTime.of(8, 30), end = LocalTime.of(10, 0)))
        val short = DatedLesson(mon, lesson.copy(start = LocalTime.of(8, 30), end = LocalTime.of(9, 15)))
        assertEquals(mon.atTime(9, 15), nextRedrawAt(listOf(long, short), mon.atTime(9, 14)))
        assertEquals(mon.atTime(9, 15), nextRedrawAt(listOf(short, long), mon.atTime(9, 14)))
    }

    @Test fun layoutThresholds() {
        assertEquals(listOf(WidgetLayout.SINGLE, WidgetLayout.NOW_NEXT, WidgetLayout.NOW_NEXT, WidgetLayout.TIMELINE), listOf(169, 170, 229, 230).map(::widgetLayout))
        assertTrue(isNarrowWidget(239))
        assertFalse(isNarrowWidget(240))
    }

    @Test fun overlapsAndDayChanges() {
        val lessons = (widgetContent(s, CLASS_A, mon.atTime(10, 30), 4) as WidgetContent.Lessons).lessons
        assertNull(lessons[1].breakBefore)
        assertTrue(lessons[2].newDay)
        assertNull(lessons[2].breakBefore)
    }

    @Test fun breaksFollowTheLatestOverlappingEnd() {
        val week = Fixtures.week1()
        val template = s.nextLessons(CLASS_A, mon.atTime(8, 45), 1).single().lesson
        val long = template.copy(start = LocalTime.of(8, 30), end = LocalTime.of(10, 0))
        val short = long.copy(end = LocalTime.of(9, 0))
        for (overlaps in listOf(listOf(long, short), listOf(short, long))) {
            for ((start, expected) in listOf(
                LocalTime.of(10, 15) to BreakUi(15, BreakKind.BREAK),
                LocalTime.of(9, 45) to null,
                LocalTime.of(10, 0) to null,
            )) {
                val schedule = Schedule(listOf(week.copy(lessons = overlaps +
                    template.copy(start = start, end = LocalTime.of(11, 0)))))
                val lessons = (widgetContent(schedule, CLASS_A, mon.atTime(8, 45), 3)
                    as WidgetContent.Lessons).lessons
                assertEquals(3, lessons.size)
                assertNull(lessons[0].breakBefore)
                assertNull(lessons[1].breakBefore)
                assertEquals(expected, lessons[2].breakBefore)
                assertEquals(if (expected == null) 3 else 2, timelineRowsThatFit(190, lessons))
            }
        }
    }

    @Test fun breaksResetOnTheNextDay() {
        val week = Fixtures.week1()
        val template = s.nextLessons(CLASS_A, mon.atTime(8, 45), 1).single().lesson
        val first = template.copy(start = LocalTime.of(8, 30), end = LocalTime.of(12, 0))
        val nextDay = first.copy(day = 1, end = LocalTime.of(9, 0))
        val schedule = Schedule(listOf(week.copy(lessons = listOf(
            first, first.copy(end = LocalTime.of(9, 0)), nextDay,
            nextDay.copy(start = LocalTime.of(9, 15), end = LocalTime.of(10, 0)),
        ))))
        val lessons = (widgetContent(schedule, CLASS_A, mon.atTime(8, 45), 4)
            as WidgetContent.Lessons).lessons
        assertEquals(4, lessons.size)
        assertTrue(lessons[2].newDay)
        assertNull(lessons[2].breakBefore)
        assertFalse(lessons[3].newDay)
        assertEquals(BreakUi(15, BreakKind.BREAK), lessons[3].breakBefore)
    }

    @Test fun timelineFitsLabelsAndRows() {
        val first = (widgetContent(s, CLASS_A, mon.atTime(9, 0), 1) as WidgetContent.Lessons).lessons.single()
        val gap = first.copy(breakBefore = breakBetween(LocalTime.of(10, 0), LocalTime.of(10, 15)))
        val lessons = listOf(first, gap, first)
        assertEquals(2, timelineRowsThatFit(164, lessons))
        assertEquals(2, timelineRowsThatFit(209, lessons))
        assertEquals(1, timelineRowsThatFit(169, listOf(first, first.copy(newDay = true))))
        assertEquals(2, timelineRowsThatFit(170, listOf(first, first.copy(newDay = true))))
        assertEquals(1, timelineRowsThatFit(0, lessons))
        assertEquals(3, timelineRowsThatFit(1000, lessons))
        assertEquals(0, timelineRowsThatFit(1000, emptyList()))
    }

    @Test fun redrawsAtNearerEnd() {
        val now = mon.atTime(9, 57)
        assertEquals(mon.atTime(10, 0), nextRedrawAt(s.nextLessons(CLASS_A, now, 2), now))
    }
}
