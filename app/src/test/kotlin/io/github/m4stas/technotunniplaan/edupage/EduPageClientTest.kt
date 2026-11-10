package io.github.m4stas.technotunniplaan.edupage

import io.github.m4stas.technotunniplaan.Fixtures
import java.io.IOException
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EduPageClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: EduPageClient

    @Before fun setUp() {
        server = MockWebServer()
        server.start()
        client = EduPageClient(OkHttpClient(), server.url("/").toString().trimEnd('/'))
    }

    @After fun tearDown() = server.close()

    private fun body(text: String) = MockResponse.Builder().body(text).build()
    private fun status(code: Int) = MockResponse.Builder().code(code).build()

    @Test fun viewerRequest() = runTest {
        server.enqueue(body(Fixtures.text("viewer.json")))
        assertEquals(2, client.publishedTimetables(2026).size)
        val r = server.takeRequest()
        assertEquals("POST", r.method)
        assertEquals("/timetable/server/ttviewer.js?__func=getTTViewerData", r.target)
        assertEquals("""{"__args":[null,2026],"__gsh":"00000000"}""", r.body!!.utf8())
        assertTrue(r.headers["Content-Type"]!!.startsWith("application/json"))
    }

    @Test fun timetableRequest() = runTest {
        server.enqueue(body(Fixtures.text("timetable.json")))
        assertEquals(8, client.timetable("22").cards.size)
        val r = server.takeRequest()
        assertEquals("/timetable/server/regulartt.js?__func=regularttGetData", r.target)
        assertEquals("""{"__args":[null,"22"],"__gsh":"00000000"}""", r.body!!.utf8())
    }

    @Test fun numberIsEscaped() = runTest {
        server.enqueue(body(Fixtures.text("timetable.json")))
        client.timetable("2\"2")
        assertEquals("""{"__args":[null,"2\"2"],"__gsh":"00000000"}""", server.takeRequest().body!!.utf8())
    }

    @Test fun http500() = runTest {
        server.enqueue(status(500))
        assertFailsWith<IOException> { client.timetable("22") }
    }

    @Test fun errorBody() = runTest {
        server.enqueue(body("""{"r":{"error":"Timetable does not exists"}}"""))
        assertFailsWith<EduPageFormatException> { client.timetable("99999") }
    }

    @Test fun serverDown() = runTest {
        server.close()
        assertFailsWith<IOException> { client.publishedTimetables(2026) }
    }
}
