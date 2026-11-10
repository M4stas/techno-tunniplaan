package io.github.m4stas.technotunniplaan

import io.github.m4stas.technotunniplaan.edupage.*

class FakeApi(var entries: List<ViewerEntry>, val timetables: MutableMap<String, RawTimetable>, var failure: Exception? = null) : EduPageApi {
    val requested = mutableListOf<String>()
    var requestedYear: Int? = null
    var onList: suspend () -> Unit = {}
    override suspend fun publishedTimetables(year: Int): List<ViewerEntry> {
        requestedYear = year
        onList()
        failure?.let { throw it }
        return entries
    }
    override suspend fun timetable(ttNum: String): RawTimetable {
        requested.add(ttNum)
        failure?.let { throw it }
        return timetables[ttNum] ?: throw EduPageFormatException("Unknown timetable")
    }
}
