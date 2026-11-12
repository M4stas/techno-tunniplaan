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
class LessonSheetTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sheetOpensTheTeachersTimetable() {
        var opened: EntityRef? = null
        val state = timetableUiState(Schedule(listOf(Fixtures.week1())), Settings(home = Fixtures.CLASS_A), Fixtures.CLASS_A, Fixtures.MONDAY, Fixtures.MONDAY.atTime(9, 0))
        compose.setContent { AppTheme(ThemeMode.DARK) { TimetableScreen(state, {}, {}, {}, { opened = it }, {}) } }
        compose.onNodeWithTag("lesson-details").assertDoesNotExist()
        compose.onAllNodesWithText("Matemaatika").onFirst().performClick()
        compose.onNodeWithTag("lesson-details").assertExists()
        compose.onNodeWithText("Monday · K-XX-24A").assertExists()
        compose.onNodeWithText("08:30 – 10:00").assertExists()
        compose.onNode(hasText("X-101") and hasAnyAncestor(hasTestTag("lesson-details"))).assertExists()
        compose.onNode(hasText("TEACHER · ROOM") and hasAnyAncestor(hasTestTag("lesson-details"))).assertExists()
        compose.onNode(hasText("Mari Näidis · X-101") and hasAnyAncestor(hasTestTag("lesson-details"))).performClick()
        assertEquals(Fixtures.MARI, opened)
        compose.onNodeWithTag("lesson-details").assertDoesNotExist()
    }
}
