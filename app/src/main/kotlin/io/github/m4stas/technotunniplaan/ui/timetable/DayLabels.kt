package io.github.m4stas.technotunniplaan.ui.timetable

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** The weekday name with a capital first letter. Estonian weekday names are lower case in the locale data. */
fun dayTitle(date: LocalDate, locale: Locale): String =
    date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }

/** [pattern] comes from the `date_pattern` or `short_date_pattern` string resource. */
fun dateLabel(date: LocalDate, pattern: String, locale: Locale): String = date.format(DateTimeFormatter.ofPattern(pattern, locale))
