package io.github.m4stas.technotunniplaan.ui.settings

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncDayTest {
    private val now = Instant.parse("2026-11-12T22:30:00Z")
    @Test fun todayUsesSchoolDateAcrossUtcMidnight() {
        assertEquals(SyncDay.TODAY, syncDay(Instant.parse("2026-11-12T22:00:00Z"), now))
    }
    @Test fun yesterdayUsesSchoolDate() {
        assertEquals(SyncDay.YESTERDAY, syncDay(Instant.parse("2026-11-12T21:59:00Z"), now))
    }
    @Test fun olderUsesDate() {
        assertEquals(SyncDay.OTHER, syncDay(Instant.parse("2026-11-10T10:00:00Z"), now))
    }
}
