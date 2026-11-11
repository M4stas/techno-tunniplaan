package io.github.m4stas.technotunniplaan.widget

import io.github.m4stas.technotunniplaan.schedule.DatedLesson
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.schedule.LessonText
import io.github.m4stas.technotunniplaan.schedule.Schedule
import io.github.m4stas.technotunniplaan.schedule.lessonText
import io.github.m4stas.technotunniplaan.store.Settings
import java.time.DayOfWeek
import java.time.LocalDateTime

data class WidgetConfig(val entity: EntityRef? = null, val count: Int = 1)
fun WidgetConfig.resolve(settings: Settings): EntityRef? = entity ?: settings.home

sealed interface WidgetContent {
    data object NoData : WidgetContent
    data object NoLessons : WidgetContent
    data class Lessons(val rows: List<WidgetRow>) : WidgetContent
}

data class WidgetRow(val day: DayOfWeek?, val text: LessonText)

fun widgetContent(schedule: Schedule?, entity: EntityRef?, now: LocalDateTime, count: Int): WidgetContent {
    if (schedule == null || schedule.weeks.isEmpty() || entity == null) return WidgetContent.NoData
    val upcoming = schedule.nextLessons(entity, now, count.coerceIn(1, 5))
    if (upcoming.isEmpty()) return WidgetContent.NoLessons
    return WidgetContent.Lessons(upcoming.map {
        WidgetRow(if (it.date == now.toLocalDate()) null else it.date.dayOfWeek, lessonText(it.lesson, entity))
    })
}

fun nextRedrawAt(upcoming: List<DatedLesson>, now: LocalDateTime): LocalDateTime? {
    val end = upcoming.firstOrNull()?.endsAt ?: return null
    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
    return if (upcoming.any { it.date > now.toLocalDate() } && midnight < end) midnight else end
}

fun rowsThatFit(heightDp: Int): Int = ((heightDp - 16) / 44).coerceIn(1, 5)
