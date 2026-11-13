package io.github.m4stas.technotunniplaan.ui.settings

import io.github.m4stas.technotunniplaan.schedule.SCHOOL_ZONE
import java.time.Instant

internal enum class SyncDay { TODAY, YESTERDAY, OTHER }
internal fun syncDay(synced: Instant, now: Instant): SyncDay {
    val date = synced.atZone(SCHOOL_ZONE).toLocalDate()
    val today = now.atZone(SCHOOL_ZONE).toLocalDate()
    return when (date) {
        today -> SyncDay.TODAY
        today.minusDays(1) -> SyncDay.YESTERDAY
        else -> SyncDay.OTHER
    }
}
