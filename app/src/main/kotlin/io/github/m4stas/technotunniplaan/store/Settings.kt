package io.github.m4stas.technotunniplaan.store

import io.github.m4stas.technotunniplaan.schedule.EntityKind
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.schedule.SubGroupFilter
import kotlinx.serialization.Serializable

enum class ThemeMode { SYSTEM, DARK, LIGHT }
enum class ViewMode { DAY, WEEK }
enum class SyncError { NETWORK, FORMAT }
@Serializable data class Settings(
    val home: EntityRef? = null,
    val favourites: List<EntityRef> = emptyList(),
    val subGroupChoices: Map<String, String> = emptyMap(),
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val viewMode: ViewMode = ViewMode.DAY,
    val lastSyncMillis: Long? = null,
    val lastSyncError: SyncError? = null,
)
fun Settings.withHome(ref: EntityRef) = copy(home = ref, subGroupChoices = emptyMap())
fun Settings.subGroupFilter(): SubGroupFilter? = home?.takeIf { it.kind == EntityKind.CLASS && subGroupChoices.isNotEmpty() }?.let { SubGroupFilter(it.key, subGroupChoices) }
fun Settings.toggleFavourite(ref: EntityRef) = copy(favourites = if (ref in favourites) favourites - ref else favourites + ref)
