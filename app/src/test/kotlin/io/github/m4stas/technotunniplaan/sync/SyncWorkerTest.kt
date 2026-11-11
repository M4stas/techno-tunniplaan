package io.github.m4stas.technotunniplaan.sync

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import io.github.m4stas.technotunniplaan.FakeApi
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.edupage.EduPageDecoder
import io.github.m4stas.technotunniplaan.edupage.EduPageFormatException
import io.github.m4stas.technotunniplaan.schedule.SCHOOL_ZONE
import io.github.m4stas.technotunniplaan.store.*
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SyncWorkerTest {
    @get:Rule val tmp = TemporaryFolder()
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun workerWith(api: FakeApi, scope: kotlinx.coroutines.CoroutineScope): SyncWorker {
        val settings = SettingsStore(PreferenceDataStoreFactory.create(scope = scope, produceFile = { File(tmp.root, "settings.preferences_pb") }))
        val engine = SyncEngine(api, TimetableRepository(TimetableCache(File(tmp.root, "cache"))), settings, Clock.fixed(Instant.parse("2026-11-10T08:00:00Z"), SCHOOL_ZONE))
        return TestListenableWorkerBuilder<SyncWorker>(context).setWorkerFactory(SyncWorkerFactory(engine)).build()
    }

    private fun working() = FakeApi(EduPageDecoder.decodeViewer(Fixtures.text("viewer.json")), mutableMapOf("22" to Fixtures.raw()))

    @Test fun successReturnsSuccess() = runTest {
        assertEquals(ListenableWorker.Result.success(), workerWith(working(), backgroundScope).doWork())
    }
    @Test fun networkFailureRetries() = runTest {
        assertEquals(ListenableWorker.Result.retry(), workerWith(FakeApi(emptyList(), mutableMapOf(), IOException()), backgroundScope).doWork())
    }
    @Test fun formatFailureReturnsSuccess() = runTest {
        assertEquals(ListenableWorker.Result.success(), workerWith(FakeApi(emptyList(), mutableMapOf(), EduPageFormatException("x")), backgroundScope).doWork())
    }
    @Test fun schedulingIsUniqueAndRequiresNetwork() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        SyncScheduler.schedule(context); SyncScheduler.schedule(context)
        val info = WorkManager.getInstance(context).getWorkInfosForUniqueWork(SyncScheduler.WORK_NAME).get().single()
        assertEquals(WorkInfo.State.ENQUEUED, info.state)
        assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
        assertEquals(30 * 60_000L, info.periodicityInfo!!.repeatIntervalMillis)
    }
}
