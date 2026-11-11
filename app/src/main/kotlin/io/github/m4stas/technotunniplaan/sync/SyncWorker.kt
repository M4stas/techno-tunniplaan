package io.github.m4stas.technotunniplaan.sync

import android.content.Context
import androidx.work.*
import io.github.m4stas.technotunniplaan.store.SyncError

class SyncWorker(context: Context, params: WorkerParameters, private val engine: SyncEngine) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = when (engine.sync()) {
        SyncResult.Failed(SyncError.NETWORK) -> Result.retry()
        else -> Result.success()
    }
}

class SyncWorkerFactory(private val engine: SyncEngine) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
        if (workerClassName == SyncWorker::class.java.name) SyncWorker(appContext, workerParameters, engine) else null
}
