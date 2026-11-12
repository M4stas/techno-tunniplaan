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
import io.github.m4stas.technotunniplaan.ui.theme.Motion
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DayView(state: TimetableUiState, onSelectDate: (LocalDate) -> Unit, onRefresh: () -> Unit, onSlotClick: (SlotUi) -> Unit = {},
    pager: PagerState = rememberDayPager(state)) {
    if (state.days.isEmpty()) {
        // No week is cached. The block scrolls so that pull to refresh still works.
        PullToRefreshBox(state.refreshing, onRefresh, Modifier.fillMaxSize().testTag("day-list")) {
            LoadFailed(onRefresh, Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("load-failed"))
        }
        return
    }
    val selectedIndex = state.days.indexOfFirst { it.date == state.selectedDate }.coerceAtLeast(0)
    val dates = state.days.map { it.date }
    // A day picked on the same set of days slides there; a new week or a changed day list jumps without animating.
    val lastDates = remember { object { var value = dates } }
    LaunchedEffect(state.selectedDate, dates) {
        val sameDays = lastDates.value == dates
        lastDates.value = dates
        if (pager.currentPage != selectedIndex) {
            if (sameDays) pager.animateScrollToPage(selectedIndex, animationSpec = Motion.medium()) else pager.scrollToPage(selectedIndex)
        }
    }
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

/** The day pager's state, hoisted so the day island can follow a swipe while it happens. */
@Composable fun rememberDayPager(state: TimetableUiState): PagerState =
    rememberPagerState(initialPage = state.days.indexOfFirst { it.date == state.selectedDate }.coerceAtLeast(0)) { state.days.size }
