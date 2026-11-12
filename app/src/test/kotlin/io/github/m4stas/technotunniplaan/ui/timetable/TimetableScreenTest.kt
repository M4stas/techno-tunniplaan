package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.*
import io.github.m4stas.technotunniplaan.ui.theme.AppTheme
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimetableScreenTest {
    @get:Rule val compose = createComposeRule()
    private val week = Schedule(listOf(Fixtures.week1()))
    private val home = Settings(home = Fixtures.CLASS_A)
    private var selected: LocalDate? = null
    private var refreshes = 0

    private fun state(date: LocalDate? = null, now: LocalDateTime = Fixtures.MONDAY.atTime(9, 0), schedule: Schedule = week) =
        timetableUiState(schedule, home, Fixtures.CLASS_A, date, now)
    private fun show(st: TimetableUiState) {
        compose.setContent { AppTheme(ThemeMode.DARK) { TimetableScreen(st, { selected = it }, { refreshes++ }, {}, {}, {}, null) } }
    }

    @Test fun headerShowsTheDayAndTheEntity() {
        show(state())
        compose.onNodeWithText("Monday").assertIsDisplayed()
        compose.onNodeWithText("9 November · K-XX-24A").assertIsDisplayed()
        compose.onNodeWithText("Nädal11 · 9.11–13.11").assertDoesNotExist()
    }

    @Test fun dayIslandSelectsADay() {
        show(state())
        compose.onNodeWithTag("island-fade").assertExists()
        compose.onNodeWithTag("thumb-days").assertIsDisplayed()
        (0L..4L).forEach { compose.onNodeWithTag("thumb-day-${Fixtures.MONDAY.plusDays(it)}").assertExists() }
        compose.onNodeWithTag("thumb-day-${Fixtures.MONDAY.plusDays(5)}").assertDoesNotExist()
        compose.onNodeWithTag("thumb-day-2026-11-09").assertIsSelected()
        compose.onNodeWithContentDescription("Wednesday 11").assertIsNotSelected()
        compose.onNodeWithTag("thumb-day-2026-11-11").performClick()
        assertEquals(LocalDate.of(2026, 11, 11), selected)
    }

    @Test fun swipePastTheWeeksLastDayMovesToTheNextWeek() {
        val twoWeeks = Schedule(listOf(Fixtures.week1(), Fixtures.week2()))
        var date by mutableStateOf<LocalDate?>(Fixtures.MONDAY.plusDays(4))
        compose.setContent {
            AppTheme(ThemeMode.DARK) {
                TimetableScreen(timetableUiState(twoWeeks, home, Fixtures.CLASS_A, date, Fixtures.MONDAY.atTime(9, 0)), { date = it }, {}, {}, {}, {}, null)
            }
        }
        compose.onNodeWithTag("thumb-day-2026-11-13").assertIsSelected()
        compose.onNodeWithTag("day-list").performTouchInput { swipeLeft() }
        compose.waitForIdle()
        assertEquals(Fixtures.MONDAY.plusWeeks(1), date)
        compose.onNodeWithText("Monday").assertIsDisplayed()
        compose.onNodeWithTag("thumb-day-2026-11-16").assertIsSelected()
        compose.onNodeWithTag("thumb-day-2026-11-13").assertDoesNotExist()
    }

    @Test fun emptyDayShowsTheNextLesson() {
        show(state(Fixtures.MONDAY.plusDays(3), Fixtures.MONDAY.atTime(12, 0)))
        compose.onNodeWithTag("empty-day").assertIsDisplayed()
        compose.onNodeWithText("No lessons").assertIsDisplayed()
        compose.onNodeWithText("Next lesson on Tuesday at 12:45").assertIsDisplayed()
    }

    @Test fun emptyDayOnTheLastDayHasNoNextLesson() {
        show(state(Fixtures.MONDAY.plusDays(3), Fixtures.MONDAY.plusDays(4).atTime(15, 0)))
        compose.onNodeWithText("No lessons").assertIsDisplayed()
        compose.onNode(hasText("Next lesson", substring = true)).assertDoesNotExist()
    }

    @Test fun noDataOffersARetry() {
        show(state(schedule = Schedule(emptyList())))
        compose.onNodeWithTag("load-failed").assertIsDisplayed()
        compose.onNodeWithTag("thumb-days").assertDoesNotExist()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, refreshes)
    }
}
