package com.azvar.puskar.domain.model

import java.time.LocalDate

enum class JobSort(val label: String) {
    DEADLINE("Deadline terdekat"),
    NEWEST("Terbaru"),
}

/**
 * Pure filtering and sorting logic, kept outside the ViewModel so it is trivial to unit test.
 */
data class JobFilter(
    val query: String = "",
    val type: JobType? = null,
    val bookmarkedOnly: Boolean = false,
    val sort: JobSort = JobSort.DEADLINE,
) {
    val isActive: Boolean get() = query.isNotBlank() || type != null || bookmarkedOnly

    /** Filters, then sorts. Closed openings always go to the bottom so open ones stay visible. */
    fun apply(jobs: List<Job>, today: LocalDate): List<Job> {
        val q = query.trim()
        val order = when (sort) {
            JobSort.DEADLINE -> compareBy<Job> { it.deadline }
            JobSort.NEWEST -> compareByDescending<Job> { it.postedAt }
        }
        return jobs
            .filter { job ->
                (type == null || job.type == type) &&
                    (!bookmarkedOnly || job.isBookmarked) &&
                    (
                        q.isEmpty() ||
                            job.title.contains(q, ignoreCase = true) ||
                            job.company.contains(q, ignoreCase = true) ||
                            job.location.contains(q, ignoreCase = true) ||
                            job.industry.contains(q, ignoreCase = true)
                        )
            }
            .sortedWith(compareBy<Job> { it.isClosed(today) }.then(order))
    }
}

fun Job.isClosed(today: LocalDate): Boolean = deadline.isBefore(today)
