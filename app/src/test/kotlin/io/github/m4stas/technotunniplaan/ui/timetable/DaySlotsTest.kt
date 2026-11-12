package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class DaySlotsTest {
    private val monday = Schedule(listOf(Fixtures.week1())).lessonsOn(Fixtures.CLASS_A, Fixtures.MONDAY)

    @Test fun groupsParallelLessonsIntoOneSlot() {
        val slots = daySlots(monday, Fixtures.CLASS_A, LocalTime.of(9, 0))
        assertEquals(2, slots.size)
        assertEquals(SlotStatus.CURRENT, slots[0].status)
        assertEquals(1f / 3f, slots[0].progress, 0.001f)
        assertNull(slots[0].breakBefore)
        assertEquals(listOf("Matemaatika"), slots[0].lines.map { it.subject })
        assertEquals("Mari Näidis", slots[0].lines[0].who)
        assertEquals("101", slots[0].lines[0].place)
        assertEquals(SlotStatus.UPCOMING, slots[1].status)
        assertEquals(0f, slots[1].progress, 0f)
        assertEquals(BreakUi(15, BreakKind.BREAK), slots[1].breakBefore)
        assertEquals(LocalTime.of(10, 15), slots[1].start)
        assertEquals(LocalTime.of(11, 0), slots[1].end)
        assertTrue(slots[1].sameSubject)
        assertEquals(listOf("Jaan Proov", "Tiit Katse"), slots[1].lines.map { it.who })
        assertEquals(listOf("102", "103"), slots[1].lines.map { it.place })
        assertEquals(listOf(null, null), slots[1].lines.map { it.time })
    }

    @Test fun statusFollowsTheClock() {
        assertEquals(listOf(SlotStatus.PAST, SlotStatus.UPCOMING), daySlots(monday, Fixtures.CLASS_A, LocalTime.of(10, 5)).map { it.status })
        assertEquals(listOf(SlotStatus.PAST, SlotStatus.PAST), daySlots(monday, Fixtures.CLASS_A, LocalTime.of(11, 0)).map { it.status })
        assertEquals(listOf(SlotStatus.UPCOMING, SlotStatus.UPCOMING), daySlots(monday, Fixtures.CLASS_A, null).map { it.status })
        assertEquals(SlotStatus.CURRENT, daySlots(monday, Fixtures.CLASS_A, LocalTime.of(8, 30))[0].status)
    }

    @Test fun breakKinds() {
        fun gap(minutes: Long) = breakBetween(LocalTime.of(10, 0), LocalTime.of(10, 0).plusMinutes(minutes))
        assertNull(gap(0))
        assertEquals(BreakUi(15, BreakKind.BREAK), gap(15))
        assertEquals(BreakUi(29, BreakKind.BREAK), gap(29))
        assertEquals(BreakUi(30, BreakKind.LUNCH), gap(30))
        assertEquals(BreakUi(60, BreakKind.LUNCH), gap(60))
        assertEquals(BreakUi(61, BreakKind.FREE), gap(61))
    }

    @Test fun unevenAndMixedSlots() {
        val english = monday.filter { it.start == LocalTime.of(10, 15) }
        val longer = english[1].copy(end = LocalTime.of(11, 45))
        val uneven = daySlots(listOf(english[0], longer), Fixtures.CLASS_A, null).single()
        assertEquals(LocalTime.of(11, 45), uneven.end)
        assertEquals(listOf("10:15–11:00", null), uneven.lines.map { it.time })
        val mixed = daySlots(listOf(english[0], english[1].copy(subject = "Programmeerimine")), Fixtures.CLASS_A, null).single()
        assertFalse(mixed.sameSubject)
    }

    @Test fun teacherSeesTheClassAndRoom() {
        val line = daySlots(monday, Fixtures.MARI, null)[0].lines[0]
        assertEquals("K-XX-24A", line.who)
        assertEquals("101", line.place)
    }
}
