package io.github.m4stas.technotunniplaan.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.m4stas.technotunniplaan.Fixtures
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.AppTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppShellTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var nav: NavHostController
    /** Stands in for MainActivity: holds the entity an intent asked for until the shell has opened it. */
    private var open by mutableStateOf<EntityRef?>(null)
    private var home by mutableStateOf<EntityRef?>(Fixtures.CLASS_A)

    @Composable private fun Shell() {
        AppTheme(ThemeMode.DARK) {
            nav = rememberNavController()
            AppShell(home, open, { open = null }, nav) { Text("onboarding") }
        }
    }
    private fun route() = compose.runOnIdle { nav.currentBackStackEntry?.destination?.route }
    private fun entityArgs() = compose.runOnIdle { nav.currentBackStackEntry?.arguments?.let { it.getString("kind") to it.getString("key") } }
    private fun tap(label: String) = compose.onNode(hasText(label) and hasClickAction()).performClick()
    private fun back() = compose.runOnIdle { nav.popBackStack() }

    @Test fun startsAtOnboardingWithoutHomeAndAtHomeWithOne() {
        home = null
        compose.setContent { Shell() }
        assertEquals(Routes.ONBOARDING, route())
        compose.onNodeWithText("onboarding").assertIsDisplayed()
        compose.onNode(hasText("Browse") and hasClickAction()).assertDoesNotExist()
    }

    @Test fun launchAtAnotherEntityKeepsHomeUnderneath() {
        open = Fixtures.MARI
        compose.setContent { Shell() }
        assertEquals("entity/{kind}/{key}", route())
        assertEquals("TEACHER" to "mari näidis", entityArgs())
        assertNull(compose.runOnIdle { open })
        // Bottom-bar taps must not pile up: one back press from any tab reaches home, and home is the root.
        tap("Browse"); tap("Settings"); tap("Browse")
        assertEquals(Routes.BROWSE, route())
        back()
        assertEquals(Routes.HOME, route())
        assertNull(compose.runOnIdle { nav.previousBackStackEntry })
    }

    @Test fun launchAtHomeEntityShowsHome() {
        open = Fixtures.CLASS_A
        compose.setContent { Shell() }
        assertEquals(Routes.HOME, route())
        assertNull(compose.runOnIdle { nav.previousBackStackEntry })
    }

    @Test fun sameEntityTwiceOpensItTwice() {
        compose.setContent { Shell() }
        assertEquals(Routes.HOME, route())
        compose.runOnIdle { open = Fixtures.MARI }
        assertEquals("entity/{kind}/{key}", route())
        tap("Browse")
        assertEquals(Routes.BROWSE, route())
        compose.runOnIdle { open = Fixtures.MARI }
        assertEquals("entity/{kind}/{key}", route())
        back()
        assertEquals(Routes.HOME, route())
    }

    @Test fun differentEntityReplacesTheOpenOne() {
        open = Fixtures.MARI
        compose.setContent { Shell() }
        compose.runOnIdle { open = Fixtures.ROOM_101 }
        assertEquals("ROOM" to "x-101", entityArgs())
        back()
        assertEquals(Routes.HOME, route())
    }

    @Test fun recreationKeepsTheUserWhereTheyWere() {
        val restoration = StateRestorationTester(compose)
        open = Fixtures.MARI
        restoration.setContent { Shell() }
        tap("Settings")
        assertEquals(Routes.SETTINGS, route())
        restoration.emulateSavedInstanceStateRestore()
        assertEquals(Routes.SETTINGS, route())
    }

    @Test fun entityWaitsUntilOnboardingHasStoredAHome() {
        home = null
        open = Fixtures.MARI
        compose.setContent { Shell() }
        assertEquals(Routes.ONBOARDING, route())
        assertEquals(Fixtures.MARI, compose.runOnIdle { open })
    }
}
