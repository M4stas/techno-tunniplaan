package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.AppTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun offlineRetry() {
        var retries = 0
        compose.setContent { AppTheme(ThemeMode.DARK) { OnboardingScreen(OnboardingState.Offline, {}, {}, { retries++ }) } }
        compose.onNodeWithText("Try again").assertIsDisplayed().performClick()
        assertEquals(1, retries)
    }
    @Test fun queryAndSelection() {
        val a = Choice(EntityRef(EntityKind.CLASS, "a"), Entity("a", "K-XX-24A", "A"))
        val b = Choice(EntityRef(EntityKind.TEACHER, "b"), Entity("b", "Mari Näidis", "MN"))
        var query = ""
        var picked: Choice? = null
        compose.setContent { AppTheme(ThemeMode.DARK) { OnboardingScreen(OnboardingState.Pick("", listOf(a, b)), { query = it }, { picked = it }, {}) } }
        compose.onNodeWithText("Select timetable").assertIsDisplayed()
        compose.onNodeWithText("I'm a student").assertDoesNotExist()
        compose.onNodeWithText(a.entity.name).assertIsDisplayed()
        compose.onNodeWithText("Class").assertIsDisplayed()
        compose.onNodeWithText(b.entity.name).assertIsDisplayed()
        compose.onNodeWithText("Teacher").assertIsDisplayed()
        compose.onNodeWithText("Search").performTextInput("b")
        assertEquals("b", query)
        compose.onNodeWithText(b.entity.name).performClick()
        assertEquals(b, picked)
    }
}
