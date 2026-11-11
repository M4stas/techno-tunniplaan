package io.github.m4stas.technotunniplaan

import android.content.Context
import android.content.Intent
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
        // A recreated activity gets the launch intent again. Opening its entity twice would undo the user's navigation.
        if (savedInstanceState == null) openEntity = parse(intent)
        setContent { AppRoot((application as App).container, openEntity) { openEntity = null } }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openEntity = parse(intent)
    }
    private fun parse(intent: Intent): EntityRef? {
        val kind = intent.getStringExtra("kind")?.let { value -> EntityKind.entries.find { it.name == value } } ?: return null
        val key = intent.getStringExtra("key")?.takeIf { it.isNotBlank() } ?: return null
        return EntityRef(kind, key)
    }
    companion object {
        fun intentFor(context: Context, entity: EntityRef?): Intent = Intent(context, MainActivity::class.java).apply {
            if (entity != null) { putExtra("kind", entity.kind.name); putExtra("key", entity.key) }
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }
}
