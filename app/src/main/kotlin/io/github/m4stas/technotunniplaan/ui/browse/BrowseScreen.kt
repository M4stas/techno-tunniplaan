package io.github.m4stas.technotunniplaan.ui.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import io.github.m4stas.technotunniplaan.ui.components.Clearance
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

@Composable fun BrowseScreen(state: BrowseUiState, onTab: (EntityKind) -> Unit, onQuery: (String) -> Unit, bottomClearance: Dp = Clearance.Nav, onOpen: (EntityRef) -> Unit) {
    val p = LocalPalette.current
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp + bottomClearance), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.favourites.isNotEmpty()) {
            item { Text(stringResource(R.string.favourites), color = p.text, style = MaterialTheme.typography.titleLarge) }
            items(state.favourites) { (ref, entity) ->
                Surface(onClick = { onOpen(ref) }, shape = RoundedCornerShape(24.dp), color = p.card, modifier = Modifier.fillMaxWidth()) {
                    Text(entity.name, Modifier.padding(16.dp), color = p.text)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(EntityKind.CLASS to R.string.tab_classes, EntityKind.TEACHER to R.string.tab_teachers, EntityKind.ROOM to R.string.tab_rooms).forEach { (kind, label) ->
                    Surface(onClick = { onTab(kind) }, shape = RoundedCornerShape(50), color = if (state.tab == kind) p.selected else p.surface, modifier = Modifier.weight(1f)) {
                        Text(stringResource(label), Modifier.padding(horizontal = 8.dp, vertical = 14.dp), color = if (state.tab == kind) p.onSelected else p.textSecondary)
                    }
                }
            }
        }
        item {
            OutlinedTextField(state.query, onQuery, label = { Text(stringResource(R.string.search), color = p.textSecondary) }, singleLine = true,
                shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = p.text, unfocusedTextColor = p.text, cursorColor = p.accentText,
                    focusedBorderColor = p.accentText, unfocusedBorderColor = p.outline, focusedContainerColor = p.surface, unfocusedContainerColor = p.surface))
        }
        items(state.results) { entity ->
            Surface(onClick = { onOpen(EntityRef(state.tab, entity.key)) }, shape = RoundedCornerShape(24.dp), color = p.card, modifier = Modifier.fillMaxWidth()) {
                Text(entity.name, Modifier.padding(16.dp), color = p.text)
            }
        }
    }
}
