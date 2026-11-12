package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.ui.components.Clearance
import io.github.m4stas.technotunniplaan.ui.components.LoadFailed
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DayView(state: TimetableUiState, onSelectDate: (LocalDate) -> Unit, onRefresh: () -> Unit, onSlotClick: (SlotUi) -> Unit = {}) {
    if (state.days.isEmpty()) {
        // No week is cached. The block scrolls so that pull to refresh still works.
        PullToRefreshBox(state.refreshing, onRefresh, Modifier.fillMaxSize().testTag("day-list")) {
            LoadFailed(onRefresh, Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("load-failed"))
        }
        return
    }
    val selectedIndex = state.days.indexOfFirst { it.date == state.selectedDate }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = selectedIndex) { state.days.size }
    LaunchedEffect(state.selectedDate, state.days) { if (pager.currentPage != selectedIndex) pager.scrollToPage(selectedIndex) }
    val currentState by rememberUpdatedState(state)
    val selectDate by rememberUpdatedState(onSelectDate)
    LaunchedEffect(pager) { snapshotFlow { pager.settledPage }.collect { index -> currentState.days.getOrNull(index)?.let { selectDate(it.date) } } }
    PullToRefreshBox(state.refreshing, onRefresh, Modifier.fillMaxSize().testTag("day-list")) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { index ->
            val day = state.days[index]
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = Clearance.NavAndDays)) {
                if (day.slots.isEmpty()) item { EmptyDay(state.nextLesson, Modifier.fillParentMaxSize()) }
                day.slots.forEach { slot ->
                    slot.breakBefore?.let { pause -> item { BreakLabel(pause) } }
                    item { TimelineRow(slot, { onSlotClick(slot) }) }
                }
            }
        }
    }
}
