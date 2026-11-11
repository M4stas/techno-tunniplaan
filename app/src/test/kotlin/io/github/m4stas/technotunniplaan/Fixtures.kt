package io.github.m4stas.technotunniplaan

import io.github.m4stas.technotunniplaan.edupage.EduPageDecoder
import io.github.m4stas.technotunniplaan.edupage.RawTimetable
import io.github.m4stas.technotunniplaan.schedule.*
import java.time.LocalDate

object Fixtures {
    val CLASS_A = EntityRef(EntityKind.CLASS, "k-xx-24a")
    val CLASS_B = EntityRef(EntityKind.CLASS, "k-xx-24b")
    val MARI = EntityRef(EntityKind.TEACHER, "mari näidis")
    val JAAN = EntityRef(EntityKind.TEACHER, "jaan proov")
    val TIIT = EntityRef(EntityKind.TEACHER, "katse tiit")
    val ROOM_101 = EntityRef(EntityKind.ROOM, "x-101")
    fun week1() = buildTimetable(raw(), MONDAY, "Nädal11")
    fun week2() = buildTimetable(rawWeek2(), MONDAY.plusWeeks(1), "Nädal12")
    val MONDAY: LocalDate = LocalDate.of(2026, 11, 9)

    fun text(name: String): String =
        requireNotNull(Fixtures::class.java.getResource("/fixtures/$name")) {
            "Missing fixture $name"
        }.readText(Charsets.UTF_8)

    fun raw(): RawTimetable = EduPageDecoder.decodeTimetable(text("timetable.json"))

    fun rawWeek2(): RawTimetable {
        val raw = raw()
        val ids = raw.teachers.associate { it.id to it.id.replaceFirst("-", "-1") }
        return raw.copy(
            teachers = raw.teachers.map {
                val given = it.name.substringBefore(" ")
                val surname = it.name.substringAfter(" ")
                it.copy(id = ids.getValue(it.id), name = "$surname $given", short = surname)
            },
            lessons = raw.lessons.map {
                it.copy(teacherids = it.teacherids.map(ids::getValue))
            },
        )
    }
}
