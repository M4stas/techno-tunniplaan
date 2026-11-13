package io.github.m4stas.technotunniplaan.ui.components

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.theme.AppTheme
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun stackedRowsRoundOnlyTheOuterCorners() {
        assertEquals(RoundedCornerShape(18.dp, 18.dp, 6.dp, 6.dp), stackShape(0, 3))
        assertEquals(RoundedCornerShape(6.dp, 6.dp, 6.dp, 6.dp), stackShape(1, 3))
        assertEquals(RoundedCornerShape(6.dp, 6.dp, 18.dp, 18.dp), stackShape(2, 3))
        assertEquals(RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp), stackShape(0, 1))
    }

    @Test fun segmentedReportsSelectionAndDescriptions() {
        var picked: String? = "none"
        val options = listOf(SegmentOption<String?>(null, "Auto", "Follow system"), SegmentOption<String?>("et", "ET", "Eesti"), SegmentOption<String?>("en", "English"))
        compose.setContent { AppTheme(ThemeMode.DARK) { Segmented(options, "et", { picked = it }) } }
        compose.onNodeWithContentDescription("Eesti").assertIsSelected()
        compose.onNodeWithText("English").assertIsNotSelected().performClick()
        assertEquals("en", picked)
        compose.onNodeWithContentDescription("Follow system").performClick()
        assertNull(picked)
        // A shortened label is hidden from accessibility services. The description stands in for it.
        compose.onNodeWithText("ET").assertDoesNotExist()
    }

    @Test fun segmentedPillSlidesToTheNewChoice() {
        val options = listOf(SegmentOption("a", "Classes"), SegmentOption("b", "Teachers"), SegmentOption("c", "Rooms"))
        var selected by mutableStateOf("a")
        compose.setContent { AppTheme(ThemeMode.DARK) { Segmented(options, selected, { selected = it }, Modifier.width(300.dp)) } }
        fun left(tagOrText: String, byTag: Boolean = false) =
            (if (byTag) compose.onNodeWithTag(tagOrText) else compose.onNodeWithText(tagOrText)).getBoundsInRoot().left
        assertEquals(left("Classes"), left("segment-indicator", byTag = true))
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Rooms").performClick()
        // Frame by frame, the pill passes through positions between the old and the new choice instead of jumping.
        val samples = List(30) { compose.mainClock.advanceTimeByFrame(); left("segment-indicator", byTag = true) }
        assertTrue(samples.any { it > left("Classes") && it < left("Rooms") })
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        assertEquals(left("Rooms"), left("segment-indicator", byTag = true))
        compose.onNodeWithText("Rooms").assertIsSelected()
    }

    @Test fun searchFieldAndLoadFailure() {
        var query = ""
        var retries = 0
        compose.setContent { AppTheme(ThemeMode.DARK) { Column { PillSearchField(query, { query = it }); LoadFailed({ retries++ }) } } }
        compose.onNodeWithText("Search").assertExists()
        compose.onNodeWithTag("search-field").performTextInput("b")
        assertEquals("b", query)
        compose.onNodeWithText("Can't load the timetable").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun circleAndPrimaryButtons() {
        var backs = 0
        var saves = 0
        compose.setContent {
            AppTheme(ThemeMode.DARK) {
                Column {
                    CircleButton(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, "Back", { backs++ })
                    PrimaryButton("Save", { saves++ }, enabled = false)
                }
            }
        }
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, backs)
        compose.onNodeWithText("Save").assertIsNotEnabled()
        assertEquals(0, saves)
    }
}
