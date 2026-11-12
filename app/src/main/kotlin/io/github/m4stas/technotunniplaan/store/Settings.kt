package io.github.m4stas.technotunniplaan.store

import io.github.m4stas.technotunniplaan.schedule.EntityRef
import kotlinx.serialization.Serializable

enum class ThemeMode { SYSTEM, DARK, LIGHT }
enum class SyncError { NETWORK, FORMAT }
@Serializable data class Settings(
    val home: EntityRef? = null,
    val favourites: List<EntityRef> = emptyList(),
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val lastSyncMillis: Long? = null,
    val lastSyncError: SyncError? = null,
)
fun Settings.withHome(ref: EntityRef) = copy(home = ref)
fun Settings.toggleFavourite(ref: EntityRef) = copy(favourites = if (ref in favourites) favourites - ref else favourites + ref)
