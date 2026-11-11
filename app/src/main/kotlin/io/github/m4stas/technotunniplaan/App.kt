package io.github.m4stas.technotunniplaan

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import io.github.m4stas.technotunniplaan.widget.WidgetUpdater
import io.github.m4stas.technotunniplaan.ui.settings.nightMode
import androidx.work.Configuration
import io.github.m4stas.technotunniplaan.sync.SyncScheduler
import io.github.m4stas.technotunniplaan.sync.SyncWorkerFactory
import java.io.IOException
import kotlinx.coroutines.*

class App : Application(), Configuration.Provider {
    lateinit var container: AppContainer
        private set
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(SyncWorkerFactory(container.sync)).build()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.onTimetableChanged = { try { WidgetUpdater.refresh(this) } catch (_: IOException) { } }
        scope.launch {
            container.settings.settings.map { Triple(it.home, it.theme, it.subGroupChoices) }
                .distinctUntilChanged().collect {
                    try { WidgetUpdater.refresh(this@App) } catch (_: IOException) { }
                }
        }
        AppCompatDelegate.setDefaultNightMode(nightMode(runBlocking { container.settings.settings.first().theme }))
        // Warm the cache early. AppRoot and the worker load it again if this fails.
        scope.launch { try { container.repository.ensureLoaded() } catch (e: IOException) { } }
        SyncScheduler.schedule(this)
    }
}
