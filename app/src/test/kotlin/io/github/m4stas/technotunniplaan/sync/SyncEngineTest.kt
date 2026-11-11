package io.github.m4stas.technotunniplaan.sync

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.m4stas.technotunniplaan.FakeApi
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.edupage.EduPageDecoder
import io.github.m4stas.technotunniplaan.edupage.EduPageFormatException
import io.github.m4stas.technotunniplaan.edupage.ViewerEntry
import io.github.m4stas.technotunniplaan.schedule.SCHOOL_ZONE
import io.github.m4stas.technotunniplaan.schedule.buildTimetable
import io.github.m4stas.technotunniplaan.store.*
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SyncEngineTest {
    @get:Rule val tmp = TemporaryFolder()
    private val clock = Clock.fixed(Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE)
    private val viewer = EduPageDecoder.decodeViewer(Fixtures.text("viewer.json"))
    private val later = ViewerEntry("30", "Nädal12", "2026-11-15")

    private inner class Setup(scope: TestScope) {
        val dir = File(tmp.root, "cache")
        val cache = TimetableCache(dir)
        val repository = TimetableRepository(cache)
        val settings = SettingsStore(PreferenceDataStoreFactory.create(scope = scope.backgroundScope, produceFile = { File(tmp.root, "settings.preferences_pb") }))
        val api = FakeApi(viewer, mutableMapOf("22" to Fixtures.raw()))
        var changed = 0
        var onChanged: suspend () -> Unit = {}
        val engine = SyncEngine(api, repository, settings, clock) { changed++; onChanged() }
        fun cached() = TimetableCache(dir).readAll()
        val old = buildTimetable(Fixtures.raw(), Fixtures.MONDAY.minusWeeks(1), "Nädal10")
    }

    @Test fun downloadsCurrentWeek() = runTest {
        val s = Setup(this)
        assertEquals(SyncResult.Success, s.engine.sync())
        assertEquals(2026, s.api.requestedYear)
        assertEquals(listOf("22"), s.api.requested)
        assertEquals(listOf(Fixtures.MONDAY), s.cached().map { it.monday })
        assertEquals("Nädal11", s.cached().single().name)
        assertEquals(clock.millis(), s.settings.settings.first().lastSyncMillis)
        assertEquals(1, s.changed)
    }
    @Test fun downloadsLaterWeeksToo() = runTest {
        val s = Setup(this)
        s.api.entries = viewer + later
        s.api.timetables["30"] = Fixtures.rawWeek2()
        assertEquals(SyncResult.Success, s.engine.sync())
        assertEquals(listOf(Fixtures.MONDAY, Fixtures.MONDAY.plusWeeks(1)), s.cached().map { it.monday })
    }
    @Test fun deletesPastWeeks() = runTest {
        val s = Setup(this)
        s.repository.replace(listOf(s.old))
        s.engine.sync()
        assertEquals(listOf(Fixtures.MONDAY), s.cached().map { it.monday })
    }
    @Test fun networkFailureKeepsCache() = runTest {
        val s = Setup(this)
        s.repository.replace(listOf(Fixtures.week1()))
        s.settings.update { it.copy(lastSyncMillis = 123L) }
        s.api.failure = IOException()
        assertEquals(SyncResult.Failed(SyncError.NETWORK), s.engine.sync())
        assertEquals(listOf(Fixtures.week1()), s.cached())
        assertEquals(SyncError.NETWORK, s.settings.settings.first().lastSyncError)
        assertEquals(123L, s.settings.settings.first().lastSyncMillis)
        assertEquals(0, s.changed)
    }
    @Test fun formatFailureKeepsCache() = runTest {
        val s = Setup(this)
        s.repository.replace(listOf(Fixtures.week1()))
        s.api.failure = EduPageFormatException("x")
        assertEquals(SyncResult.Failed(SyncError.FORMAT), s.engine.sync())
        assertEquals(listOf(Fixtures.week1()), s.cached())
        assertEquals(SyncError.FORMAT, s.settings.settings.first().lastSyncError)
        assertEquals(0, s.changed)
    }
    @Test fun oneBadWeekWritesNothing() = runTest {
        val s = Setup(this)
        s.repository.replace(listOf(s.old))
        s.api.entries = viewer + later
        assertEquals(SyncResult.Failed(SyncError.FORMAT), s.engine.sync())
        assertEquals(listOf(s.old), s.cached())
    }
    @Test fun callbackErrorIsNotASyncFailure() = runTest {
        val s = Setup(this)
        s.onChanged = { throw IOException("callback") }
        val thrown = runCatching { s.engine.sync() }.exceptionOrNull()
        assertEquals("callback", (thrown as IOException).message)
        assertNull(s.settings.settings.first().lastSyncError)
        assertEquals(listOf(Fixtures.MONDAY), s.cached().map { it.monday })
    }
    @Test fun emptyListKeepsCache() = runTest {
        val s = Setup(this)
        s.repository.replace(listOf(Fixtures.week1()))
        s.api.entries = emptyList()
        assertEquals(SyncResult.Success, s.engine.sync())
        assertEquals(listOf(Fixtures.week1()), s.cached())
        assertEquals(clock.millis(), s.settings.settings.first().lastSyncMillis)
    }
    @Test fun successClearsOldError() = runTest {
        val s = Setup(this)
        s.settings.update { it.copy(lastSyncError = SyncError.NETWORK) }
        s.engine.sync()
        assertNull(s.settings.settings.first().lastSyncError)
    }
    @Test fun syncIfStale() = runTest {
        val s = Setup(this)
        s.settings.update { it.copy(lastSyncMillis = clock.millis() - 10 * 60_000) }
        assertNull(s.engine.syncIfStale())
        assertNull(s.api.requestedYear)
        s.settings.update { it.copy(lastSyncMillis = clock.millis() - 31 * 60_000) }
        assertEquals(SyncResult.Success, s.engine.syncIfStale())
    }
    @Test fun concurrentCallsDoNotOverlap() = runTest {
        val s = Setup(this)
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        s.api.onList = { calls++; if (calls == 1) gate.await() }
        val first = async { s.engine.sync() }
        runCurrent()
        val second = async { s.engine.sync() }
        runCurrent()
        assertEquals(1, calls)
        gate.complete(Unit)
        assertEquals(SyncResult.Success, first.await())
        assertEquals(SyncResult.Success, second.await())
        assertEquals(2, calls)
    }
}
