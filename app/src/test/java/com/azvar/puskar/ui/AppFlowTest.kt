package com.azvar.puskar.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azvar.puskar.MainActivity
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * End-to-end test that runs the real app (Hilt + Room + bundled data) on the JVM
 * with Robolectric, and records screenshots with Roborazzi for the README.
 *
 * Record screenshots:  ./gradlew recordRoborazziDebug
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class AppFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun jobList_loadsAndOpensDetail() {
        waitForText("Android Developer Intern")
        composeRule.onRoot().captureRoboImage("../docs/screenshots/01_job_list.png")

        composeRule.onNodeWithText("Android Developer Intern").performClick()
        waitForText("Lamar Sekarang")
        composeRule.onNodeWithText("Kualifikasi").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("../docs/screenshots/02_job_detail.png")
    }

    @Test
    fun search_filtersList() {
        waitForText("Android Developer Intern")
        composeRule.onNodeWithText("Cari posisi, perusahaan, atau lokasi").performTextInput("data")
        waitForText("Data Analyst Intern")
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Android Developer Intern")).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onRoot().captureRoboImage("../docs/screenshots/03_search.png")
    }
}
