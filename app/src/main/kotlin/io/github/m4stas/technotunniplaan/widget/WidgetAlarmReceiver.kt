package io.github.m4stas.technotunniplaan.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.IOException
import kotlinx.coroutines.*

class WidgetAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try { WidgetUpdater.refresh(context.applicationContext) } catch (_: IOException) { }
            finally { pending.finish() }
        }
    }
}
