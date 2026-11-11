package com.azvar.puskar.testing

import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.domain.model.WorkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

fun testJob(
    id: String,
    title: String = "Job $id",
    company: String = "Company $id",
    location: String = "Jakarta",
    type: JobType = JobType.INTERNSHIP,
    isBookmarked: Boolean = false,
) = Job(
    id = id,
    title = title,
    company = company,
    location = location,
    type = type,
    workMode = WorkMode.ONSITE,
    description = "",
    requirements = emptyList(),
    deadline = LocalDate.of(2026, 12, 1),
    applyUrl = "https://example.com",
    isBookmarked = isBookmarked,
)

/** In-memory fake: faster and more readable than mocking. */
class FakeJobRepository(initial: List<Job> = emptyList()) : JobRepository {
    private val jobs = MutableStateFlow(initial)
    var shouldFailRefresh = false
    var refreshCount = 0
        private set

    override fun observeJobs(): Flow<List<Job>> = jobs

    override fun observeJob(id: String): Flow<Job?> = jobs.map { list -> list.find { it.id == id } }

    override suspend fun refresh(): Result<Unit> {
        refreshCount++
        return if (shouldFailRefresh) Result.failure(RuntimeException("offline")) else Result.success(Unit)
    }

    override suspend fun toggleBookmark(jobId: String) {
        jobs.update { list ->
            list.map { if (it.id == jobId) it.copy(isBookmarked = !it.isBookmarked) else it }
        }
    }
}
