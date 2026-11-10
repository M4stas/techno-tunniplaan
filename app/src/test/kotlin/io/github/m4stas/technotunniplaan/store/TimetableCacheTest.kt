package io.github.m4stas.technotunniplaan.store

import io.github.m4stas.technotunniplaan.Fixtures
import java.io.File
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TimetableCacheTest {
    @get:Rule val tmp = TemporaryFolder()

    @Test fun cacheRoundTripAndCorruption() {
        val dir = tmp.newFolder()
        val cache = TimetableCache(dir)
        cache.write(Fixtures.week2())
        cache.write(Fixtures.week1())
        assertEquals(listOf(Fixtures.week1(), Fixtures.week2()), cache.readAll())
        assertEquals(setOf("week-2026-11-09.json", "week-2026-11-16.json"), dir.list()!!.toSet())
        cache.retainOnly(setOf(Fixtures.MONDAY.plusWeeks(1)))
        assertEquals(listOf(Fixtures.week2()), cache.readAll())
        val corrupt = File(dir, "week-2026-11-09.json")
        corrupt.writeText("""{"monday":"2026-11-09","na""")
        assertEquals(listOf(Fixtures.week2()), cache.readAll())
        assertFalse(corrupt.exists())
        val invalid = File(dir, "week-2026-11-02.json")
        invalid.writeText("""{"version":9}""")
        assertEquals(listOf(Fixtures.week2()), cache.readAll())
        assertFalse(invalid.exists())
        assertTrue(TimetableCache(File(dir, "missing")).readAll().isEmpty())
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test fun repositoryLoadsAndReplaces() = runTest {
        val cache = TimetableCache(tmp.newFolder())
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repo = TimetableRepository(cache, dispatcher)
        assertNull(repo.weeks.value)
        repo.replace(listOf(Fixtures.week1()))
        assertEquals(listOf(Fixtures.week1()), repo.weeks.value)
        assertEquals(listOf(Fixtures.week1()), TimetableRepository(cache, dispatcher).ensureLoaded())
    }
}
