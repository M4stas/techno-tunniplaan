package io.github.m4stas.technotunniplaan.ui.timetable

import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DayLabelsTest {
    private val wednesday = LocalDate.of(2026, 11, 11)

    @Test fun estonian() {
        val et = Locale.forLanguageTag("et")
        assertEquals("Kolmapäev", dayTitle(wednesday, et))
        assertEquals("11. november", dateLabel(wednesday, "d. MMMM", et))
        assertEquals("11.11", dateLabel(wednesday, "d.MM", et))
    }

    @Test fun english() {
        assertEquals("Wednesday", dayTitle(wednesday, Locale.ENGLISH))
        assertEquals("11 November", dateLabel(wednesday, "d MMMM", Locale.ENGLISH))
        assertEquals("11 Nov", dateLabel(wednesday, "d MMM", Locale.ENGLISH))
    }
}
