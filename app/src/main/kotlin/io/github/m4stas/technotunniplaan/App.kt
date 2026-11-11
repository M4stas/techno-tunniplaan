package io.github.m4stas.technotunniplaan

import android.app.Application
import androidx.work.Configuration
import io.github.m4stas.technotunniplaan.sync.SyncScheduler
import io.github.m4stas.technotunniplaan.sync.SyncWorkerFactory
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
        scope.launch { container.repository.ensureLoaded() }
        SyncScheduler.schedule(this)
    }
}
