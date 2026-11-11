package io.github.m4stas.technotunniplaan.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
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
    val settings = dataStore.data.map { decode(it[key]) }
    suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { it[key] = json.encodeToString(transform(decode(it[key]))) }
    }
}
