package io.github.m4stas.technotunniplaan.ui.timetable
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
@RunWith(RobolectricTestRunner::class)
class DayViewTest {
 @get:Rule val compose = createComposeRule()
 private fun state() = timetableUiState(Schedule(listOf(Fixtures.week1())),Settings(home=Fixtures.CLASS_A),Fixtures.CLASS_A,null,Fixtures.MONDAY.atTime(9,0))
 private fun show(st: TimetableUiState, pick: () -> Unit = {}) { compose.setContent { AppTheme(ThemeMode.DARK) { TimetableScreen(st, {}, {}, {}, {}, {}, pick, null) } } }
 @Test fun lessons() { show(state()); compose.onAllNodesWithTag("lesson-highlighted", useUnmergedTree = true).assertCountEquals(1); listOf("Matemaatika","08:30–10:00","Grupp 1","Grupp 2").forEach { compose.onNodeWithText(it).performScrollTo().assertIsDisplayed() } }
 @Test fun syncError() { show(state().copy(syncError=SyncError.NETWORK)); compose.onNodeWithTag("banner-sync-error").assertIsDisplayed() }
 @Test fun missing() { var picked=false; show(state().copy(entityMissing=true)) { picked=true }; compose.onNodeWithTag("entity-missing").assertIsDisplayed(); compose.onNodeWithText("Pick again").performClick(); assertTrue(picked) }
}
