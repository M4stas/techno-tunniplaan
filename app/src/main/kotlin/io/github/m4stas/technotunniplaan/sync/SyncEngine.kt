package io.github.m4stas.technotunniplaan.sync

import io.github.m4stas.technotunniplaan.edupage.EduPageApi
import io.github.m4stas.technotunniplaan.edupage.EduPageFormatException
import io.github.m4stas.technotunniplaan.schedule.SCHOOL_ZONE
import io.github.m4stas.technotunniplaan.schedule.buildTimetable
import io.github.m4stas.technotunniplaan.schedule.selectWeeks
import io.github.m4stas.technotunniplaan.store.SettingsStore
import io.github.m4stas.technotunniplaan.store.SyncError
import io.github.m4stas.technotunniplaan.store.TimetableRepository
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SyncResult {
    data object Success : SyncResult
    data class Failed(val error: SyncError) : SyncResult
}

class SyncEngine(
    private val api: EduPageApi, private val repository: TimetableRepository, private val settings: SettingsStore,
    private val clock: Clock, private val onChanged: suspend () -> Unit = {},
) {
    private val mutex = Mutex()

    suspend fun sync(): SyncResult = mutex.withLock {
        val weeks = try {
            val today = LocalDate.now(clock.withZone(SCHOOL_ZONE))
            selectWeeks(api.publishedTimetables(today.year), today)
                .map { buildTimetable(api.timetable(it.ttNum), it.monday, it.name) }
        } catch (e: IOException) {
            return@withLock fail(SyncError.NETWORK)
        } catch (e: EduPageFormatException) {
            return@withLock fail(SyncError.FORMAT)
        }
        if (weeks.isNotEmpty()) repository.replace(weeks)
        settings.update { it.copy(lastSyncMillis = clock.millis(), lastSyncError = null) }
        onChanged()
        SyncResult.Success
    }

    private suspend fun fail(error: SyncError): SyncResult {
        settings.update { it.copy(lastSyncError = error) }
        return SyncResult.Failed(error)
    }

    suspend fun syncIfStale(maxAge: Duration = Duration.ofMinutes(30)): SyncResult? {
        val lastSync = settings.settings.first().lastSyncMillis
        if (lastSync != null && clock.millis() - lastSync < maxAge.toMillis()) return null
        return sync()
    }
}
