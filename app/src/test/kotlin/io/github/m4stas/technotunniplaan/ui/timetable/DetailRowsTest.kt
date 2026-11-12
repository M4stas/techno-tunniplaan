package io.github.m4stas.technotunniplaan.ui.timetable

import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import org.junit.Assert.assertEquals
import org.junit.Test

class DetailRowsTest {
    private val monday = Schedule(listOf(Fixtures.week1())).lessonsOn(Fixtures.CLASS_A, Fixtures.MONDAY)

    @Test fun teachersThenRoomsThenClassesWithoutTheViewer() {
        val rows = detailRows(daySlots(monday, Fixtures.CLASS_A, null)[1], Fixtures.CLASS_A)
        assertEquals(listOf("Jaan Proov · 102", "Tiit Katse · 103", "X-102", "X-103"), rows.map { it.value })
        assertEquals(listOf(R.string.detail_teacher_room, R.string.detail_teacher_room, R.string.kind_room, R.string.kind_room), rows.map { it.label })
        assertEquals(Fixtures.JAAN, rows[0].ref)
        assertEquals(EntityKind.ROOM, rows[2].ref.kind)
    }

    @Test fun teacherWithoutRoomUsesTeacherLabel() {
        val tuesday = Schedule(listOf(Fixtures.week1())).lessonsOn(Fixtures.CLASS_A, Fixtures.MONDAY.plusDays(1))
        val rows = detailRows(daySlots(tuesday, Fixtures.CLASS_A, null).first(), Fixtures.CLASS_A)
        assertEquals(listOf("Mari Näidis", "Jaan Proov"), rows.filter { it.ref.kind == EntityKind.TEACHER }.map { it.value })
        assertEquals(listOf(R.string.kind_teacher, R.string.kind_teacher), rows.filter { it.ref.kind == EntityKind.TEACHER }.map { it.label })
    }

    @Test fun anEntityInSeveralLessonsOfTheSlotGetsOneRow() {
        val english = daySlots(monday, Fixtures.CLASS_A, null)[1]
        assertEquals(listOf(Fixtures.CLASS_A), detailRows(english, Fixtures.ROOM_101).filter { it.ref.kind == EntityKind.CLASS }.map { it.ref })
    }

    @Test fun teacherViewerSeesTheRoomAndTheClass() {
        val maths = daySlots(monday, Fixtures.MARI, null)[0]
        assertEquals(listOf(Fixtures.ROOM_101, Fixtures.CLASS_A), detailRows(maths, Fixtures.MARI).map { it.ref })
    }
}
