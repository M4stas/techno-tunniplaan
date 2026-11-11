package io.github.m4stas.technotunniplaan.ui.settings

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
class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun optionsAndActions() {
        var theme: ThemeMode? = null
        var language: String? = null
        var syncs = 0
        val state = settingsUiState(Schedule(listOf(Fixtures.week1())), Settings(home = Fixtures.CLASS_A))
        compose.setContent { AppTheme(ThemeMode.DARK) { SettingsScreen(state, {}, { _, _ -> }, { theme = it }, { language = it }, { syncs++ }) } }
        listOf("Grupp 1", "Grupp 2", "All groups").forEach { compose.onNodeWithText(it).assertExists() }
        compose.onNodeWithText("Dark").performScrollTo().performClick()
        assertEquals(ThemeMode.DARK, theme)
        compose.onNodeWithText("Eesti").performScrollTo().performClick()
        assertEquals("et", language)
        compose.onNodeWithText("Never updated").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Update now").performScrollTo().performClick()
        assertEquals(1, syncs)
    }
}
