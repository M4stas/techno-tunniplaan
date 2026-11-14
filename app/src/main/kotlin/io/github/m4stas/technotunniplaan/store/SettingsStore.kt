package io.github.m4stas.technotunniplaan.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsStore(private val dataStore: DataStore<Preferences>) {
    private val key = stringPreferencesKey("settings")
    private val json = Json { ignoreUnknownKeys = true }
    private fun decode(value: String?): Settings = try {
        if (value == null) Settings() else json.decodeFromString<Settings>(value)
    } catch (e: IllegalArgumentException) {
        Settings()
    }
    /** An unreadable file reads as the defaults, so the collectors in the UI do not throw. */
    val settings = dataStore.data.retryWhen { cause, attempt ->
        if (cause !is IOException) return@retryWhen false
        emit(emptyPreferences())
        delay(minOf(1_000L shl attempt.coerceAtMost(5).toInt(), 30_000L))
        true
    }.map { decode(it[key]) }
    suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { it[key] = json.encodeToString(transform(decode(it[key]))) }
    }
}
