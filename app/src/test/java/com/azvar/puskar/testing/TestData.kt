package com.azvar.puskar.testing

import com.azvar.puskar.data.repository.EventRepository
import com.azvar.puskar.data.repository.JobRepository
import com.azvar.puskar.domain.model.CareerEvent
import com.azvar.puskar.domain.model.EVENT_ZONE
import com.azvar.puskar.domain.model.EventCategory
import com.azvar.puskar.domain.model.Job
import com.azvar.puskar.domain.model.JobType
import com.azvar.puskar.domain.model.WorkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

fun testJob(
    id: String,
    title: String = "Job $id",
    company: String = "Company $id",
    location: String = "Jakarta",
    type: JobType = JobType.INTERNSHIP,
    isBookmarked: Boolean = false,
    postedAt: LocalDate = LocalDate.of(2026, 11, 1),
    deadline: LocalDate = LocalDate.of(2026, 12, 1),
    industry: String = "Teknologi",
) = Job(
    id = id,
    title = title,
    company = company,
    industry = industry,
    companyAbout = "",
    location = location,
    type = type,
    workMode = WorkMode.ONSITE,
    description = "",
    requirements = emptyList(),
    benefits = emptyList(),
    salary = null,
    postedAt = postedAt,
    deadline = deadline,
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

val FIXED_NOW: LocalDateTime = LocalDateTime.of(2026, 11, 15, 10, 0)

/** A clock frozen at [FIXED_NOW] in WIB, so "upcoming" vs "past" never depends on when tests run. */
val fixedClock: Clock = Clock.fixed(
    FIXED_NOW.atZone(EVENT_ZONE).toInstant(),
    EVENT_ZONE,
)

fun testEvent(
    id: String,
    start: LocalDateTime,
    isOnline: Boolean = false,
    isReminderSet: Boolean = false,
) = CareerEvent(
    id = id,
    title = "Event $id",
    organizer = "Organizer",
    category = EventCategory.WORKSHOP,
    description = "",
    startAt = start,
    endAt = start.plusHours(2),
    location = if (isOnline) "Online" else "Ciputat",
    isOnline = isOnline,
    registrationUrl = "https://example.com",
    isReminderSet = isReminderSet,
)

class FakeEventRepository(initial: List<CareerEvent> = emptyList()) :
    EventRepository {
    private val events = MutableStateFlow(initial)

    override fun observeEvents() = events

    override fun observeEvent(id: String) = events.map { list -> list.find { it.id == id } }

    override suspend fun refresh(): Result<Unit> = Result.success(Unit)

    override suspend fun toggleReminder(eventId: String): Boolean {
        var enabled = false
        events.update { list ->
            list.map {
                if (it.id == eventId) it.copy(isReminderSet = !it.isReminderSet).also { e -> enabled = e.isReminderSet } else it
            }
        }
        return enabled
    }
}
