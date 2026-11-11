package io.github.m4stas.technotunniplaan

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import io.github.m4stas.technotunniplaan.edupage.EduPageClient
import io.github.m4stas.technotunniplaan.schedule.SCHOOL_ZONE
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.sync.SyncEngine
import java.io.File
import java.time.Clock
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    val clock: Clock = Clock.system(SCHOOL_ZONE)
    val settings = SettingsStore(PreferenceDataStoreFactory.create(corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }) { context.preferencesDataStoreFile("settings") })
    val repository = TimetableRepository(TimetableCache(File(context.filesDir, "timetables")))
    var onTimetableChanged: suspend () -> Unit = {}
    private val http = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).callTimeout(60, TimeUnit.SECONDS).build()
    val sync = SyncEngine(EduPageClient(http), repository, settings, clock) { onTimetableChanged() }
}
