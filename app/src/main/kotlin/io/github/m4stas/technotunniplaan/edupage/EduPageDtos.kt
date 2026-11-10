package io.github.m4stas.technotunniplaan.edupage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class EduPageFormatException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Serializable data class ViewerEntry(@SerialName("tt_num") val ttNum: String, val text: String = "", val datefrom: String = "", val hidden: Boolean = false)
@Serializable data class PeriodRow(val id: String, val period: String = "", val starttime: String = "", val endtime: String = "")
@Serializable data class NamedRow(val id: String, val name: String = "", val short: String = "")
@Serializable data class GroupRow(val id: String, val name: String = "", val classid: String = "", val entireclass: Boolean = false)
@Serializable data class DivisionRow(val id: String, val classid: String = "", val groupids: List<String> = emptyList())
@Serializable data class LessonRow(val id: String, val subjectid: String = "", val teacherids: List<String> = emptyList(), val groupids: List<String> = emptyList(), val classids: List<String> = emptyList(), val durationperiods: Int = 1)
@Serializable data class CardRow(val id: String, val lessonid: String = "", val period: String = "", val days: String = "", val classroomids: List<String> = emptyList())
data class RawTimetable(val periods: List<PeriodRow>, val classes: List<NamedRow>, val teachers: List<NamedRow>, val classrooms: List<NamedRow>, val subjects: List<NamedRow>, val groups: List<GroupRow>, val divisions: List<DivisionRow>, val lessons: List<LessonRow>, val cards: List<CardRow>)
