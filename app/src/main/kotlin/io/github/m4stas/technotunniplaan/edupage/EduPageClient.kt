package io.github.m4stas.technotunniplaan.edupage

import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

const val EDUPAGE_BASE_URL = "https://kesklinn-techno.edupage.org"

interface EduPageApi {
    suspend fun publishedTimetables(year: Int): List<ViewerEntry>
    suspend fun timetable(ttNum: String): RawTimetable
}

class EduPageClient(private val http: OkHttpClient, private val baseUrl: String = EDUPAGE_BASE_URL) : EduPageApi {
    override suspend fun publishedTimetables(year: Int): List<ViewerEntry> = withContext(Dispatchers.IO) {
        EduPageDecoder.decodeViewer(post("/timetable/server/ttviewer.js?__func=getTTViewerData", JsonPrimitive(year)))
    }

    override suspend fun timetable(ttNum: String): RawTimetable = withContext(Dispatchers.IO) {
        EduPageDecoder.decodeTimetable(post("/timetable/server/regulartt.js?__func=regularttGetData", JsonPrimitive(ttNum)))
    }

    private fun post(path: String, argument: JsonElement): String {
        val json = buildJsonObject {
            put("__args", JsonArray(listOf(JsonNull, argument)))
            put("__gsh", JsonPrimitive("00000000"))
        }
        val request = Request.Builder()
            .url(baseUrl + path)
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()
        return http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            response.body.string()
        }
    }
}
