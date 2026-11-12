package io.github.m4stas.technotunniplaan.schedule

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

val SCHOOL_ZONE: ZoneId = ZoneId.of("Europe/Tallinn")
enum class EntityKind { CLASS, TEACHER, ROOM }
@Serializable data class EntityRef(val kind: EntityKind, val key: String)
@Serializable data class Entity(val key: String, val name: String, val short: String)
@Serializable data class Period(val number: Int, @Serializable(with = LocalTimeSerializer::class) val start: LocalTime, @Serializable(with = LocalTimeSerializer::class) val end: LocalTime)
@Serializable data class SubGroup(val classKey: String, val name: String)
@Serializable data class Split(val classKey: String, val groups: List<String>) { val id: String get() = groups.joinToString("|") }
@Serializable data class Lesson(val day: Int, val firstPeriod: Int, val lastPeriod: Int, @Serializable(with = LocalTimeSerializer::class) val start: LocalTime, @Serializable(with = LocalTimeSerializer::class) val end: LocalTime, val subject: String, val subjectShort: String, val teachers: List<Entity>, val classes: List<Entity>, val rooms: List<Entity>, val subGroups: List<SubGroup>)
@Serializable data class Timetable(@Serializable(with = LocalDateSerializer::class) val monday: LocalDate, val name: String, val periods: List<Period>, val classes: List<Entity>, val teachers: List<Entity>, val rooms: List<Entity>, val splits: List<Split>, val lessons: List<Lesson>)
