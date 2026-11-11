package io.github.m4stas.technotunniplaan

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.first
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
        AppCompatDelegate.setDefaultNightMode(nightMode(runBlocking { container.settings.settings.first().theme }))
        // Warm the cache early. AppRoot and the worker load it again if this fails.
        scope.launch { try { container.repository.ensureLoaded() } catch (e: IOException) { } }
        SyncScheduler.schedule(this)
    }
}
