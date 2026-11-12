package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.ui.components.CircleButton
import io.github.m4stas.technotunniplaan.ui.components.IslandFade
import io.github.m4stas.technotunniplaan.ui.components.PrimaryButton
import io.github.m4stas.technotunniplaan.ui.components.ScreenHeader
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.DayOfWeek
import java.time.LocalDate

private const val SEPARATOR = " · "

@Composable fun TimetableScreen(state: TimetableUiState, onSelectDate: (LocalDate) -> Unit, onRefresh: () -> Unit,
    onToggleFavourite: () -> Unit, onOpenEntity: (EntityRef) -> Unit, onPickAgain: () -> Unit) {
    val p = LocalPalette.current
    val locale = LocalConfiguration.current.locales[0]
    var openSlot by remember { mutableStateOf<SlotUi?>(null) }
    openSlot?.let { slot ->
        val day = state.selectedDate?.let { dayTitle(it, locale) }.orEmpty()
        LessonDetailSheet(slot, listOf(day, state.title).filter { it.isNotEmpty() }.joinToString(SEPARATOR), state.entity, { openSlot = null }, onOpenEntity)
    }
    val date = state.selectedDate
    val monday = date?.with(DayOfWeek.MONDAY)
    val weekDays = state.days.map { it.date }.filter { it.with(DayOfWeek.MONDAY) == monday }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (date != null) {
                val dateText = dateLabel(date, stringResource(R.string.date_pattern), locale)
                ScreenHeader(
                    dayTitle(date, locale),
                    listOf(dateText, state.title).filter { it.isNotEmpty() }.joinToString(SEPARATOR),
                    trailing = if (state.isHome) null else {
                        {
                            CircleButton(Icons.Filled.Star,
                                stringResource(if (state.isFavourite) R.string.unpin else R.string.pin),
                                onToggleFavourite, Modifier.testTag("entity-star"), active = state.isFavourite)
                        }
                    },
                )
            }
            if (state.entityMissing) {
                Column(Modifier.testTag("entity-missing").padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.entity_missing), color = p.text, fontSize = 16.sp)
                    PrimaryButton(stringResource(R.string.pick_again), onPickAgain)
                }
            } else {
                if (state.weekIsOutdated) Text(stringResource(R.string.week_outdated), Modifier.padding(horizontal = 20.dp, vertical = 2.dp), color = p.textMuted, fontSize = 12.sp)
                if (state.syncError != null) Text(stringResource(R.string.update_failed), Modifier.testTag("banner-sync-error").padding(horizontal = 20.dp, vertical = 2.dp), color = p.error, fontSize = 12.sp)
                DayView(state, onSelectDate, onRefresh) { openSlot = it }
            }
        }
        IslandFade(Modifier.align(Alignment.BottomCenter))
        // 84dp keeps the day island clear of the navigation island that AppShell draws below it.
        if (!state.entityMissing && weekDays.isNotEmpty()) {
            ThumbDays(weekDays, date, onSelectDate, Modifier.align(Alignment.BottomCenter).padding(bottom = 84.dp))
        }
    }
}
