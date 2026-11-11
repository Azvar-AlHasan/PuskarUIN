package com.azvar.puskar.ui.jobs

import app.cash.turbine.test
import com.azvar.puskar.domain.model.JobSort
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.testing.FakeJobRepository
import com.azvar.puskar.testing.fixedClock
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
import java.time.LocalDate

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
        JobsViewModel(repository, fixedClock)
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `emits all jobs after loading`() = runTest {
        val viewModel = JobsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(listOf("1", "2"), state.jobs.map { it.id })
        }
    }

    @Test
    fun `search query filters the list`() = runTest {
        val viewModel = JobsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("backend")
            assertEquals(listOf("2"), awaitItem().jobs.map { it.id })
        }
    }

    @Test
    fun `selecting the same type twice clears the filter`() = runTest {
        val viewModel = JobsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onTypeSelected(JobType.INTERNSHIP)
            assertEquals(listOf("1"), awaitItem().jobs.map { it.id })
            viewModel.onTypeSelected(JobType.INTERNSHIP)
            assertEquals(2, awaitItem().jobs.size)
        }
    }

    @Test
    fun `bookmarking updates the list`() = runTest {
        val viewModel = JobsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onBookmarkClick("2")
            assertTrue(awaitItem().jobs.first { it.id == "2" }.isBookmarked)
        }
    }

    @Test
    fun `failed refresh shows an error that can be dismissed`() = runTest {
        repository.shouldFailRefresh = true
        val viewModel = JobsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Gagal memuat data terbaru", state.errorMessage)
            // Cached jobs are still shown even though the refresh failed (offline-first).
            assertEquals(2, state.jobs.size)
            viewModel.onErrorShown()
            assertNull(awaitItem().errorMessage)
        }
    }

    @Test
    fun `newest sort puts the most recently posted job first`() = runTest {
        val repo = FakeJobRepository(
            listOf(
                testJob("old", postedAt = LocalDate.of(2026, 11, 1), deadline = LocalDate.of(2026, 11, 20)),
                testJob("new", postedAt = LocalDate.of(2026, 11, 14), deadline = LocalDate.of(2026, 12, 20)),
            ),
        )
        val viewModel = JobsViewModel(repo, fixedClock)
        viewModel.uiState.test {
            assertEquals(listOf("old", "new"), awaitItem().jobs.map { it.id }) // default: deadline
            viewModel.onSortSelected(JobSort.NEWEST)
            assertEquals(listOf("new", "old"), awaitItem().jobs.map { it.id })
        }
    }

    @Test
    fun `clear filters keeps the chosen sort`() = runTest {
        val viewModel = JobsViewModel(repository, fixedClock)
        viewModel.uiState.test {
            awaitItem()
            viewModel.onSortSelected(JobSort.NEWEST)
            awaitItem()
            viewModel.onQueryChange("backend")
            awaitItem()
            viewModel.onClearFilters()
            val state = awaitItem()
            assertEquals(JobSort.NEWEST, state.filter.sort)
            assertEquals("", state.filter.query)
            assertEquals(2, state.jobs.size)
        }
    }
}
