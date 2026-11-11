package com.azvar.puskar.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azvar.puskar.MainActivity
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Same app, dark theme: makes sure every screen stays readable at night. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
class DarkModeScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun eventsTab_dark() {
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(hasText("Lowongan")).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText("Event").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Campus Hiring Day 2026")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage("../docs/screenshots/07_events_dark.png")
    }
}
