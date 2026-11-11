package com.azvar.puskar.ui

import android.Manifest
import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.azvar.puskar.MainActivity
import com.azvar.puskar.data.reminder.WorkManagerEventReminderScheduler
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
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

    @Before
    fun setUpWorkManager() {
        // Synchronous WorkManager so enqueued work can be inspected immediately.
        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(),
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
    }

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

    @Test
    fun events_tabShowsEventsAndDetail() {
        waitForText("Android Developer Intern")
        composeRule.onNodeWithText("Event").performClick()
        waitForText("Campus Hiring Day 2026")
        composeRule.onRoot().captureRoboImage("../docs/screenshots/04_events.png")

        composeRule.onNodeWithText("Campus Hiring Day 2026").performClick()
        waitForText("Tambah ke Kalender")
        composeRule.onRoot().captureRoboImage("../docs/screenshots/05_event_detail.png")
    }

    @Test
    fun reminder_schedulesNotificationWork() {
        // Simulate the user granting the Android 13+ notification permission.
        shadowOf(ApplicationProvider.getApplicationContext<Application>())
            .grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

        waitForText("Android Developer Intern")
        composeRule.onNodeWithText("Event").performClick()
        waitForText("Campus Hiring Day 2026")
        composeRule.onNodeWithText("Campus Hiring Day 2026").performClick()
        waitForText("Ingatkan Saya")
        composeRule.onNodeWithText("Ingatkan Saya").performClick()
        waitForText("Pengingat aktif (1 jam sebelum)")

        val work = WorkManager.getInstance(ApplicationProvider.getApplicationContext())
            .getWorkInfosForUniqueWork(WorkManagerEventReminderScheduler.workName("evt-001"))
            .get()
        assertEquals(1, work.size)
        assertEquals(WorkInfo.State.ENQUEUED, work.single().state)
    }

    @Test
    fun saved_tabShowsBookmarkedJob() {
        waitForText("Android Developer Intern")
        composeRule.onAllNodesWithContentDescription("Simpan lowongan")[0].performClick()
        composeRule.onNodeWithText("Tersimpan").performClick()
        waitForText("Lowongan tersimpan (1)")
        composeRule.onRoot().captureRoboImage("../docs/screenshots/06_saved.png")
    }
}
