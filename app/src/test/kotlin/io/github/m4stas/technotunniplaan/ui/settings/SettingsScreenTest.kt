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
        var homeOpened = false
        val state = settingsUiState(Schedule(listOf(Fixtures.week1())), Settings(home = Fixtures.CLASS_A))
        compose.setContent { AppTheme(ThemeMode.DARK) { SettingsScreen(state, { homeOpened = true }, { theme = it }, { language = it }, { syncs++ }) } }
        compose.onNodeWithText("K-XX-24A").performClick()
        assertTrue(homeOpened)
        compose.onNodeWithContentDescription("Dark").performScrollTo().performClick()
        assertEquals(ThemeMode.DARK, theme)
        compose.onNodeWithContentDescription("Eesti").performScrollTo().performClick()
        assertEquals("et", language)
        compose.onNodeWithContentDescription("Light").performScrollTo().performClick()
        assertEquals(ThemeMode.LIGHT, theme)
        compose.onAllNodesWithContentDescription("Follow system")[0].performScrollTo().performClick()
        assertEquals(ThemeMode.SYSTEM, theme)
        compose.onNodeWithContentDescription("English").performScrollTo().performClick()
        assertEquals("en", language)
        compose.onAllNodesWithContentDescription("Follow system")[1].performScrollTo().performClick()
        assertNull(language)
        compose.onNodeWithText("Never updated").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Update").performScrollTo().performClick()
        assertEquals(1, syncs)
    }
    @Test fun syncIsDisabledWhileSyncing() {
        var syncs = 0
        val state = settingsUiState(Schedule(emptyList()), Settings(), syncing = true)
        compose.setContent { AppTheme(ThemeMode.DARK) { SettingsScreen(state, {}, {}, {}, { syncs++ }) } }
        compose.onNodeWithText("Update").performScrollTo().assertIsNotEnabled().performClick()
        assertEquals(0, syncs)
    }
}
