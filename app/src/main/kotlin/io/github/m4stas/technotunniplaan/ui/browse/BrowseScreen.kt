package io.github.m4stas.technotunniplaan.ui.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.ui.components.*
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

/** [showTitle] is off where a host screen, such as the widget setup, already has its own heading. */
@Composable fun BrowseScreen(state: BrowseUiState, onTab: (EntityKind) -> Unit, onQuery: (String) -> Unit, bottomClearance: Dp = Clearance.Nav,
    showTitle: Boolean = true, onOpen: (EntityRef) -> Unit) {
    val p = LocalPalette.current
    val tabs = listOf(
        SegmentOption(EntityKind.CLASS, stringResource(R.string.tab_classes)),
        SegmentOption(EntityKind.TEACHER, stringResource(R.string.tab_teachers)),
        SegmentOption(EntityKind.ROOM, stringResource(R.string.tab_rooms)),
    )
    // Rows of one list sit 4dp apart. Section labels and the controls add their own top padding.
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 20.dp + bottomClearance),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (showTitle) item {
            Text(stringResource(R.string.nav_browse), Modifier.semantics { heading() }, color = p.text,
                fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.84).sp)
        }
        item { Segmented(tabs, state.tab, onTab, Modifier.fillMaxWidth().padding(top = if (showTitle) 14.dp else 0.dp)) }
        item { PillSearchField(state.query, onQuery, Modifier.padding(top = 8.dp)) }
        if (state.favourites.isNotEmpty()) {
            item { SectionLabel(stringResource(R.string.favourites)) }
            itemsIndexed(state.favourites) { index, (ref, entity) ->
                EntityRow(entity.name, kindLabel(ref.kind), index, state.favourites.size) { onOpen(ref) }
            }
        }
        if (state.results.isNotEmpty()) item { SectionLabel(tabs.first { it.value == state.tab }.label) }
        itemsIndexed(state.results) { index, entity ->
            EntityRow(entity.name, null, index, state.results.size) { onOpen(EntityRef(state.tab, entity.key)) }
        }
    }
}

@Composable private fun EntityRow(name: String, kind: String?, index: Int, count: Int, onClick: () -> Unit) {
    val p = LocalPalette.current
    StackRow(index, count, onClick, minHeight = 52.dp) {
        Text(name, Modifier.weight(1f), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (kind != null) Text(kind, color = p.textMuted, fontSize = 13.sp)
        Chevron()
    }
}

@Composable private fun SectionLabel(title: String) {
    Text(title, Modifier.padding(start = 4.dp, top = 14.dp, bottom = 2.dp),
        color = LocalPalette.current.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
}

@Composable private fun kindLabel(kind: EntityKind) = stringResource(when (kind) {
    EntityKind.CLASS -> R.string.kind_class
    EntityKind.TEACHER -> R.string.kind_teacher
    EntityKind.ROOM -> R.string.kind_room
})
