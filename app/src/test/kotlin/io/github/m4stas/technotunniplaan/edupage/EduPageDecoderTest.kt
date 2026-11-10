package io.github.m4stas.technotunniplaan.edupage

import io.github.m4stas.technotunniplaan.Fixtures
import org.junit.Test
import org.junit.Assert.*

class EduPageDecoderTest {
    @Test fun viewerEntries() {
        val e = EduPageDecoder.decodeViewer(Fixtures.text("viewer.json"))
        assertEquals(listOf("19", "22"), e.map { it.ttNum })
        assertEquals("2026-11-08", e[1].datefrom); assertFalse(e[1].hidden)
        assertEquals("Nädal11 (08. 11. - 14. 11. 2026)", e[1].text)
    }
    @Test fun timetableTables() {
        val t = Fixtures.raw()
        assertEquals(listOf(6, 2, 3, 3, 4, 6, 4, 7, 8), listOf(t.periods, t.classes, t.teachers, t.classrooms, t.subjects, t.groups, t.divisions, t.lessons, t.cards).map { it.size })
        assertEquals(CardRow("c1", "*1", "1", "100000", listOf("-20")), t.cards[0])
        assertEquals(2, t.lessons[0].durationperiods); assertTrue(t.groups[0].entireclass)
    }
    @Test fun errorInsideR() { assertThrows(EduPageFormatException::class.java) { EduPageDecoder.decodeTimetable("""{"r":{"rights":{},"error":"Timetable does not exists"}}""") } }
    @Test fun errorWithoutR() { assertThrows(EduPageFormatException::class.java) { EduPageDecoder.decodeViewer("""{"e":"x","em":"y"}""") } }
    @Test fun htmlBody() { assertThrows(EduPageFormatException::class.java) { EduPageDecoder.decodeViewer("<html>busy</html>") } }
    @Test fun missingTableIsEmpty() { assertEquals(emptyList<CardRow>(), EduPageDecoder.decodeTimetable("""{"r":{"dbiAccessorRes":{"tables":[]}}}""").cards) }
}
