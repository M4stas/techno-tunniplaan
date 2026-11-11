package io.github.m4stas.technotunniplaan.ui.onboarding

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.m4stas.technotunniplaan.schedule.Entity
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
        compose.setContent { AppTheme(ThemeMode.DARK) { OnboardingScreen(OnboardingState.Offline, {}, {}, {}, {}, { retries++ }) } }
        compose.onNodeWithText("Try again").assertIsDisplayed().performClick()
        assertEquals(1, retries)
    }
    @Test fun teacherRole() {
        var role: Role? = null
        compose.setContent { AppTheme(ThemeMode.DARK) { OnboardingScreen(OnboardingState.ChooseRole, { role = it }, {}, {}, {}, {}) } }
        compose.onNodeWithText("I'm a teacher").performClick()
        assertEquals(Role.TEACHER, role)
    }
    @Test fun queryAndSelection() {
        val a = Entity("a", "K-XX-24A", "A")
        val b = Entity("b", "K-XX-24B", "B")
        var query = ""
        var picked: Entity? = null
        compose.setContent { AppTheme(ThemeMode.DARK) { OnboardingScreen(OnboardingState.Pick(Role.STUDENT, "", listOf(a, b)), {}, {}, { query = it }, { picked = it }, {}) } }
        compose.onNodeWithText(a.name).assertIsDisplayed()
        compose.onNodeWithText(b.name).assertIsDisplayed()
        compose.onNodeWithText("Search").performTextInput("b")
        assertEquals("b", query)
        compose.onNodeWithText(b.name).performClick()
        assertEquals(b, picked)
    }
}
