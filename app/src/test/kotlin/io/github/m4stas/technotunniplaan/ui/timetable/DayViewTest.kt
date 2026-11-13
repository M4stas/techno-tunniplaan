package io.github.m4stas.technotunniplaan.ui.timetable
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.ui.theme.AppTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
@RunWith(RobolectricTestRunner::class)
class DayViewTest {
 @get:Rule val compose = createComposeRule()
 private fun state() = timetableUiState(Schedule(listOf(Fixtures.week1())),Settings(home=Fixtures.CLASS_A),Fixtures.CLASS_A,null,Fixtures.MONDAY.atTime(9,0))
 private fun show(st: TimetableUiState, pick: () -> Unit = {}) { compose.setContent { AppTheme(ThemeMode.DARK) { TimetableScreen(st, {}, {}, {}, {}, pick) } } }
 @Test fun selectingCurrentPageWithForwardOffsetCentersIt() = selectFractionalPage(0.25f)
 @Test fun selectingCurrentPageWithReverseOffsetCentersIt() = selectFractionalPage(-0.25f)
 private fun selectFractionalPage(offset: Float) {
     val initial = state()
     val selected = mutableStateOf(initial.selectedDate)
     lateinit var pager: PagerState
     lateinit var scope: CoroutineScope
     compose.setContent {
         AppTheme(ThemeMode.DARK) {
             val current = initial.copy(selectedDate = selected.value)
             pager = rememberDayPager(current)
             scope = rememberCoroutineScope()
             DayView(current, {}, {}, pager = pager)
         }
     }
     compose.runOnIdle {
         scope.launch {
             pager.scrollToPage(2, offset)
             selected.value = initial.days[2].date
         }
     }
     compose.waitForIdle()
     compose.runOnIdle {
         assertEquals(2, pager.currentPage)
         assertEquals(0f, pager.currentPageOffsetFraction, 0.001f)
     }
 }
 @Test fun lessons() {
     show(state())
     compose.onAllNodesWithTag("slot-current", useUnmergedTree = true).assertCountEquals(1)
     compose.onAllNodesWithTag("slot-card").assertCountEquals(2)
     listOf("Matemaatika", "Mari Näidis · 101", "Jaan Proov", "Tiit Katse", "102", "103", "15 min break", "08:30", "10:00").forEach { compose.onNodeWithText(it).assertExists() }
     compose.onAllNodesWithText("Inglise keel").assertCountEquals(1)
     compose.onNodeWithText("Grupp 1").assertDoesNotExist()
     compose.onNodeWithText("08:30–10:00").assertDoesNotExist()
 }
 @Test fun progress() {
     show(timetableUiState(Schedule(listOf(Fixtures.week1())), Settings(home = Fixtures.CLASS_A), Fixtures.CLASS_A, null, Fixtures.MONDAY.atTime(9, 15)))
     val half = SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.5f, 0f..1f))
     compose.onNode(hasTestTag("slot-current") and half, useUnmergedTree = true).assertExists()
 }
 @Test fun syncError() { show(state().copy(syncError=SyncError.NETWORK)); compose.onNodeWithTag("banner-sync-error").assertIsDisplayed() }
 @Test fun missing() { var picked=false; show(state().copy(entityMissing=true)) { picked=true }; compose.onNodeWithTag("entity-missing").assertIsDisplayed(); compose.onNodeWithText("Pick again").performClick(); assertTrue(picked) }
}
