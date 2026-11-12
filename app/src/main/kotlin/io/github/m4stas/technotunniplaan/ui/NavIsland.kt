package io.github.m4stas.technotunniplaan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

/** The floating bottom navigation. [current] is the current route, so no tab is active on an entity timetable. */
@Composable fun NavIsland(current: String?, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val items = listOf(
        Triple(Routes.HOME, rememberVectorPainter(Icons.Filled.DateRange), stringResource(R.string.nav_timetable)),
        Triple(Routes.BROWSE, rememberVectorPainter(Icons.Filled.Search), stringResource(R.string.nav_browse)),
        Triple(Routes.SETTINGS, painterResource(R.drawable.ic_sliders), stringResource(R.string.nav_settings)),
    )
    Row(modifier.testTag("nav-island").clip(CircleShape).background(p.island).padding(6.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { (route, icon, label) ->
            val active = current == route
            Box(Modifier.size(48.dp).clip(CircleShape).background(if (active) p.accent else p.island)
                .selectable(active, role = Role.Tab, onClick = { onNavigate(route) })
                .semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(22.dp), tint = if (active) p.onAccent else p.onIsland)
            }
        }
    }
}
