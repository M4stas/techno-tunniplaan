package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.store.ViewMode
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.LocalDate

@Composable fun TimetableScreen(state: TimetableUiState, onSelectDate: (LocalDate) -> Unit, onViewMode: (ViewMode) -> Unit, onRefresh: () -> Unit,
    onToggleFavourite: () -> Unit, onOpenEntity: (EntityRef) -> Unit, onPickAgain: () -> Unit, onBack: (() -> Unit)?) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.timetable_back), tint = p.text) }
            Text(state.title, Modifier.weight(1f).padding(vertical = 16.dp), color = p.text, style = MaterialTheme.typography.titleLarge)
            if (!state.isHome) IconButton(onClick = onToggleFavourite) { Icon(painterResource(R.drawable.ic_pin), stringResource(if (state.isFavourite) R.string.unpin else R.string.pin), tint = if (state.isFavourite) p.accent else p.textSecondary) }
        }
        Row(Modifier.padding(horizontal = 16.dp)) {
            listOf(ViewMode.DAY to R.string.view_day, ViewMode.WEEK to R.string.view_week).forEach { (mode, label) ->
                TextButton(onClick = { onViewMode(mode) }, modifier = Modifier.testTag(if (mode == ViewMode.DAY) "view-day" else "view-week")) { Text(stringResource(label), color = if (state.viewMode == mode) p.accent else p.textSecondary) }
            }
        }
        if (state.entityMissing) {
            Column(Modifier.testTag("entity-missing").padding(24.dp)) { Text(stringResource(R.string.entity_missing), color = p.text); TextButton(onClick = onPickAgain) { Text(stringResource(R.string.pick_again), color = p.accent) } }
        } else {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                val monday = state.selectedDate?.with(java.time.DayOfWeek.MONDAY)
                val weeks = state.days.map { it.date.with(java.time.DayOfWeek.MONDAY) }.distinct()
                val index = weeks.indexOf(monday)
                if (index > 0) IconButton(onClick = { onSelectDate(weeks[index - 1].plusDays((state.selectedDate!!.dayOfWeek.value - 1).toLong())) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.timetable_previous), tint = p.text) }
                Text(state.weekTitle, Modifier.weight(1f).padding(vertical = 12.dp), color = p.textSecondary)
                if (index >= 0 && index < weeks.lastIndex) IconButton(onClick = { onSelectDate(weeks[index + 1].plusDays((state.selectedDate!!.dayOfWeek.value - 1).toLong())) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.timetable_next), tint = p.text) }
            }
            if (state.weekIsOutdated) Text(stringResource(R.string.week_outdated), Modifier.padding(horizontal = 16.dp), color = p.textSecondary)
            if (state.syncError != null) Text(stringResource(R.string.update_failed), Modifier.testTag("banner-sync-error").padding(16.dp), color = p.error)
            if (state.viewMode == ViewMode.DAY) DayView(state, onSelectDate, onRefresh)
        }
    }
}
