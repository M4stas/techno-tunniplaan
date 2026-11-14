package io.github.m4stas.technotunniplaan.ui.browse

import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.Settings

data class BrowseUiState(val tab: EntityKind, val query: String, val favourites: List<Pair<EntityRef, Entity>>, val results: List<Entity>)

fun browseUiState(schedule: Schedule, settings: Settings, tab: EntityKind, query: String) = BrowseUiState(
    tab, query,
    settings.favourites.mapNotNull { ref -> schedule.find(ref)?.let { ref to it } },
    searchEntities(schedule.entities(tab), query),
)
