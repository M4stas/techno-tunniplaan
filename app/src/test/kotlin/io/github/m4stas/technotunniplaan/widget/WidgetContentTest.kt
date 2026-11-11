package io.github.m4stas.technotunniplaan.widget

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.Fixtures.CLASS_A
import io.github.m4stas.technotunniplaan.Fixtures.MARI
import io.github.m4stas.technotunniplaan.schedule.LessonText
import io.github.m4stas.technotunniplaan.schedule.Schedule
import io.github.m4stas.technotunniplaan.schedule.DatedLesson
import io.github.m4stas.technotunniplaan.store.Settings
import java.time.DayOfWeek
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class WidgetContentTest {
    private val s = Schedule(listOf(Fixtures.week1()))
    private val mon = Fixtures.MONDAY

    @Test fun rendersLessonsForViewerAndDay() {
        val c = widgetContent(s, CLASS_A, mon.atTime(9, 0), 2) as WidgetContent.Lessons
        assertEquals(listOf(null, null), c.rows.map { it.day })
        assertEquals(LessonText("08:30–10:00", "Matemaatika", "101", "Mari Näidis", null), c.rows[0].text)
        val next = widgetContent(s, CLASS_A, mon.atTime(11, 0), 1) as WidgetContent.Lessons
        assertEquals(DayOfWeek.TUESDAY, next.rows.single().day)
        assertEquals("K-XX-24A", (widgetContent(s, MARI, mon.atTime(9, 0), 1) as WidgetContent.Lessons).rows[0].text.who)
    }

    @Test fun clampsCountAndHandlesEmptyStates() {
        assertEquals(5, (widgetContent(s, CLASS_A, mon.atTime(0, 0), 9) as WidgetContent.Lessons).rows.size)
        assertEquals(1, (widgetContent(s, CLASS_A, mon.atTime(0, 0), 0) as WidgetContent.Lessons).rows.size)
        assertEquals(WidgetContent.NoLessons, widgetContent(s, CLASS_A, mon.plusDays(4).atTime(14, 15), 3))
        assertEquals(WidgetContent.NoData, widgetContent(null, CLASS_A, mon.atTime(9, 0), 1))
        assertEquals(WidgetContent.NoData, widgetContent(Schedule(emptyList()), CLASS_A, mon.atTime(9, 0), 1))
        assertEquals(WidgetContent.NoData, widgetContent(s, null, mon.atTime(9, 0), 1))
    }

    @Test fun redrawsAtLessonEndOrMidnight() {
        assertEquals(mon.atTime(10, 0), nextRedrawAt(s.nextLessons(CLASS_A, mon.atTime(9, 0), 2), mon.atTime(9, 0)))
        assertEquals(mon.plusDays(1).atStartOfDay(), nextRedrawAt(s.nextLessons(CLASS_A, mon.atTime(11, 0), 1), mon.atTime(11, 0)))
        assertNull(nextRedrawAt(emptyList(), mon.atTime(9, 0)))
    }

    @Test fun fitsRowsAndResolvesEntity() {
        assertEquals(listOf(1, 2, 3, 5, 5), listOf(96, 110, 180, 250, 1000).map { rowsThatFit(it, 300) })
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

    @Test fun countsTallerRowsAtNarrowWidths() {
        for (width in listOf(110, 160, 239)) {
            assertEquals(80, widgetRowHeight(width, 96))
            assertEquals(listOf(1, 1, 2, 4, 5), listOf(96, 175, 176, 415, 416).map { rowsThatFit(it, width) })
        }
        for (width in listOf(240, 300, 600)) {
            assertEquals(44, widgetRowHeight(width, 60))
            assertEquals(listOf(1, 1, 2, 4, 5), listOf(60, 103, 104, 235, 236).map { rowsThatFit(it, width) })
        }
    }

    @Test fun fitsOneCellWidgetsInBothOrientations() {
        assertEquals(80, widgetRowHeight(130, 102))
        assertEquals(1, rowsThatFit(102, 130))
        for (width in listOf(110, 130, 240, 300)) {
            assertEquals(44, widgetRowHeight(width, 51))
            assertEquals(1, rowsThatFit(51, width))
            assertEquals(40, widgetRowHeight(width, 40))
            assertEquals(1, rowsThatFit(40, width))
            for (height in listOf(40, 51, 59, 60, 95, 96, 102, 176, 416)) {
                assertTrue(rowsThatFit(height, width) * widgetRowHeight(width, height) +
                    2 * widgetVerticalPadding(height) <= height)
            }
        }
    }
}
