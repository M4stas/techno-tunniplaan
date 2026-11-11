package io.github.m4stas.technotunniplaan.ui.browse

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
class BrowseScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun showsFavouritesAndOpensRows() {
        var tab: EntityKind? = null
        var opened: EntityRef? = null
        val state = browseUiState(Schedule(listOf(Fixtures.week1())), Settings(favourites = listOf(Fixtures.ROOM_101, Fixtures.MARI)), EntityKind.CLASS, "")
        compose.setContent { AppTheme(ThemeMode.DARK) { BrowseScreen(state, { tab = it }, {}, { opened = it }) } }
        compose.onNodeWithText("Favourites").assertIsDisplayed()
        compose.onNodeWithText("X-101").assertIsDisplayed()
        compose.onNodeWithText("Mari Näidis").assertIsDisplayed().performClick()
        assertEquals(Fixtures.MARI, opened)
        compose.onNodeWithText("Teachers").performClick()
        assertEquals(EntityKind.TEACHER, tab)
        compose.onNodeWithText("K-XX-24B").performClick()
        assertEquals(Fixtures.CLASS_B, opened)
    }
    @Test fun hidesEmptyFavourites() {
        val state = browseUiState(Schedule(listOf(Fixtures.week1())), Settings(), EntityKind.CLASS, "")
        compose.setContent { AppTheme(ThemeMode.DARK) { BrowseScreen(state, {}, {}, {}) } }
        compose.onNodeWithText("Favourites").assertDoesNotExist()
    }
}
