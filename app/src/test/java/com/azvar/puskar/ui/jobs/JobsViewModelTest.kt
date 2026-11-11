package com.azvar.puskar.ui.jobs

import app.cash.turbine.test
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.testing.FakeJobRepository
import com.azvar.puskar.testing.testJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobsViewModelTest {

    private lateinit var repository: FakeJobRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeJobRepository(
            listOf(
                testJob("1", title = "Android Intern", type = JobType.INTERNSHIP),
                testJob("2", title = "Backend Engineer", type = JobType.FULL_TIME),
            ),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshes once on start`() {
        JobsViewModel(repository)
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `emits all jobs after loading`() = runTest {
        val viewModel = JobsViewModel(repository)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(listOf("1", "2"), state.jobs.map { it.id })
        }
    }

    @Test
    fun `search query filters the list`() = runTest {
        val viewModel = JobsViewModel(repository)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("backend")
            assertEquals(listOf("2"), awaitItem().jobs.map { it.id })
        }
    }

    @Test
    fun `selecting the same type twice clears the filter`() = runTest {
        val viewModel = JobsViewModel(repository)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onTypeSelected(JobType.INTERNSHIP)
            assertEquals(listOf("1"), awaitItem().jobs.map { it.id })
            viewModel.onTypeSelected(JobType.INTERNSHIP)
            assertEquals(2, awaitItem().jobs.size)
        }
    }

    @Test
    fun `bookmarking updates the list and the saved filter`() = runTest {
        val viewModel = JobsViewModel(repository)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onBookmarkClick("2")
            assertTrue(awaitItem().jobs.first { it.id == "2" }.isBookmarked)
            viewModel.onBookmarkedOnlyToggle()
            assertEquals(listOf("2"), awaitItem().jobs.map { it.id })
        }
    }

    @Test
    fun `failed refresh shows an error that can be dismissed`() = runTest {
        repository.shouldFailRefresh = true
        val viewModel = JobsViewModel(repository)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Gagal memuat data terbaru", state.errorMessage)
            // Cached jobs are still shown even though the refresh failed (offline-first).
            assertEquals(2, state.jobs.size)
            viewModel.onErrorShown()
            assertNull(awaitItem().errorMessage)
        }
    }
}
