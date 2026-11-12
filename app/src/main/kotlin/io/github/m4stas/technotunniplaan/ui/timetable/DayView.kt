package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import io.github.m4stas.technotunniplaan.ui.components.Clearance
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DayView(state: TimetableUiState, onSelectDate: (LocalDate) -> Unit, onRefresh: () -> Unit, onLessonClick: (io.github.m4stas.technotunniplaan.schedule.Lesson) -> Unit = {}) {
    val p = LocalPalette.current
    if (state.days.isEmpty()) { PullToRefreshBox(state.refreshing, onRefresh, Modifier.fillMaxSize().testTag("day-list")) { Text(stringResource(R.string.no_lessons), color = p.text) }; return }
    val selectedIndex = state.days.indexOfFirst { it.date == state.selectedDate }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = selectedIndex) { state.days.size }
    LaunchedEffect(state.selectedDate, state.days) { if (pager.currentPage != selectedIndex) pager.scrollToPage(selectedIndex) }
    val currentState by rememberUpdatedState(state)
    val selectDate by rememberUpdatedState(onSelectDate)
    LaunchedEffect(pager) { snapshotFlow { pager.settledPage }.collect { index -> currentState.days.getOrNull(index)?.let { selectDate(it.date) } } }
    Column(Modifier.testTag("day-list")) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.SpaceEvenly) {
            state.days.filter { it.date.with(java.time.DayOfWeek.MONDAY) == state.selectedDate?.with(java.time.DayOfWeek.MONDAY) }.forEach { day ->
                TextButton(onClick = { onSelectDate(day.date) }) { Text(day.date.format(DateTimeFormatter.ofPattern("EEE d")), color = if (day.date == state.selectedDate) p.accentText else p.textSecondary) }
            }
        }
        PullToRefreshBox(state.refreshing, onRefresh, Modifier.fillMaxSize()) {
            HorizontalPager(pager, Modifier.fillMaxSize()) { index ->
                val day = state.days[index]
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp + Clearance.Nav)) {
                    if (day.rows.isEmpty()) item { Text(stringResource(R.string.no_lessons), color = p.text) }
                    items(day.rows.size) { row -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { day.rows[row].forEach { lesson -> LessonCard(lesson, state.entity, lesson == day.highlight, Modifier.weight(1f), onLessonClick) } } }
                }
            }
        }
    }
}
