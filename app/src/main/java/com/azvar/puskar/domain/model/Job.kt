package com.azvar.puskar.domain.model

import java.time.LocalDate

/**
 * A job or internship opening shown to students.
 * This is the UI/domain model; it never leaks Room or network annotations.
 */
data class Job(
    val id: String,
    val title: String,
    val company: String,
    val industry: String,
    val companyAbout: String,
    val location: String,
    val type: JobType,
    val workMode: WorkMode,
    val description: String,
    val requirements: List<String>,
    val benefits: List<String>,
    /** Free-form so ranges like "Rp4–6 jt/bulan" or "Uang saku" read naturally; null if not disclosed. */
    val salary: String?,
    val postedAt: LocalDate,
    val deadline: LocalDate,
    val applyUrl: String,
    val isBookmarked: Boolean = false,
)

enum class JobType(val label: String) {
    INTERNSHIP("Magang"),
    FULL_TIME("Full-time"),
    PART_TIME("Part-time"),
}

enum class WorkMode(val label: String) {
    ONSITE("On-site"),
    HYBRID("Hybrid"),
    REMOTE("Remote"),
}
