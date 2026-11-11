package io.github.m4stas.technotunniplaan.schedule

import io.github.m4stas.technotunniplaan.edupage.ViewerEntry
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters

data class PublishedWeek(val ttNum: String, val name: String, val monday: LocalDate)

fun correctToMonday(date: LocalDate): LocalDate = when (date.dayOfWeek) {
    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> date.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    else -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

fun weekName(text: String): String = text.substringBefore(" (").trim()

fun selectWeeks(entries: List<ViewerEntry>, today: LocalDate): List<PublishedWeek> {
    val weeks = entries.filterNot { it.hidden }.mapNotNull { entry ->
        val date = try { LocalDate.parse(entry.datefrom) } catch (_: DateTimeParseException) { null }
        date?.let { PublishedWeek(entry.ttNum, weekName(entry.text), correctToMonday(it)) }
    }.associateBy { it.monday }.values.sortedBy { it.monday }
    val current = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val upcoming = weeks.filter { it.monday >= current }
    return if (upcoming.any { it.monday == current }) upcoming
    else listOfNotNull(weeks.lastOrNull { it.monday < current }) + upcoming
}
