package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.ui.theme.AppTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ViewSwitchTest {
    @get:Rule val compose = createComposeRule()
    @Test fun overlappingLessonsKeepTheirOwnPeriodBounds() {
        val week = Fixtures.week1()
        val template = week.lessons.first { it.firstPeriod == 1 && it.lastPeriod == 2 }
        val spanning = template.copy(subjectShort = "A")
        val later = template.copy(subjectShort = "B", firstPeriod = 2, start = week.periods.first { it.number == 2 }.start)
        val earlier = template.copy(subjectShort = "C", lastPeriod = 1, end = week.periods.first { it.number == 1 }.end)
        val grid = weekGrid(week, mapOf(0 to listOf(spanning, later, earlier)))
        var clicked: Lesson? = null
        compose.setContent { AppTheme(ThemeMode.DARK) { WeekView(grid) { clicked = it } } }
        val a = compose.onNode(hasText("A") and hasClickAction())
        val b = compose.onNode(hasText("B") and hasClickAction())
        val c = compose.onNode(hasText("C") and hasClickAction())
        a.assertHeightIsEqualTo(170.dp)
        b.assertHeightIsEqualTo(82.dp)
        c.assertHeightIsEqualTo(82.dp)
        val boundsA = a.getUnclippedBoundsInRoot()
        val boundsB = b.getUnclippedBoundsInRoot()
        val boundsC = c.getUnclippedBoundsInRoot()
        assertEquals(boundsA.top + 88.dp, boundsB.top)
        assertEquals(boundsA.top, boundsC.top)
        assertTrue(boundsC.right <= boundsB.left)
        b.performClick()
        assertEquals(later, clicked)
    }
    @Test fun switchesAndOpensTeacher() {
        var opened: EntityRef? = null
        var selected: ViewMode? = null
        compose.setContent {
            var mode by remember { mutableStateOf(ViewMode.DAY) }
            val state = timetableUiState(Schedule(listOf(Fixtures.week1())), Settings(home = Fixtures.CLASS_A, viewMode = mode), Fixtures.CLASS_A, Fixtures.MONDAY, Fixtures.MONDAY.atTime(9,0))
            AppTheme(ThemeMode.DARK) { TimetableScreen(state, {}, { selected = it; mode = it }, {}, {}, { opened = it }, {}, null) }
        }
        compose.onNodeWithTag("day-list").assertExists()
        compose.onNodeWithTag("week-grid").assertDoesNotExist()
        compose.onNodeWithTag("view-week").performClick()
        assertEquals(ViewMode.WEEK, selected)
        compose.onNodeWithTag("day-list").assertDoesNotExist()
        compose.onNodeWithTag("week-grid").assertExists()
        compose.onAllNodesWithText("101").onFirst().assertExists()
        compose.onAllNodesWithText("Mat").onFirst().performClick()
        compose.onNodeWithTag("lesson-details").assertExists()
        compose.onNodeWithText("Matemaatika").assertExists()
        compose.onNodeWithText("Mari Näidis").performClick()
        assertEquals(Fixtures.MARI, opened)
        compose.onNodeWithTag("lesson-details").assertDoesNotExist()
    }
}
