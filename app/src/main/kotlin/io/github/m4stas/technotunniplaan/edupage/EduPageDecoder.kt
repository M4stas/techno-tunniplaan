package io.github.m4stas.technotunniplaan.edupage

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object EduPageDecoder {
    private val json = Json { ignoreUnknownKeys = true }

    fun decodeViewer(body: String): List<ViewerEntry> = decode {
        val r = response(body)
        val entries = r["regular"]?.jsonObject?.get("timetables")
            ?: throw EduPageFormatException("Missing r.regular.timetables")
        json.decodeFromJsonElement<List<ViewerEntry>>(entries)
    }

    fun decodeTimetable(body: String): RawTimetable = decode {
        val r = response(body)
        val tables = r["dbiAccessorRes"]?.jsonObject?.get("tables")
            ?: throw EduPageFormatException("Missing r.dbiAccessorRes.tables")
        val rows = json.decodeFromJsonElement<List<JsonObject>>(tables)
        RawTimetable(
            table(rows, "periods"), table(rows, "classes"), table(rows, "teachers"),
            table(rows, "classrooms"), table(rows, "subjects"), table(rows, "groups"),
            table(rows, "divisions"), table(rows, "lessons"), table(rows, "cards"),
        )
    }

    private fun response(body: String): JsonObject {
        val root = json.parseToJsonElement(body).jsonObject
        val r = root["r"]?.jsonObject ?: throw EduPageFormatException("Missing r")
        if ("error" in r) {
            val error = r.getValue("error")
            throw EduPageFormatException(
                (error as? kotlinx.serialization.json.JsonPrimitive)?.content ?: error.toString(),
            )
        }
        return r
    }

    private inline fun <reified T> table(tables: List<JsonObject>, id: String): List<T> {
        val table = tables.firstOrNull { it["id"]?.jsonPrimitive?.content == id }
            ?: return emptyList()
        val rows = table["data_rows"] ?: throw EduPageFormatException("Missing data_rows for $id")
        return json.decodeFromJsonElement<List<T>>(rows)
    }

    private inline fun <T> decode(block: () -> T): T = try {
        block()
    } catch (cause: SerializationException) {
        throw EduPageFormatException("Invalid EduPage response", cause)
    } catch (cause: IllegalArgumentException) {
        throw EduPageFormatException("Invalid EduPage response", cause)
    }
}
