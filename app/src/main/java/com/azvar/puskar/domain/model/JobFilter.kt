package com.azvar.puskar.domain.model

/**
 * Pure filtering logic kept outside the ViewModel so it is trivial to unit test.
 */
data class JobFilter(
    val query: String = "",
    val type: JobType? = null,
    val bookmarkedOnly: Boolean = false,
) {
    fun apply(jobs: List<Job>): List<Job> {
        val q = query.trim()
        return jobs.filter { job ->
            (type == null || job.type == type) &&
                (!bookmarkedOnly || job.isBookmarked) &&
                (
                    q.isEmpty() ||
                        job.title.contains(q, ignoreCase = true) ||
                        job.company.contains(q, ignoreCase = true) ||
                        job.location.contains(q, ignoreCase = true)
                    )
        }
    }
}
