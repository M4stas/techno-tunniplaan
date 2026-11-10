package io.github.m4stas.technotunniplaan.schedule

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.edupage.EduPageDecoder
import io.github.m4stas.technotunniplaan.edupage.ViewerEntry
import java.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals

class WeeksTest {
    private fun d(s: String) = LocalDate.parse(s)
    private val v = EduPageDecoder.decodeViewer(Fixtures.text("viewer.json"))

    @Test fun weekendMovesForwardOtherDaysBack() {
        assertEquals(d("2026-11-09"), correctToMonday(d("2026-11-08")))
        assertEquals(d("2026-11-09"), correctToMonday(d("2026-11-07")))
        assertEquals(d("2026-11-09"), correctToMonday(d("2026-11-09")))
        assertEquals(d("2026-11-09"), correctToMonday(d("2026-11-13")))
        assertEquals(d("2026-11-02"), correctToMonday(d("2026-11-01")))
    }
    @Test fun weekNameIsTextBeforeDates() {
        assertEquals("Nädal11", weekName("Nädal11 (08. 11. - 14. 11. 2026)")); assertEquals("Sügis", weekName("Sügis"))
    }
    @Test fun selectWeeksCases() {
        assertEquals(listOf(PublishedWeek("22", "Nädal11", d("2026-11-09"))), selectWeeks(v, d("2026-11-10")))
        assertEquals(listOf("19", "22"), selectWeeks(v, d("2026-11-03")).map { it.ttNum })
        assertEquals(listOf("22"), selectWeeks(v, d("2026-11-15")).map { it.ttNum })
        assertEquals(listOf("22"), selectWeeks(v, d("2026-11-17")).map { it.ttNum })
        assertEquals(listOf("19", "30"), selectWeeks(v.dropLast(1) + ViewerEntry("30", "Nädal12", "2026-11-15"), d("2026-11-10")).map { it.ttNum })
        assertEquals(listOf("25"), selectWeeks(v + ViewerEntry("25", "Nädal11 uus", "2026-11-09"), d("2026-11-10")).map { it.ttNum })
        assertEquals(listOf("19"), selectWeeks(listOf(v[0], v[1].copy(hidden = true)), d("2026-11-10")).map { it.ttNum })
        assertEquals(emptyList<PublishedWeek>(), selectWeeks(emptyList(), d("2026-11-10")))
        assertEquals(emptyList<PublishedWeek>(), selectWeeks(listOf(ViewerEntry("1", "x", "soon"), ViewerEntry("2", "y", "")), d("2026-11-10")))
    }
}
