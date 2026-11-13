package io.github.m4stas.technotunniplaan.schedule

import io.github.m4stas.technotunniplaan.edupage.NamedRow
import io.github.m4stas.technotunniplaan.edupage.RawTimetable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

fun entityKey(name: String): String = name.lowercase(Locale.ROOT).trim().split(Regex("\\s+")).sorted().joinToString(" ")

fun buildTimetable(raw: RawTimetable, monday: LocalDate, name: String): Timetable {
    fun entities(rows: List<NamedRow>) = rows.filter { it.name.isNotBlank() }.associate { it.id to Entity(entityKey(it.name), it.name, it.short) }
    val classes = entities(raw.classes)
    val teachers = entities(raw.teachers)
    val rooms = entities(raw.classrooms)
    val subjects = raw.subjects.associateBy { it.id }
    val groups = raw.groups.associateBy { it.id }
    val lessonRows = raw.lessons.associateBy { it.id }
    val format = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT)
    val periods = raw.periods.mapNotNull { row ->
        val number = row.period.toIntOrNull() ?: return@mapNotNull null
        if (!row.starttime.matches(Regex("\\d{2}:\\d{2}")) || !row.endtime.matches(Regex("\\d{2}:\\d{2}"))) return@mapNotNull null
        runCatching { Period(number, LocalTime.parse(row.starttime, format), LocalTime.parse(row.endtime, format)) }.getOrNull()
    }
    val byNumber = periods.associateBy { it.number }
    val lessons = raw.cards.flatMap { card ->
        val first = card.period.toIntOrNull()?.let(byNumber::get)
        val row = lessonRows[card.lessonid]
        if (card.days.isBlank() || first == null || row == null) return@flatMap emptyList()
        val days = card.days.take(6).mapIndexedNotNull { index, c -> index.takeIf { c == '1' } }
        if (days.isEmpty()) return@flatMap emptyList()
        val last = (first.number.toLong() + row.durationperiods.coerceAtLeast(1).toLong() - 1).coerceAtMost(byNumber.keys.max().toLong()).toInt()
        val end = periods.filter { it.number <= last }.maxBy { it.number }.end
        val subject = subjects[row.subjectid]
        val subGroups = row.groupids.mapNotNull { id -> groups[id]?.takeUnless { it.entireclass }?.let { group -> classes[group.classid]?.let { SubGroup(it.key, group.name) } } }
        days.map { day -> Lesson(day, first.number, last, first.start, end, subject?.name.orEmpty(), subject?.short.orEmpty(), row.teacherids.mapNotNull(teachers::get), row.classids.mapNotNull(classes::get), card.classroomids.mapNotNull(rooms::get), subGroups) }
    }.sortedWith(compareBy<Lesson> { it.day }.thenBy { it.start }.thenBy { it.subject }.thenBy { it.subGroups.firstOrNull()?.name.orEmpty() })
    return Timetable(monday, name, periods, classes.values.distinctBy { it.key }, teachers.values.distinctBy { it.key }, rooms.values.distinctBy { it.key }, lessons)
}
