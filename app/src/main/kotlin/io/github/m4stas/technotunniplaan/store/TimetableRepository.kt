package io.github.m4stas.technotunniplaan.store

import io.github.m4stas.technotunniplaan.schedule.Timetable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TimetableRepository(private val cache: TimetableCache, private val io: CoroutineDispatcher = Dispatchers.IO) {
    private val mutex = Mutex()
    private val state = MutableStateFlow<List<Timetable>?>(null)
    val weeks: StateFlow<List<Timetable>?> = state

    suspend fun ensureLoaded(): List<Timetable> = mutex.withLock {
        state.value ?: withContext(io) { cache.readAll() }.also { state.value = it }
    }

    suspend fun replace(weeks: List<Timetable>) = mutex.withLock {
        val sorted = weeks.sortedBy { it.monday }
        withContext(io) {
            sorted.forEach(cache::write)
            cache.retainOnly(sorted.map { it.monday }.toSet())
        }
        state.value = sorted
    }
}
