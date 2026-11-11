package io.github.m4stas.technotunniplaan.store

import io.github.m4stas.technotunniplaan.schedule.Timetable
import java.io.File
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimetableCache(private val dir: File) {
    private val json = Json { ignoreUnknownKeys = true }
    private fun weekFiles() = dir.listFiles()?.filter { it.isFile && it.name.startsWith("week-") && it.name.endsWith(".json") }.orEmpty()

    fun write(timetable: Timetable) {
        check(dir.isDirectory || dir.mkdirs())
        val temp = File.createTempFile("week-", ".tmp", dir)
        try {
            temp.writeText(json.encodeToString(timetable))
            check(temp.renameTo(File(dir, "week-${timetable.monday}.json")))
        } finally {
            temp.delete()
        }
    }

    fun readAll(): List<Timetable> = weekFiles().mapNotNull { file ->
        try {
            json.decodeFromString<Timetable>(file.readText())
        } catch (e: IllegalArgumentException) {
            file.delete()
            null
        }
    }.sortedBy { it.monday }

    fun retainOnly(mondays: Set<LocalDate>) {
        val names = mondays.map { "week-$it.json" }.toSet()
        weekFiles().filter { it.name !in names }.forEach { it.delete() }
    }
}
