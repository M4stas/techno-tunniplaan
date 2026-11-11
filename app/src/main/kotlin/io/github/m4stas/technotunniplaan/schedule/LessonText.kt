package io.github.m4stas.technotunniplaan.schedule

import java.time.format.DateTimeFormatter

data class LessonText(val time: String, val title: String, val place: String, val who: String, val subGroup: String?)
fun lessonText(lesson: Lesson, viewer: EntityRef): LessonText {
    val time = DateTimeFormatter.ofPattern("HH:mm")
    val place = if (viewer.kind == EntityKind.ROOM) lesson.classes else lesson.rooms
    val who = if (viewer.kind == EntityKind.TEACHER) lesson.classes else lesson.teachers
    val groups = lesson.subGroups.filter { viewer.kind != EntityKind.CLASS || it.classKey == viewer.key }
    return LessonText(
        "${lesson.start.format(time)}–${lesson.end.format(time)}",
        lesson.subject.ifBlank { "—" }, place.joinToString(", ") { it.short },
        who.joinToString(", ") { if (viewer.kind == EntityKind.TEACHER) it.short else it.name },
        groups.map { it.name }.joinToString("/").ifEmpty { null },
    )
}
fun Timetable.title(): String {
    val end = monday.plusDays(if (hasSaturday) 5 else 4)
    return "$name · ${monday.dayOfMonth}.${monday.monthValue}–${end.dayOfMonth}.${end.monthValue}"
}
