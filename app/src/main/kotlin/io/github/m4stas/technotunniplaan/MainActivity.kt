package io.github.m4stas.technotunniplaan

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.m4stas.technotunniplaan.schedule.EntityKind
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.ui.AppRoot

class MainActivity : AppCompatActivity() {
    private var openEntity by mutableStateOf<EntityRef?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openEntity = initialRequest(savedInstanceState, intent)
        setContent { AppRoot((application as App).container, openEntity) { openEntity = null } }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savePending(outState, openEntity)
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // AppCompat applies a theme or language picked in the app by calling this method alone, without telling the
        // views. Passing the change on lets Compose redraw with the new strings and night mode.
        window.decorView.dispatchConfigurationChanged(newConfig)
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openEntity = parse(intent)
    }
    companion object {
        private const val PENDING_KIND = "pendingKind"
        private const val PENDING_KEY = "pendingKey"
        /**
         * A fresh start opens the intent's entity. A recreated activity gets the launch intent again, and opening it
         * twice would undo the user's navigation, so it reopens only a request the shell had not opened yet.
         */
        internal fun initialRequest(savedInstanceState: Bundle?, intent: Intent): EntityRef? =
            if (savedInstanceState == null) parse(intent) else ref(savedInstanceState.getString(PENDING_KIND), savedInstanceState.getString(PENDING_KEY))
        internal fun savePending(outState: Bundle, pending: EntityRef?) {
            if (pending != null) { outState.putString(PENDING_KIND, pending.kind.name); outState.putString(PENDING_KEY, pending.key) }
        }
        internal fun parse(intent: Intent): EntityRef? = ref(intent.getStringExtra("kind"), intent.getStringExtra("key"))
        private fun ref(kindName: String?, key: String?): EntityRef? {
            val kind = kindName?.let { value -> EntityKind.entries.find { it.name == value } } ?: return null
            return EntityRef(kind, key?.takeIf { it.isNotBlank() } ?: return null)
        }
        fun intentFor(context: Context, entity: EntityRef?): Intent = Intent(context, MainActivity::class.java).apply {
            if (entity != null) { putExtra("kind", entity.kind.name); putExtra("key", entity.key) }
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}
