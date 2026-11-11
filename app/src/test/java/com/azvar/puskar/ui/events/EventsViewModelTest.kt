package com.azvar.puskar.ui.events

import app.cash.turbine.test
import com.azvar.puskar.testing.FIXED_NOW
import com.azvar.puskar.testing.FakeEventRepository
import com.azvar.puskar.testing.fixedClock
import com.azvar.puskar.testing.testEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private val repository = FakeEventRepository(
        listOf(
            testEvent("past-old", FIXED_NOW.minusDays(10)),
            testEvent("past-recent", FIXED_NOW.minusDays(2), isOnline = true),
            testEvent("soon", FIXED_NOW.plusDays(1), isOnline = true),
            testEvent("later", FIXED_NOW.plusDays(7)),
        ),
    )

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `splits events into upcoming (soonest first) and past (latest first)`() = runTest {
        EventsViewModel(repository, fixedClock).uiState.test {
            val state = awaitItem()
            assertEquals(listOf("soon", "later"), state.upcoming.map { it.id })
            assertEquals(listOf("past-recent", "past-old"), state.past.map { it.id })
        }
    }

    @Test
    fun `an event that is still running counts as upcoming`() = runTest {
        // Started 1 hour ago, ends in 1 hour.
        val repo = FakeEventRepository(listOf(testEvent("live", FIXED_NOW.minusHours(1))))
        EventsViewModel(repo, fixedClock).uiState.test {
            assertEquals(listOf("live"), awaitItem().upcoming.map { it.id })
        }
    }

    @Test
    fun `online filter keeps only online events`() = runTest {
        val viewModel = EventsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onModeSelected(EventModeFilter.ONLINE)
            val state = awaitItem()
            assertEquals(listOf("soon"), state.upcoming.map { it.id })
            assertEquals(listOf("past-recent"), state.past.map { it.id })
        }
    }

    @Test
    fun `toggling a reminder updates the event and shows a message`() = runTest {
        val viewModel = EventsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onReminderClick("soon")
            val state = expectMostRecentItem()
            assertTrue(state.upcoming.first { it.id == "soon" }.isReminderSet)
            assertEquals(EventsViewModel.REMINDER_ON, state.message)
        }
    }
}
